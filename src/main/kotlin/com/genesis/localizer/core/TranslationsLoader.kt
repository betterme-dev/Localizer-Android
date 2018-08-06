package com.genesis.localizer.core

import com.genesis.localizer.data.TranslationsLocalStore
import com.genesis.localizer.data.TranslationsRestStore
import com.genesis.localizer.data.models.ApiParams

interface TranslationsLoader {

    /**
     * Downloads and updates localized strings.xml file from POEditor.
     *
     * @param resFolderPath path to project's resources folder.
     */
    fun downloadLocalizedStrings(resFolderPath: String)

    /**
     * Uploads original strings.xml file to POEditor for a given locale.
     *
     * @param resFolderPath path to project's resources folder.
     * @param locale desired locale.
     * @param overwrite true if strings with the existing keys need to be overwritten.
     */
    fun uploadTermsAndTranslations(resFolderPath: String, locale: String, overwrite: Boolean)
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
            println("Starting translations for locale $locale download")
            val fileContent = restStore.loadTranslationsContent(url)
            println("Saving translations for locale $locale")
            localStore.saveToFile(resFolderPath, fileContent, locale)
        }
    }

    override fun uploadTermsAndTranslations(resFolderPath: String, locale: String, overwrite: Boolean) {
        val stringsFilePath = localStore.getStringsFilePath(resFolderPath)
        restStore.uploadStringsFile(apiParams, stringsFilePath, locale, overwrite)
    }
}
