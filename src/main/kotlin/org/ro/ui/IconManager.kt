package org.ro.ui

import org.ro.core.Utils

object IconManager {
    val PREFIX = "fas fa-"
    val DEFAULT_ICON = PREFIX + "cube"

    /* Recreate from configuration value*/
    val word2Icon = mapOf<String, String>(
            "Actions" to "ellipsis-h",
            "Create" to "plus",
            "Edit" to "pencil",
            "Delete" to "trash",
            "Find" to "search",
            "List" to "list",
            "Download" to "download",
            "Open" to "book",
            "Run" to "rocket",
            "Simple" to "cubes",
            "Configuration" to "wrench",
            "Manager" to "manager",
            "Switch" to "power-off",
            "Hsql" to "database",
            "Prototyping" to "object-group",
            "Objects" to "cubes",
            "Log" to "history",
            "Connect" to "plug",
            "Close" to "times",
            "Burger" to "bars")

    fun find(query: String): String {
        val actionTitle = Utils.deCamel(query)
        val mixedCaseList = actionTitle.split(" ")
        for (w in mixedCaseList) {
            val hit = word2Icon.get(w)
            if (hit != null) {
                return PREFIX + hit
            }
        }
        return DEFAULT_ICON
    }
}
