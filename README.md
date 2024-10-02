# Localizer-Android
Gradle plugin which simplifies Android string resources &amp; translations synchronization with POEditor (API v2).

With the help of this plugin you can either download strings.xml for all existing locales in your project or
export your own terms and translations to POEdtior in order to keep both sources - local and remote - up-to-date.

## Configuration

### 1. Add plugin's classpath to your app-level `build.gradle`.

```groovy
buildscript {
    repositories {
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        final localizerVer = '1.0.6'

        classpath "world.betterme.localizer:localizer-android:$localizerVer"
    }
}
```

### 2. Apply plugin in your module-level `build.gradle`.

```groovy
apply plugin: 'world.betterme.localizer'
```

### 3. Set up POEditor configs and translations upload/download parameters.

```groovy
localizer {
    apiToken = getPoEditorApiToken()
    projectId = getPoEditorProjectId()

    resourcesPath = "${rootDir}/app/src/main/res".toString()

    overwriteOnExport = "true"
    syncTerms = "true"
    exportLocale = "en"
    supportRegions = "true" // optional, false by default

    filters = ["translated"]
    tags = ["android"]
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

`syncTerms`: allows or denies (if set to false) two-sided strings synchronization with POEditor API, and automatically removes strings remotely deleted locally.

`filters`: Filter strings by 'translated', 'untranslated', 'fuzzy', 'not_fuzzy', 'automatic', 'not_automatic', 'proofread', 'not_proofread' (only available when Proofreading is set to "Yes" in Project Settings).

`tags`: Filter strings by tags.

`supportRegions`: enables or disables (if set to false or not defined) regional locales support (which are usually defined in the following pattern in POEditor zh_CH). By default, or if set to false externally, the regional postfix (i.e. `_CH` in `zh_CH` will be omitted).

Finally, this plugin is ready to use!

## Usage

In order to trigger the tasks consider selecting `downloadTranslations` and `uploadTranslations` tasks in
`translations` group Gradle tasks folder associated to the module where you apply this plugin.

Will download all the translations
```
 ./gradlew downloadTranslations --full
```

Will download only the translations with specified keys
```
./gradlew downloadTranslations --keys='key1, key2, key3'
```

Will update only the translations that are present in the main strings file
```
./gradlew downloadTranslations
```

Improvement ideas and pull requests are highly appreciated.