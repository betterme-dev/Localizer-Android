package com.betterme.localizer

import com.betterme.localizer.core.TranslationsLoaderFactory
import com.betterme.localizer.data.models.ApiParams
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class TranslationsUploaderTask : DefaultTask() {

    @Input val apiToken = project.objects.property(String::class.java)
    @Input val projectId = project.objects.property(String::class.java)
    @Input val resourcesPath = project.objects.property(String::class.java)
    @Input val locale = project.objects.property(String::class.java)
    @Input val overwrite = project.objects.property(String::class.java)

    init {
        description = "Uploads terms and translations in a current project for a given locale"
        group = "translations"
    }

    @Suppress("PlatformExtensionReceiverOfInline")
    @TaskAction
    fun uploadTranslations() {
        val apiParams = ApiParams(apiToken = apiToken.get(), projectId = projectId.get())
        val translationsLoader = TranslationsLoaderFactory.create(apiParams)
        translationsLoader.uploadTermsAndTranslations(resourcesPath.get(), locale.get(),
                /* Small temp hack to avoid Groovy and Kotlin boolean incompatibility issue */
                overwrite.get().toBoolean())
    }
}