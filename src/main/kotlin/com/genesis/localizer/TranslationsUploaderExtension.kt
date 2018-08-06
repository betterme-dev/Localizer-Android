package com.genesis.localizer

import org.gradle.api.Project
import org.gradle.api.provider.Property

class TranslationsUploaderExtension {

    val apiToken: Property<String>
    val projectId: Property<String>
    val resourcesPath: Property<String>
    val locale: Property<String>
    val overwrite: Property<Boolean>

    constructor(project: Project) {
        apiToken = project.objects.property(String::class.java)
        projectId = project.objects.property(String::class.java)
        resourcesPath = project.objects.property(String::class.java)
        locale = project.objects.property(String::class.java)
        overwrite = project.objects.property(Boolean::class.java)
    }
}
