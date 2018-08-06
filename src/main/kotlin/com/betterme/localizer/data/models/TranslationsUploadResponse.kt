package com.betterme.localizer.data.models

import com.google.gson.annotations.SerializedName

data class TranslationsUploadResponse(
        @SerializedName("response") val response: ResponseModel,
        @SerializedName("result") val result: ResultModel
)

data class ResponseModel(
        @SerializedName("status") val status: String,
        @SerializedName("code") val code: Int,
        @SerializedName("message") val message: String
)

data class ResultModel(
        @SerializedName("translations") val translations: TranslationsModel,
        @SerializedName("terms") val terms: TermsModel
)

data class TranslationsModel(
        @SerializedName("parsed") val parsed: Int,
        @SerializedName("added") val added: Int,
        @SerializedName("updated") val updated: Int
)

data class TermsModel(
        @SerializedName("parsed") val parsed: Int,
        @SerializedName("added") val added: Int,
        @SerializedName("updated") val updated: Int
)
