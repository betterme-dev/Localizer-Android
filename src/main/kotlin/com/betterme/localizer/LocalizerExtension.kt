package com.betterme.localizer

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

open class LocalizerExtension(project: Project) {
    val apiToken: Property<String> = project.objects.property(String::class.java)
    val projectId: Property<String> = project.objects.property(String::class.java)
    val resourcesPath: Property<String> = project.objects.property(String::class.java)
    val exportLocale: Property<String> = project.objects.property(String::class.java)
    val overwriteOnExport: Property<String> = project.objects.property(String::class.java)
    val syncTerms: Property<String> = project.objects.property(String::class.java)
    val filters: ListProperty<String> = project.objects.listProperty(String::class.java)
    val tags: ListProperty<String> = project.objects.listProperty(String::class.java)
    val supportRegions: Property<String> = project.objects.property(String::class.java)
}