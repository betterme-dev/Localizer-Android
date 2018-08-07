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
    @Input val exportLocale = project.objects.property(String::class.java)
    @Input val overwriteOnExport = project.objects.property(String::class.java)
    @Input val syncTerms = project.objects.property(String::class.java)

    init {
        description = "Uploads terms and translations in a current project for a given exportLocale"
        group = "translations"
    }

    @Suppress("PlatformExtensionReceiverOfInline")
    @TaskAction
    fun uploadTranslations() {
        val apiParams = ApiParams(apiToken = apiToken.get(), projectId = projectId.get())
        val translationsLoader = TranslationsLoaderFactory.create(apiParams)
        translationsLoader.uploadTermsAndTranslations(resourcesPath.get(), exportLocale.get(),
                /* Small temp hack to avoid Groovy and Kotlin boolean incompatibility issue */
                overwriteOnExport.get().toBoolean(), syncTerms.get().toBoolean())
    }
}