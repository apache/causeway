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
            when (c.content) {
                is Link -> {
                    result.add((c.content as Link).title)
                }
                is String -> {
                    result.add(c.content as String)
                }
            }
        }
        return result
    }

    fun getHrefByTitle(title: String): String? {
        for (c in choices) {
            val l = c.content
            when (l) {
                is Link -> {
                    if (l.title == title) {
                        return l.href
                    }
                }
                is String -> {
                    return l
                }
            }
        }
        return null
    }

}
