package org.ro.to

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Parameter(val id: String,
                val num: Int = 0,
                val description: String,
                val name: String,
                // choices either are a list of:
                // Links -> ACTIONS_RUN_FIXTURE_SCRIPT
                // Strings -> ACTIONS_DOWNLOAD_LAYOUTS
                val choices: List<Value> = emptyList(),
                @SerialName("default") val defaultChoice: Value? = null
) : TransferObject {

    fun getChoiceListKeys(): MutableList<String> {
        val result: MutableList<String> = mutableListOf()
        for (c in choices) {
            if (c.content is Link) {
                result.add((c.content).title)
            }
        }
        return result
    }

    fun getHrefByTitle(title: String): String? {
        for (c in choices) {
            if (c.content is Link) {
                val l = c.content
                if (l.title == title) {
                    return l.href
                }
            }
        }
        return null
    }

}
