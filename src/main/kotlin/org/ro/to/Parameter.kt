package org.ro.to

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Parameter(val id: String,
                val num: Int = 0,
                val description: String,
                val name: String,
                val choices: List<Link> = emptyList(),
                @SerialName("default") val defaultChoice: Link? = null
) : TransferObject {

    fun getChoiceListKeys(): MutableList<String> {
        val result: MutableList<String> = mutableListOf()
        for (c in choices) {
            result.add(c.title)
        }
        return result
    }

    fun getHrefByTitle(title: String): String? {
        for (l in choices) {
            if (l.title == title) {
                return l.href
            }
        }
        return null
    }

}
