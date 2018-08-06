package com.betterme.localizer.data

import java.io.File

internal interface TranslationsLocalStore {

    fun saveToFile(resFolderPath: String, fileContents: String, locale: String)

    fun getStringsFilePath(resFolderPath: String, locale: String = ""): String
}

internal class TranslationsLocalStoreImpl : TranslationsLocalStore {

    override fun saveToFile(resFolderPath: String, fileContents: String, locale: String) {
        val fileName = getStringsFilePath(resFolderPath, locale)
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
    }

    override fun getStringsFilePath(resFolderPath: String, locale: String): String {
        val valuesFolderPrefix = if (locale.isEmpty() || locale ==
                MetaDataContants.Values.Locales.VALUE_ENG) {

            "$resFolderPath/values"
        } else {
            "$resFolderPath/values-${locale.toLowerCase()}"
        }
        return "$valuesFolderPrefix/strings.xml"
    }
}
