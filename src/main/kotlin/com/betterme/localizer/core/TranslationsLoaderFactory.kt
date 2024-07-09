package com.betterme.localizer.core

import com.betterme.localizer.data.TranslationsLocalStore
import com.betterme.localizer.data.TranslationsLocalStoreImpl
import com.betterme.localizer.data.TranslationsRestStore
import com.betterme.localizer.data.TranslationsRestStoreImpl
import com.betterme.localizer.data.models.ApiParams
import com.google.gson.Gson
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class TranslationsLoaderFactory {

    companion object {

        /**
         * @param apiParams params necessary for interaction with POEditor API.
         */
        @JvmStatic
        fun create(apiParams: ApiParams): TranslationsLoader {
            return TranslationsLoaderImpl(
                apiParams = apiParams,
                restStore = translationsRestStore(),
                localStore = translationsLocalStore()
            )
        }

        private fun translationsRestStore(): TranslationsRestStore {
            val client = OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build() // PoEditor API seems to be slow to generate large translation files
            return TranslationsRestStoreImpl(client, Gson())
        }

        private fun translationsLocalStore(): TranslationsLocalStore = TranslationsLocalStoreImpl()
    }
}