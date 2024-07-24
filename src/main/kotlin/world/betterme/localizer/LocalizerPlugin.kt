package world.betterme.localizer

import org.gradle.api.Plugin
import org.gradle.api.Project

open class LocalizerPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension =
            project.extensions.create("localizer", LocalizerExtension::class.java, project)

        val supportRegions = extension.supportRegions

        project.tasks.create("uploadTranslations", TranslationsUploaderTask::class.java) {
            it.apiToken.set(extension.apiToken)
            it.projectId.set(extension.projectId)
            it.resourcesPath.set(extension.resourcesPath).toString()
            it.exportLocale.set(extension.exportLocale)
            it.overwriteOnExport.set(extension.overwriteOnExport)
            it.syncTerms.set(extension.syncTerms)
            it.supportRegions?.set(supportRegions)
        }

        project.tasks.create("downloadTranslations", TranslationsDownloaderTask::class.java) {
            it.apiToken.set(extension.apiToken)
            it.projectId.set(extension.projectId)
            it.resourcesPath.set(extension.resourcesPath)
            it.filters?.addAll(extension.filters)
            it.tags?.addAll(extension.tags)
            it.supportRegions?.set(supportRegions)
        }
    }
}


