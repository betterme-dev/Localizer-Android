package com.betterme.localizer.core

import com.betterme.localizer.data.TranslationsLocalStore
import com.betterme.localizer.data.TranslationsRestStore
import com.betterme.localizer.data.models.ApiParams

interface TranslationsLoader {

    /**
     * Downloads and updates localized strings.xml file from POEditor.
     *
     * @param resFolderPath path to project's resources folder.
     */
    fun downloadLocalizedStrings(resFolderPath: String)

    /**
     * Uploads original strings.xml file to POEditor for a given exportLocale.
     *
     * @param resFolderPath path to project's resources folder.
     * @param locale desired exportLocale.
     * @param overwrite true if strings with the existing keys need to be overwritten.
     */
    fun uploadTermsAndTranslations(resFolderPath: String, locale: String, overwrite: Boolean, syncTerms: Boolean)
}

internal class TranslationsLoaderImpl(
        private val localStore: TranslationsLocalStore,
        private val restStore: TranslationsRestStore,
        private val apiParams: ApiParams
) : TranslationsLoader {

    override fun downloadLocalizedStrings(resFolderPath: String) {
        val availableLocales = restStore.getAvailableLanguages(apiParams)
        println("Retrieved list of locales available for this project: $availableLocales")
        val urls = restStore.loadTranslationsUrls(availableLocales, apiParams)
        urls.forEach { locale, url ->
            println("Starting translations for exportLocale $locale download")
            val fileContent = restStore.loadTranslationsContent(url)
            println("Saving translations for exportLocale $locale")
            localStore.saveToFile(resFolderPath, fileContent, locale)
        }
    }

    override fun uploadTermsAndTranslations(resFolderPath: String, locale: String, overwrite: Boolean, syncTerms: Boolean) {
        val stringsFilePath = localStore.getStringsFilePath(resFolderPath, locale)
        restStore.uploadStringsFile(apiParams, stringsFilePath, locale, overwrite, syncTerms)
    }
}
