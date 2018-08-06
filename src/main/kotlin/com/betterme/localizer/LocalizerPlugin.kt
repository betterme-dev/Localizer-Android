package com.betterme.localizer

import org.gradle.api.Plugin
import org.gradle.api.Project

open class LocalizerPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.create("downloadTranslations", TranslationsDownloaderTask::class.java)
        project.tasks.create("uploadTranslations", TranslationsUploaderTask::class.java)
    }

}