# Localizer-Android
Gradle plugin which simplifies Android string resources &amp; translations synchronization with POEditor (API v2).

With the help of this plugin you can either download strings.xml for all existing locales in your project or 
export your own terms and translations to POEdtior in order to keep both sources - local and remote - up-to-date.

## Configure in project

### 1. Add plugin's classpath to your app-level `build.gradle`.

```groovy
buildscript {
    repositories {
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        final localizerVer = '0.0.9'

        classpath "gradle.plugin.com.betterme.localizer:localizer-android:$localizerVer"
    }
}
```

### 2. Apply plugin in your module-level `build.gradle`.

```groovy
apply plugin: 'com.betterme.localizer'
```

### 3. Set up POEditor configs and translations upload/download parameters.

```groovy
localizer {
    apiToken = getPoEditorApiToken()
    projectId = getPoEditorProjectId()

    resourcesPath = "${rootDir}/app/src/main/res".toString()

    overwriteOnExport = "true"
    exportLocale = "en"
}
```

`apiToken`: api token for your POEditor project.
`projectId`: project ID of your project in POEditor.
`resourcesPath`: path to your values folder in current project.
It's important to convert `resourcesPath` to Groovy String type with `toString()` method for compatibility with 
Java type system.
`overwriteOnExport`: allows or denies (if set to false) strings overwriting on their upload.
`exportLocale`: desired locale for local strings resources to be uploaded to POEditor. Usually, should be
set to your default language ("en"), as it's the most common case when you need to keep your English 
(as primary ones) terms up-to-date in POEditor as well. But other languages may also be the case if 
there are some typos in POEditor translations or special symbols need to be used in remote strings.xml, 
that need to be applied to remote versions of your strings.

Finally, this plugin is ready to use! Enjoy!
Improvement ideas and pull requests are highly appreciated.
