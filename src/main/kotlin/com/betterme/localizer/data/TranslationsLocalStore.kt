package com.betterme.localizer.data

import java.io.File
import com.betterme.localizer.data.constants.MetaDataContants.Values.Locales
import com.betterme.localizer.data.constants.MetaDataContants.Values.Locales.isDutch
import com.betterme.localizer.data.constants.MetaDataContants.Values.Locales.isHebrew

internal interface TranslationsLocalStore {

    fun saveToFile(
        resFolderPath: String,
        fileContents: String,
        locale: String,
        supportRegions: Boolean
    )

    fun getStringsFilePath(resFolderPath: String, locale: String, supportRegions: Boolean): String
}

internal class TranslationsLocalStoreImpl : TranslationsLocalStore {

    override fun saveToFile(
        resFolderPath: String,
        fileContents: String,
        locale: String,
        supportRegions: Boolean
    ) {
        val fileName = getStringsFilePath(resFolderPath, locale, supportRegions)
        val translationFile = File(fileName)
        if (!translationFile.parentFile.exists()) {
            translationFile.parentFile.mkdir()
        }
        if (!translationFile.exists()) {
            translationFile.createNewFile()
        }
        translationFile.bufferedWriter().use { out ->
            out.write(fileContents)
        }
        // Almost identical https://en.wikipedia.org/wiki/Comparison_of_Afrikaans_and_Dutch
        if (locale.isDutch()) {
            saveToFile(resFolderPath, fileContents, Locales.VALUE_AF, supportRegions)
        }
        // To handle this particular scenario https://stackoverflow.com/a/8202428
        if (locale.isHebrew()) {
            saveToFile(resFolderPath, fileContents, Locales.VALUE_IW, supportRegions)
        }
    }

    override fun getStringsFilePath(
        resFolderPath: String,
        locale: String,
        supportRegions: Boolean
    ): String {
        val isRegionalLocale = locale.contains(Regex("[a-z\\-A-Z]"))

        val valuesFolderPrefix = if (
            locale.isEmpty() || locale == Locales.VALUE_ENG
        ) {
            "$resFolderPath/values"
        } else if (isRegionalLocale && supportRegions) {
            val processedRegionalLocale = locale.replace("-", "-r")
            "$resFolderPath/values-$processedRegionalLocale"
        } else if (isRegionalLocale) {
            val processedLocaleName = locale.replace(Regex("[\\-A-Z]"), "")
            "$resFolderPath/values-$processedLocaleName"
        } else {
            "$resFolderPath/values-${locale.lowercase()}"
        }
        return "$valuesFolderPrefix/strings.xml"
    }
}
