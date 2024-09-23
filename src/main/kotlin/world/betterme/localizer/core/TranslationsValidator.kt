package world.betterme.localizer.core

import java.util.Locale

typealias Resources = Map<String, String>

class TranslationsValidator(
    private val xmlParser: XmlParser,
    private val notifier: SlackNotifier
) {
    private val translations = mutableMapOf<Locale, Resources>()
    private val validationErrors = mutableListOf<String>()

    fun appendTranslationContent(locale: String, translationContent: String) {
        val parsedTranslationContent = xmlParser.parseXmlStrings(translationContent)
        translations[Locale.forLanguageTag(locale)] = parsedTranslationContent
    }

    fun validateAll() {
        println("Starting validation placeholders")
        val referenceLocale = Locale.ENGLISH
        val referenceTranslation = translations[referenceLocale]
            ?: throw IllegalArgumentException("Reference locale $referenceLocale is missing.")

        translations.forEach { (locale, translationStrings) ->
            translationStrings.forEach { (key, localizedText) ->
                val referenceText = referenceTranslation[key]
                if (referenceText != null) {
                    validatePlaceholders(referenceText, localizedText, locale, key)
                }
            }
        }

        if (validationErrors.isNotEmpty()) {
            val report = validationErrors.joinToString(separator = "\n")
            notifier.sendSlackMessage("Translation validation issues:\n$report")
        } else {
            println("All translations validated successfully!")
        }
    }

    private fun validatePlaceholders(
        referenceText: String,
        localizedText: String,
        locale: Locale,
        key: String
    ) {
        val referencePlaceholders = extractPlaceholdersFromText(referenceText)
        val localizedPlaceholders = extractPlaceholdersFromText(localizedText)

        // Validate placeholder count
        if (referencePlaceholders.size != localizedPlaceholders.size) {
            validationErrors.add(
                "Placeholder count mismatch for key '$key' in locale '$locale'. " +
                        "Expected ${referencePlaceholders.size}, found ${localizedPlaceholders.size}."
            )
        }

        // Validate placeholder types
        referencePlaceholders.forEachIndexed { index, referencePlaceholder ->
            val localizedPlaceholder = localizedPlaceholders.getOrNull(index)
            if (localizedPlaceholder == null || referencePlaceholder != localizedPlaceholder) {
                validationErrors.add(
                    "Placeholder type mismatch for key '$key' in locale '$locale' at position $index. " +
                            "Expected $referencePlaceholder, found $localizedPlaceholder."
                )
            }
        }
    }

    /**
     * Extracts placeholders like %1$s, %1$d from the text.
     */
    private fun extractPlaceholdersFromText(text: String): List<String> {
        val placeholderRegex = """%\d+\$[sd]""".toRegex()
        return placeholderRegex.findAll(text).map { it.value }.toList()
    }
}





