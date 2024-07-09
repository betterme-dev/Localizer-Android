package com.betterme.localizer.data.models

import com.google.gson.annotations.SerializedName

internal data class AvailableLanguagesResponse(@SerializedName("result") val result: Result)

internal data class Result(@SerializedName("languages") val languages: List<LanguageItem>)

internal data class LanguageItem(
    val name: String,
    val code: String
)