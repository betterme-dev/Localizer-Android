package world.betterme.localizer.core

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import world.betterme.localizer.data.TranslationsLocalStore
import world.betterme.localizer.data.TranslationsRestStore
import world.betterme.localizer.data.constants.MetaDataContants.Values.Locales.VALUE_ENG
import world.betterme.localizer.data.models.ApiParams
import java.io.ByteArrayInputStream
import java.io.File
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

interface TranslationsLoader {

    /**
     * Downloads and updates localized strings.xml file from POEditor.
     *
     * @param resFolderPath path to project's resources folder.
     * @param filters list of filters by which the downloaded strings will be filtered: 'translated', 'untranslated',
     *              'fuzzy', 'not_fuzzy', 'automatic', 'not_automatic', 'proofread', 'not_proofread'
     * @param tags list of tags, by which the downloaded strings will be filtered.
     * @param supportRegions whether to support regions in locale names.
     * @param fullMode if true, downloads all strings for all locales.
     * @param keys list of keys to download and update only these strings, if empty, downloads only strings that are available in main strings.xml
     * and not in translations.
     */
    fun downloadLocalizedStrings(
        resFolderPath: String,
        filters: List<String>,
        tags: List<String>,
        supportRegions: Boolean,
        fullMode: Boolean = false,
        keys: List<String> = emptyList(),
    )

    /**
     * Uploads original strings.xml file to POEditor for a given locale.
     *
     * @param resFolderPath path to project's resources folder.
     * @param locale desired locale.
     * @param overwrite true if strings with the existing keys need to be overwritten.
     */
    fun uploadTermsAndTranslations(
        resFolderPath: String,
        locale: String,
        overwrite: Boolean,
        syncTerms: Boolean,
        supportRegions: Boolean,
    )
}

internal class TranslationsLoaderImpl(
    private val apiParams: ApiParams,
    private val restStore: TranslationsRestStore,
    private val localStore: TranslationsLocalStore,
) : TranslationsLoader {

    override fun downloadLocalizedStrings(
        resFolderPath: String,
        filters: List<String>,
        tags: List<String>,
        supportRegions: Boolean,
        fullMode: Boolean,
        keys: List<String>,
    ) {
        val availableLocales = restStore.getAvailableLanguages(apiParams)
        val urls = restStore.loadTranslationsUrls(availableLocales, filters, tags, apiParams)
        urls.forEach { (locale, url) ->
            val fileContent = restStore.loadTranslationsContent(url)

            runCatching {
                when {
                    // Save all the translations
                    fullMode -> {
                        val (restDoc, allKeys) = parseTranslations(fileContent)
                        val resultContent = writeTranslations(restDoc, allKeys)
                        localStore.saveToFile(resFolderPath, resultContent, locale, supportRegions)
                    }

                    // Update only the translations with specified keys
                    keys.isNotEmpty() -> {
                        val (restDoc, _) = parseTranslations(fileContent)
                        val localStringsFilePath = localStore.getStringsFilePath(resFolderPath, locale, supportRegions)
                        val localTranslationsKeys = if (File(localStringsFilePath).exists()) {
                            val localFileContent = File(localStringsFilePath).readText()
                            val (_, localKeys) = parseTranslations(localFileContent)
                            localKeys
                        } else {
                            emptySet()
                        }
                        val keysToKeep = localTranslationsKeys + keys
                        val filteredContent = writeTranslations(restDoc, keysToKeep)
                        localStore.saveToFile(resFolderPath, filteredContent, locale, supportRegions)
                    }

                    // Update only the keys that are present in the main strings file
                    else -> {
                        val (restDoc, _) = parseTranslations(fileContent)
                        val mainLocale = VALUE_ENG
                        val mainStringsFilePath = localStore.getStringsFilePath(resFolderPath, mainLocale, supportRegions)
                        val mainTranslationsKeys = if (File(mainStringsFilePath).exists()) {
                            val localFileContent = File(mainStringsFilePath).readText()
                            val (_, mainKeys) = parseTranslations(localFileContent)
                            mainKeys
                        } else {
                            emptySet()
                        }
                        val filteredContent = writeTranslations(restDoc, mainTranslationsKeys)
                        localStore.saveToFile(resFolderPath, filteredContent, locale, supportRegions)
                    }
                }
            }
                .fold(
                    onSuccess = {
                        println("Download success: $locale")
                    },
                    onFailure = { error ->
                        // We need to die gracefully on translations parsing or download failures.
                        val errorMessage = "Download error: Did not manage to parse strings for locale: $locale "
                            .plus("\nvia URL: $url")
                            .plus("\nError: ${error.message}")
                            .plus("\n")
                            .plus(
                                "Make sure there are no files with empty strings list and the strings files " +
                                "themselves are downloaded in a correct format."
                            )
                        println(errorMessage)
                    }
                )
        }
    }

    override fun uploadTermsAndTranslations(
        resFolderPath: String,
        locale: String,
        overwrite: Boolean,
        syncTerms: Boolean,
        supportRegions: Boolean,
    ) {
        val stringsFilePath = localStore.getStringsFilePath(resFolderPath, locale, supportRegions)
        restStore.uploadStringsFile(apiParams, stringsFilePath, locale, overwrite, syncTerms)
    }
}

private fun parseTranslations(xmlContent: String): Pair<Document, Set<String>> {
    val factory = DocumentBuilderFactory.newInstance().apply {
        isIgnoringComments = false
        isIgnoringElementContentWhitespace = false
    }
    val builder = factory.newDocumentBuilder()
    val inputStream = ByteArrayInputStream(xmlContent.toByteArray(Charsets.UTF_8))
    val doc = builder.parse(inputStream)
    val stringElements = doc.getElementsByTagName("string")
    val keys = mutableSetOf<String>()
    for (i in 0 until stringElements.length) {
        val node = stringElements.item(i)
        if (node.nodeType == Node.ELEMENT_NODE) {
            val element = node as Element
            val key = element.getAttribute("name")
            keys.add(key)
        }
    }
    return Pair(doc, keys)
}

private fun writeTranslations(doc: Document, keysToKeep: Set<String>): String {
    val stringElements = doc.getElementsByTagName("string")
    val nodesToRemove = mutableListOf<Node>()
    for (i in 0 until stringElements.length) {
        val node = stringElements.item(i)
        if (node.nodeType == Node.ELEMENT_NODE) {
            val element = node as Element
            val key = element.getAttribute("name")
            if (key !in keysToKeep) {
                nodesToRemove.add(node)
            }
        }
    }
    for (node in nodesToRemove) {
        node.parentNode.removeChild(node)
    }
    val transformerFactory = TransformerFactory.newInstance()
    val transformer = transformerFactory.newTransformer().apply {
        // UTF-8 is broken
        // https://stackoverflow.com/q/15592025/
        setOutputProperty(OutputKeys.ENCODING, "UTF-16")
        setOutputProperty(OutputKeys.METHOD, "xml")
        setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        setOutputProperty(OutputKeys.INDENT, "no")
    }
    val writer = StringWriter()
    val docWrapper = object : Document by doc {
        override fun getXmlEncoding(): String? {
            // UTF-8 is broken
            // https://stackoverflow.com/a/38232925/12793676
            return null
        }
    }
    transformer.transform(DOMSource(docWrapper), StreamResult(writer))
    var result = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n$writer"
    result = result.replace(Regex("\\n\\s*\\n"), "\n\n")
    return result
}