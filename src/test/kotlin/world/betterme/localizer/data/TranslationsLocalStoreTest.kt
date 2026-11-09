package world.betterme.localizer.data

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.assertj.core.api.Assertions.assertThat
import world.betterme.localizer.data.constants.MetaDataContants
import java.io.File

private const val resFolderPath = "/app/src/main/res"

internal class TranslationsLocalStoreTest {

    private lateinit var localStore: TranslationsLocalStore

    @BeforeEach
    fun setUp() {
        localStore = TranslationsLocalStoreImpl()
    }

    @Test
    fun `get strings file path for default english locale`() {
        val locale = MetaDataContants.Values.Locales.VALUE_ENG

        val resultFilePath = localStore.getStringsFilePath(resFolderPath, locale, supportRegions = false)
        val expectedFilePath = "$resFolderPath/values/strings.xml"

        assertThat(resultFilePath).isEqualTo(expectedFilePath)
    }

    @Test
    fun `get strings file path for the regional locale when regional locales are supported`() {
        val locale = "zh-CH"

        val resultFilePath = localStore.getStringsFilePath(resFolderPath, locale, supportRegions = true)
        val expectedFilePath = "$resFolderPath/values-zh-rCH/strings.xml"

        assertThat(resultFilePath).isEqualTo(expectedFilePath)
    }

    @Test
    fun `get strings file path for the regional locale when regional locales are NOT supported`() {
        val locale = "zh-CH"

        val resultFilePath = localStore.getStringsFilePath(resFolderPath, locale, supportRegions = false)
        val expectedFilePath = "$resFolderPath/values-zh/strings.xml"

        assertThat(resultFilePath).isEqualTo(expectedFilePath)
    }

    @ParameterizedTest
    @ValueSource(strings = ["true", "false"])
    fun `get strings file for any custom non-regional locale`(supportRegionsStr: String) {
        val supportRegions = supportRegionsStr.toBoolean()

        val locale = "pt"

        val resultFilePath = localStore.getStringsFilePath(resFolderPath, locale, supportRegions = supportRegions)
        val expectedFilePath = "$resFolderPath/values-$locale/strings.xml"

        assertThat(resultFilePath).isEqualTo(expectedFilePath)
    }

    @ParameterizedTest
    @ValueSource(strings = ["true", "false"])
    fun `save norwegian bokmal strings also as no and nn copies`(supportRegionsStr: String, @TempDir tempDir: File) {
        val supportRegions = supportRegionsStr.toBoolean()

        val locale = "nb"
        val fileContents = "<resources><string name=\"hello\">Hei</string></resources>"

        localStore.saveToFile(tempDir.absolutePath, fileContents, locale, supportRegions = supportRegions)

        val bokmalBytes = File(tempDir, "values-nb/strings.xml").readBytes()
        assertThat(bokmalBytes).isEqualTo(fileContents.toByteArray(Charsets.UTF_8))
        assertThat(File(tempDir, "values-no/strings.xml").readBytes()).isEqualTo(bokmalBytes)
        assertThat(File(tempDir, "values-nn/strings.xml").readBytes()).isEqualTo(bokmalBytes)
    }

    @Test
    fun `save norwegian strings without bokmal and nynorsk copies`(@TempDir tempDir: File) {
        val locale = "no"
        val fileContents = "<resources><string name=\"hello\">Hei</string></resources>"

        localStore.saveToFile(tempDir.absolutePath, fileContents, locale, supportRegions = true)

        assertThat(File(tempDir, "values-no/strings.xml")).exists()
        assertThat(File(tempDir, "values-nb")).doesNotExist()
        assertThat(File(tempDir, "values-nn")).doesNotExist()
    }
}