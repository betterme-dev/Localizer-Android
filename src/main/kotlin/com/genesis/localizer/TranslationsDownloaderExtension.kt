package com.genesis.localizer

import org.gradle.api.Project
import org.gradle.api.provider.Property

class TranslationsDownloaderExtension {

    val apiToken: Property<String>
    val projectId: Property<String>
    val resourcesPath: Property<String>

    constructor(project: Project) {
        apiToken = project.objects.property(String::class.java)
        projectId = project.objects.property(String::class.java)
        resourcesPath = project.objects.property(String::class.java)
    }
}
