package com.betterme.localizer.data

import com.betterme.localizer.data.models.ApiParams
import com.betterme.localizer.data.models.AvailableLanguagesResponse
import com.betterme.localizer.data.models.TranslationsDownloadResponse
import com.betterme.localizer.data.models.TranslationsUploadResponse
import com.google.gson.Gson
import okhttp3.*
import java.io.File
import java.io.IOException

internal interface TranslationsRestStore {

    fun loadTranslationsUrls(locales: List<String>, apiParams: ApiParams): Map<String, String>

    fun loadTranslationsUrl(locale: String, apiParams: ApiParams): String

    fun loadTranslationsContent(translationsUrl: String): String

    fun getAvailableLanguages(apiParams: ApiParams): List<String>

    fun uploadStringsFile(apiParams: ApiParams, filePath: String, locale: String, overWrite: Boolean)
}

internal class TranslationsRestStoreImpl(
        private val okHttpClient: OkHttpClient,
        private val gson: Gson
) : TranslationsRestStore {

    override fun loadTranslationsUrls(locales: List<String>, apiParams: ApiParams): Map<String, String> {
        val urls = mutableMapOf<String, String>()
        locales.forEach { locale ->
            val url = loadTranslationsUrl(locale, apiParams)
            urls[locale] = url
        }
        return urls
    }

    override fun loadTranslationsUrl(locale: String, apiParams: ApiParams): String {
        val postRequest = createTranslationsExportRequest(locale, apiParams.apiToken, apiParams.projectId)

        val postResponse = okHttpClient.newCall(postRequest).execute()
        if (!postResponse.isSuccessful) {
            throw IOException("Could not execute export request: $postResponse")
        }
        val postResponseString = postResponse.body()?.string()
        val fileResult = gson.fromJson(postResponseString, TranslationsDownloadResponse::class.java)
        return fileResult.result.url
    }

    override fun loadTranslationsContent(translationsUrl: String): String {
        val fileLoadingRequest = Request.Builder()
                .url(translationsUrl)
                .build()

        val fileResponse = okHttpClient.newCall(fileLoadingRequest).execute()
        if (!fileResponse.isSuccessful) throw IOException()
        return fileResponse.body()?.string() ?: throw IllegalStateException("Could not retrieve " +
                "file contents")
    }

    override fun getAvailableLanguages(apiParams: ApiParams): List<String> {
        val requestBody = FormBody.Builder()
                .add(MetaDataContants.Params.PARAM_API_TOKEN, apiParams.apiToken)
                .add(MetaDataContants.Params.PARAM_PROJECT_ID, apiParams.projectId)
                .build()
        val request = Request.Builder()
                .url(MetaDataContants.API_ENDPOINT.plus(MetaDataContants.API_REQUEST_AVAILABLE_LANGUAGES))
                .post(requestBody)
                .build()

        val rawLanguageResponse = okHttpClient.newCall(request).execute()
        if (!rawLanguageResponse.isSuccessful) throw IOException("Could not execute languages retrieval response")
        val languageResponseString = rawLanguageResponse.body()?.string()
        val languageResponse = gson.fromJson(languageResponseString, AvailableLanguagesResponse::class.java)
        return languageResponse.result.languages.map { it.code }
    }

    override fun uploadStringsFile(apiParams: ApiParams, filePath: String, locale: String, overWrite: Boolean) {
        val fileUploadingRequest = createTranslationsUploadRequest(apiParams.apiToken,
                apiParams.projectId, filePath, overWrite, locale)

        val fileResponse = okHttpClient.newCall(fileUploadingRequest).execute()
        if (!fileResponse.isSuccessful) throw IOException()
        val fileResponseString = fileResponse.body()?.string() ?: throw IllegalStateException("Could " +
                "not upload translations file")
        val uploadResponse = gson.fromJson(fileResponseString, TranslationsUploadResponse::class.java)
        println("Translations file $filePath upload response: $uploadResponse")
        return
    }

    private fun createTranslationsExportRequest(locale: String, apiToken: String, projectId: String): Request {
        val requestBody = FormBody.Builder()
                .add(MetaDataContants.Params.PARAM_API_TOKEN, apiToken)
                .add(MetaDataContants.Params.PARAM_PROJECT_ID, projectId)
                .add(MetaDataContants.Params.PARAM_LANGUAGE, locale)
                .add(MetaDataContants.Params.PARAM_TYPE, MetaDataContants.Values.VALUE_TYPE_ANDROID_STRINGS)
                .build()
        return Request.Builder()
                .url(MetaDataContants.API_ENDPOINT.plus(MetaDataContants.API_REQUEST_EXPORT))
                .post(requestBody)
                .build()
    }

    private fun createTranslationsUploadRequest(
            apiToken: String, projectId: String, filePath: String, overWrite: Boolean,
            locale: String = "en"
    ): Request {

        val overwriteCode = if (overWrite) "1" else "0"

        val fileRequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", filePath, RequestBody
                        .create(MediaType.parse("xml"), File(filePath)))
                .addFormDataPart(MetaDataContants.Params.PARAM_API_TOKEN, apiToken)
                .addFormDataPart(MetaDataContants.Params.PARAM_PROJECT_ID, projectId)
                .addFormDataPart(MetaDataContants.Params.PARAM_LANGUAGE, locale)
                .addFormDataPart(MetaDataContants.Params.PARAM_FILE, filePath)
                .addFormDataPart(MetaDataContants.Params.PARAM_UPDATING,
                        MetaDataContants.Values.VALUE_UPDATING_TERMS_AND_TRANSLATIONS)
                .addFormDataPart(MetaDataContants.Params.PARAM_OVERWRITE, overwriteCode)
                .build()

        return Request.Builder()
                .url(MetaDataContants.API_ENDPOINT.plus(MetaDataContants.API_REQUEST_UPLOAD))
                .post(fileRequestBody)
                .build()
    }

}