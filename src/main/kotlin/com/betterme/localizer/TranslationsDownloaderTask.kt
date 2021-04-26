package com.betterme.localizer

import com.betterme.localizer.core.TranslationsLoaderFactory
import com.betterme.localizer.data.models.ApiParams
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

open class TranslationsDownloaderTask : DefaultTask() {

    @Input val apiToken = project.objects.property(String::class.java)
    @Input val projectId = project.objects.property(String::class.java)
    @Input val resourcesPath = project.objects.property(String::class.java)
    @get:Input @get:Optional val supportRegions = project.objects.property(String::class.java)
    @get:Input @get:Optional val filters = project.objects.listProperty(String::class.java)
    @get:Input @get:Optional val tags = project.objects.listProperty(String::class.java)
    @get:Input @get:Optional val languageFilters = project.objects.listProperty(String::class.java)
    @get:Input @get:Optional val languageCodeMap = project.objects.mapProperty(String::class.java, String::class.java)

    init {
        description = "Downloads translations for all locales supported in this project"
        group = "translations"
    }

    @Suppress("UNNECESSARY_SAFE_CALL", "PlatformExtensionReceiverOfInline")
    @TaskAction
    fun downloadTranslations() {
        val apiParams = ApiParams(apiToken = apiToken.get(), projectId = projectId.get())
        val translationsLoader = TranslationsLoaderFactory.create(apiParams)
        translationsLoader.downloadLocalizedStrings(
            resFolderPath = resourcesPath.get(),
            filters = filters.getOrElse(emptyList()),
            tags = tags.getOrElse(emptyList()),
            supportRegions = supportRegions.getOrElse("false").toBoolean(),
            languageFilters = languageFilters.getOrElse(emptyList()),
            languageCodeMap = languageCodeMap.getOrElse(emptyMap<String, String>()) as Map<String, String>
        )
    }
}
