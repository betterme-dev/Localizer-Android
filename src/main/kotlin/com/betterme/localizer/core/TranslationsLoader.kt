package com.betterme.localizer.core

import com.betterme.localizer.data.TranslationsLocalStore
import com.betterme.localizer.data.TranslationsRestStore
import com.betterme.localizer.data.models.ApiParams

interface TranslationsLoader {

    /**
     * Downloads and updates localized strings.xml file from POEditor.
     *
     * @param resFolderPath path to project's resources folder.
     * @param filters list of filters by which the downloaded strings will be filtered: 'translated', 'untranslated',
     *              'fuzzy', 'not_fuzzy', 'automatic', 'not_automatic', 'proofread', 'not_proofread'
     * @param tags list of tags, by which the downloaded strings will be filtered.
     * @param languageFilters list of supported languages, other languages available will be filtered. Eg. ['en','tr']
     */
    fun downloadLocalizedStrings(resFolderPath: String, filters: List<String>, tags: List<String>, supportRegions: Boolean, languageFilters: List<String>)

    /**
     * Uploads original strings.xml file to POEditor for a given exportLocale.
     *
     * @param resFolderPath path to project's resources folder.
     * @param locale desired exportLocale.
     * @param overwrite true if strings with the existing keys need to be overwritten.
     */
    fun uploadTermsAndTranslations(resFolderPath: String, locale: String, overwrite: Boolean, syncTerms: Boolean, supportRegions: Boolean)
}

internal class TranslationsLoaderImpl(
    private val localStore: TranslationsLocalStore,
    private val restStore: TranslationsRestStore,
    private val apiParams: ApiParams
) : TranslationsLoader {

    override fun downloadLocalizedStrings(resFolderPath: String, filters: List<String>, tags: List<String>, supportRegions: Boolean, languageFilters: List<String>) {
        var availableLocales = restStore.getAvailableLanguages(apiParams)
        println("Retrieved list of locales available for this project: $availableLocales")
        if (languageFilters.isNotEmpty()) {
            availableLocales = availableLocales.intersect(languageFilters).toList()
            println("Filtered list of locales available for this project: $availableLocales")
        }
        val urls = restStore.loadTranslationsUrls(availableLocales, filters, tags, apiParams)
        urls.forEach { (locale, url) ->
            println("Starting translations for exportLocale $locale download with filters [$filters] and tags [$tags]")
            val fileContent = restStore.loadTranslationsContent(url)
            println("Saving translations for exportLocale $locale")
            localStore.saveToFile(resFolderPath, fileContent, locale, supportRegions)
        }
    }

    override fun uploadTermsAndTranslations(resFolderPath: String, locale: String, overwrite: Boolean, syncTerms: Boolean, supportRegions: Boolean) {
        val stringsFilePath = localStore.getStringsFilePath(resFolderPath, locale, supportRegions)
        restStore.uploadStringsFile(apiParams, stringsFilePath, locale, overwrite, syncTerms)
    }
}
