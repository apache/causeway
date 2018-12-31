package org.ro.to

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int

class Parameter(jsonObj: JsonObject? = null) : BaseTO() {
    private var num: Int = 0
    internal var id = ""
    private var description = ""
    private var name = ""
    internal var choiceList: MutableList<Link> = mutableListOf()
    private var defaultChoiceObject: Link? = null

    init {
        if (jsonObj != null) {
            num = jsonObj["num"].int
            id = jsonObj["id"].toString()
            description = jsonObj["description"].toString()
            name = jsonObj["name"].toString()
            val choices = jsonObj["Choices"].jsonArray
            for (c in choices) {
                choiceList.add(Link(c as JsonObject))
            }
            val defaultChoice = jsonObj["defaultChoice"].jsonObject
            defaultChoiceObject = Link(defaultChoice)
        }
    }


    fun getDefaultChoice(): Link? {
        return defaultChoiceObject
    }

    fun hasChoices(): Boolean {
        return (choiceList.size > 0)
    }

    fun getChoiceListKeys(): MutableList<String> {
        val result: MutableList<String> = mutableListOf()
        for (c in choiceList) {
            result.add(c.title)
        }
        return result
    }

    fun getHrefByTitle(title: String): String? {
        for (l in choiceList) {
            if (l.title == title) {
                return l.href
            }
        }
        return null
    }

    fun findIndexOfDefaultChoice(): Int {
        val dcTitle = defaultChoiceObject!!.title
        var i = 0
        for (c in choiceList) {
            i++
            if (c.title == dcTitle) {
                return i
            }
        }
        return 0
    }

}