package world.betterme.localizer.core

import world.betterme.localizer.data.TranslationsLocalStore
import world.betterme.localizer.data.TranslationsRestStore
import world.betterme.localizer.data.models.ApiParams

interface TranslationsLoader {

    /**
     * Downloads and updates localized strings.xml file from POEditor.
     *
     * @param resFolderPath path to project's resources folder.
     * @param filters list of filters by which the downloaded strings will be filtered: 'translated', 'untranslated',
     *              'fuzzy', 'not_fuzzy', 'automatic', 'not_automatic', 'proofread', 'not_proofread'
     * @param tags list of tags, by which the downloaded strings will be filtered.
     */
    fun downloadLocalizedStrings(
        resFolderPath: String,
        filters: List<String>,
        tags: List<String>,
        supportRegions: Boolean,
        validateTranslations: Boolean,
    )

    /**
     * Uploads original strings.xml file to POEditor for a given exportLocale.
     *
     * @param resFolderPath path to project's resources folder.
     * @param locale desired exportLocale.
     * @param overwrite true if strings with the existing keys need to be overwritten.
     */
    fun uploadTermsAndTranslations(
        resFolderPath: String,
        locale: String,
        overwrite: Boolean,
        syncTerms: Boolean,
        supportRegions: Boolean
    )
}

internal class TranslationsLoaderImpl(
    private val apiParams: ApiParams,
    private val restStore: TranslationsRestStore,
    private val localStore: TranslationsLocalStore,
    private val translationsValidator: TranslationsValidator,
) : TranslationsLoader {

    override fun downloadLocalizedStrings(
        resFolderPath: String,
        filters: List<String>,
        tags: List<String>,
        supportRegions: Boolean,
        validateTranslations: Boolean,
    ) {
        val availableLocales = restStore.getAvailableLanguages(apiParams)
        println("Retrieved list of locales available for this project: $availableLocales")
        val urls = restStore.loadTranslationsUrls(availableLocales, filters, tags, apiParams)
        urls.forEach { (locale, url) ->
            println("Starting translations for exportLocale $locale download with filters [$filters] and tags [$tags]")
            val fileContent = restStore.loadTranslationsContent(url)
            if (validateTranslations) {
                translationsValidator.appendTranslationContent(locale, fileContent)
            }
            println("Saving translations for exportLocale $locale")
            localStore.saveToFile(resFolderPath, fileContent, locale, supportRegions)
        }

        if (validateTranslations) {
            translationsValidator.validateAll()
        }
    }

    override fun uploadTermsAndTranslations(
        resFolderPath: String,
        locale: String,
        overwrite: Boolean,
        syncTerms: Boolean,
        supportRegions: Boolean
    ) {
        val stringsFilePath = localStore.getStringsFilePath(resFolderPath, locale, supportRegions)
        restStore.uploadStringsFile(apiParams, stringsFilePath, locale, overwrite, syncTerms)
    }
}
