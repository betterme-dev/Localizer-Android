package com.betterme.localizer.data.constants

internal object MetaDataContants {

    const val API_ENDPOINT = "https://api.poeditor.com"
    const val API_REQUEST_EXPORT = "/v2/projects/export"
    const val API_REQUEST_UPLOAD = "/v2/projects/upload"
    const val API_REQUEST_AVAILABLE_LANGUAGES = "/v2/languages/list"

    object Params {
        const val PARAM_API_TOKEN = "api_token"
        const val PARAM_TYPE = "type"
        const val PARAM_LANGUAGE = "language"
        const val PARAM_FILTERS = "filters"
        const val PARAM_TAGS = "tags"
        const val PARAM_PROJECT_ID = "id"
        const val PARAM_UPDATING = "updating"
        const val PARAM_FILE = "file"
        const val PARAM_OVERWRITE = "overwrite"
        const val PARAM_SYNC_TERMS = "sync_terms"

        object Result {
            const val PARAM_RESULT = "result"
            const val PARAM_URL = "url"
        }
    }

    object Values {
        const val VALUE_TYPE_ANDROID_STRINGS = "android_strings"
        const val VALUE_UPDATING_TERMS_AND_TRANSLATIONS = "terms_translations"

        object Locales {
            const val VALUE_ENG = "en"
            const val VALUE_AF = "af"
            const val VALUE_IW = "iw"
            private const val VALUE_NL = "nl"
            private const val VALUE_HE = "he"

            fun String.isDutch(): Boolean {
                return this == VALUE_NL
            }

            fun String.isHebrew(): Boolean {
                return this == VALUE_HE
            }
        }
    }
}