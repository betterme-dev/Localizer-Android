package world.betterme.localizer.core

import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilderFactory


class XmlParser {
    fun parseXmlStrings(content: String): Resources {
        val translations = mutableMapOf<String, String>()

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document: Document = builder.parse(content.byteInputStream())
        document.documentElement.normalize()

        val nodeList = document.getElementsByTagName("string")

        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            val name = node.attributes.getNamedItem("name").nodeValue
            val textContent = node.textContent

            translations[name] = textContent
        }
        return translations
    }

}

