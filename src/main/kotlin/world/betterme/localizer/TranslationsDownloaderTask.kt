package world.betterme.localizer

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import world.betterme.localizer.core.TranslationsLoaderFactory
import world.betterme.localizer.data.models.ApiParams

open class TranslationsDownloaderTask : DefaultTask() {

    @Input
    val apiToken: Property<String> = project.objects.property(String::class.java)

    @Input
    val projectId: Property<String> = project.objects.property(String::class.java)

    @Input
    val resourcesPath: Property<String> = project.objects.property(String::class.java)

    @Input
    @Optional
    val supportRegions: Property<String> = project.objects.property(String::class.java)

    @Input
    @Optional
    val filters: ListProperty<String> = project.objects.listProperty(String::class.java)

    @Input
    @Optional
    val tags: ListProperty<String> = project.objects.listProperty(String::class.java)

    @Input
    @Optional
    val full: Property<Boolean> = project.objects.property(Boolean::class.java).convention(false)

    @Input
    @Optional
    val keys: ListProperty<String> = project.objects.listProperty(String::class.java)

    init {
        description = "Downloads translations for all locales supported in this project"
        group = "translations"
    }

    @Option(option = "keys", description = "List of keys to download (e.g., --keys='key1,key2'")
    fun setKeys(keys: String) {
        val keyList = parseKeys(keys)
        this.keys.set(keyList)
    }

    @Option(option = "full", description = "Full mode – download all keys for all locales")
    fun setFull(full: Boolean) {
        this.full.set(full)
    }

    @TaskAction
    fun downloadTranslations() {
        val apiParams = ApiParams(apiToken = apiToken.get(), projectId = projectId.get())
        val translationsLoader = TranslationsLoaderFactory.create(apiParams)
        val fullMode = full.get()
        val keysFromOption = keys.getOrElse(emptyList())
        translationsLoader.downloadLocalizedStrings(
            resFolderPath = resourcesPath.get(),
            filters = filters.getOrElse(emptyList()),
            tags = tags.getOrElse(emptyList()),
            supportRegions = supportRegions.getOrElse("false").toBoolean(),
            fullMode = fullMode,
            keys = keysFromOption
        )
    }

    private fun parseKeys(keysString: String): List<String> {
        return keysString
            .removeSurrounding("'")
            .trim()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}