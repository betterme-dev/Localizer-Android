package world.betterme.localizer

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import world.betterme.localizer.core.TranslationsLoaderFactory
import world.betterme.localizer.data.models.ApiParams

open class TranslationsDownloaderTask : DefaultTask() {

    @Input
    val apiToken: Property<String> = project.objects.property(String::class.java)

    @Input
    val projectId: Property<String> = project.objects.property(String::class.java)

    @Input
    val resourcesPath: Property<String> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val supportRegions: Property<String>? = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val filters: ListProperty<String>? = project.objects.listProperty(String::class.java)

    @get:Input
    @get:Optional
    val tags: ListProperty<String>? = project.objects.listProperty(String::class.java)

    @Input
    val slackWebHook: Property<String> = project.objects.property(String::class.java)

    @get:Input
    @get:Optional
    val validateTranslations: Property<String> = project.objects.property(String::class.java)

    init {
        description = "Downloads translations for all locales supported in this project"
        group = "translations"
    }

    @TaskAction
    fun downloadTranslations() {
        val apiParams = ApiParams(
            apiToken = apiToken.get(),
            projectId = projectId.get(),
            slackWebHook = slackWebHook.get()
        )
        val translationsLoader = TranslationsLoaderFactory.create(apiParams)
        translationsLoader.downloadLocalizedStrings(
            resFolderPath = resourcesPath.get(),
            filters = filters?.get() ?: emptyList(),
            tags = tags?.get() ?: emptyList(),
            supportRegions = supportRegions?.get()?.toBoolean() ?: false,
            validateTranslations = validateTranslations.get().toBoolean(),
        )
    }
}
