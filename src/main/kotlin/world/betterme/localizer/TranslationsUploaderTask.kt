package world.betterme.localizer

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.provider.Property
import world.betterme.localizer.data.models.ApiParams
import world.betterme.localizer.core.TranslationsLoaderFactory

open class TranslationsUploaderTask : DefaultTask() {

    @Input
    val apiToken: Property<String> = project.objects.property(String::class.java)
    @Input
    val projectId: Property<String> = project.objects.property(String::class.java)
    @Input
    val resourcesPath: Property<String> = project.objects.property(String::class.java)
    @Input
    val exportLocale: Property<String> = project.objects.property(String::class.java)
    @Input
    val overwriteOnExport: Property<String> = project.objects.property(String::class.java)
    @Input
    val syncTerms: Property<String> = project.objects.property(String::class.java)
    @get:Input
    @get:Optional
    val supportRegions: Property<String>? = project.objects.property(String::class.java)

    init {
        description = "Uploads terms and translations in a current project for a given exportLocale"
        group = "translations"
    }

    @Suppress("unused")
    @TaskAction
    fun uploadTranslations() {
        val apiParams = ApiParams(apiToken = apiToken.get(), projectId = projectId.get())
        val translationsLoader = TranslationsLoaderFactory.create(apiParams)
        translationsLoader.uploadTermsAndTranslations(
            resFolderPath = resourcesPath.get(),
            locale = exportLocale.get(),
            /* Small temp hack to avoid Groovy and Kotlin boolean incompatibility issue */
            overwrite = overwriteOnExport.get().toBoolean(),
            syncTerms = syncTerms.get().toBoolean(),
            supportRegions = supportRegions?.get()?.toBoolean() ?: false
        )
    }
}