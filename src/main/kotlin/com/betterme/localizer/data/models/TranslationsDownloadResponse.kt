package com.betterme.localizer.data.models

import com.betterme.localizer.data.constants.MetaDataContants
import com.google.gson.annotations.SerializedName

internal data class TranslationsDownloadResponse(
    @SerializedName(MetaDataContants.Params.Result.PARAM_RESULT) val result: TranslationsDownloadResult
)

internal data class TranslationsDownloadResult(
    @SerializedName(MetaDataContants.Params.Result.PARAM_URL) val url: String
)
