package com.genesis.localizer

import com.genesis.localizer.core.TranslationsLoaderFactory
import com.genesis.localizer.data.models.ApiParams
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class TranslationsDownloaderTask : DefaultTask() {

    @Input val apiToken = project.objects.property(String::class.java)
    @Input val projectId = project.objects.property(String::class.java)
    @Input val resourcesPath = project.objects.property(String::class.java)

    init {
        description = "Downloads translations for all locales supported in this project"
        group = "translations"
    }

    @TaskAction
    fun downloadTranslations() {
        val apiParams = ApiParams(apiToken = apiToken.get(), projectId = projectId.get())
        val translationsLoader = TranslationsLoaderFactory.create(apiParams)
        translationsLoader.downloadLocalizedStrings(resourcesPath.get())
    }
}
