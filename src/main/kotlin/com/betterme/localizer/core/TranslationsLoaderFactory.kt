package com.betterme.localizer.core

import com.betterme.localizer.data.TranslationsLocalStore
import com.betterme.localizer.data.TranslationsLocalStoreImpl
import com.betterme.localizer.data.TranslationsRestStore
import com.betterme.localizer.data.TranslationsRestStoreImpl
import com.betterme.localizer.data.models.ApiParams
import com.google.gson.Gson
import okhttp3.OkHttpClient

class TranslationsLoaderFactory {

    companion object {

        /**
         * @param apiParams params necessary for interaction with POEditor API.
         */
        @JvmStatic fun create(apiParams: ApiParams): TranslationsLoader {
            return TranslationsLoaderImpl(translationsLocalStore(), translationsRestStore(),
                    apiParams)
        }

        private fun translationsRestStore(): TranslationsRestStore {
            return TranslationsRestStoreImpl(OkHttpClient(), Gson())
        }

        private fun translationsLocalStore(): TranslationsLocalStore = TranslationsLocalStoreImpl()
    }
}