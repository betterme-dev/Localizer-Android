package com.betterme.localizer

import org.gradle.api.Project
import org.gradle.api.provider.Property

open class LocalizerExtension {

    val apiToken: Property<String>
    val projectId: Property<String>
    val resourcesPath: Property<String>
    val exportLocale: Property<String>
    val overwriteOnExport: Property<String>
    val syncTerms: Property<String>

    constructor(project: Project) {
        apiToken = project.objects.property(String::class.java)
        projectId = project.objects.property(String::class.java)
        resourcesPath = project.objects.property(String::class.java)
        exportLocale = project.objects.property(String::class.java)
        overwriteOnExport = project.objects.property(String::class.java)
        syncTerms = project.objects.property(String::class.java)
    }
}
