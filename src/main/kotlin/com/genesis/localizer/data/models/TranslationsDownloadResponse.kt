package com.genesis.localizer.data.models

import com.genesis.localizer.data.MetaDataContants
import com.google.gson.annotations.SerializedName

internal data class TranslationsDownloadResponse(
        @SerializedName(MetaDataContants.Params.Result.PARAM_RESULT) val result: TranslationsDownloadResult
)

internal data class TranslationsDownloadResult(
        @SerializedName(MetaDataContants.Params.Result.PARAM_URL) val url: String
)
