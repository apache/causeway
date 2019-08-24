package org.ro.view

import org.ro.core.Utils

object IconManager {
    /*  Recreate */
    val word2Icon = mapOf<String, String>(
            "Create" to "fa-plus",
            "Find" to "fa-search",
            "List" to "fa-list",
            "Download" to "fa-download",
            "Open" to "fa-book",
            "Run" to "fa-rocket",
            "Simple" to "fa-cubes",
            "Configuration" to "fa-cog",
            "Manager" to "fa-manager",
            "Switch" to "fa-power-off",
            "Hsql" to "fa-database",
            "Prototyping" to "fa-object-group",
            "Objects" to "fa-cubes",
            "Log" to "fa-history",
            "Connect" to "fa-plug",
            "Close" to "fa-times")

    fun find(query: String): String {
        val actionTitle = Utils.deCamel(query)
        val mixedCaseList = actionTitle.split(" ")
        for (w in mixedCaseList) {
            val hit = word2Icon.get(w)
            if (hit != null) {
                return hit
            }
        }
        return "fa-magic"
    }
}
