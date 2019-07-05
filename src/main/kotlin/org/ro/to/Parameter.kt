package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Parameter(val id: String,
                val num: Int = 0,
                val description: String,
                val name: String,
                @Optional val choices: List<Link> = emptyList(),
                @Optional @SerialName("default") val defaultChoice: Link? = null
) : TransferObject {

    fun hasChoices(): Boolean {
        return choices.isNotEmpty()
    }

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

    fun findIndexOfDefaultChoice(): Int {
        val dcTitle = defaultChoice!!.title
        var i = 0
        for (c in choices) {
            i++
            if (c.title == dcTitle) {
                return i
            }
        }
        return 0
    }

}
