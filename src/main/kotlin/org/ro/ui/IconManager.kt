package org.ro.ui

import org.ro.core.Utils

object IconManager {
    val PREFIX = "fas fa-"
    val DEFAULT_ICON = PREFIX + "cube"

    /* Recreate from configuration value*/
    val word2Icon = mapOf<String, String>(
            "Actions" to "ellipsis-v",
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
            "Burger" to "bars",
            "Save" to "file",
            "Undo" to "undo")

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

    fun findStyleFor(actionName: String): Set<String> {
        when {
            actionName == "delete" -> return setOf("text-danger")
            actionName == "undo" -> return setOf("text-warn")
            actionName == "save" -> return setOf("text-ok")
            else -> return setOf("text-normal")
        }
    }

/*   isis.reflector.facet.cssClassFa.patterns
new.*:fa-plus,
add.*:fa-plus-square,
create.*:fa-plus,
update.*:fa-edit,
delete.*:fa-trash,
save.*:fa-floppy-o,
change.*:fa-edit,
edit.*:fa-pencil-square-o,
maintain.*:fa-edit,
remove.*:fa-minus-square,
copy.*:fa-copy,
move.*:fa-exchange,
first.*:fa-star,
find.*:fa-search,
lookup.*:fa-search,
search.*:fa-search,
view.*:fa-search,
clear.*:fa-remove,
previous.*:fa-step-backward,
next.*:fa-step-forward,
list.*:fa-list,
all.*:fa-list,
download.*:fa-download,
upload.*:fa-upload,
export.*:fa-download,
switch.*:fa-exchange,
import.*:fa-upload,
execute.*:fa-bolt,
run.*:fa-bolt,
calculate.*:fa-calculator,
verify.*:fa-check-circle,
refresh.*:fa-refresh,
install.*:fa-wrench,
stop.*:fa-stop,
terminate.*:fa-stop,
cancel.*:fa-stop,
discard.*:fa-trash-o,
pause.*:fa-pause,
suspend.*:fa-pause,
resume.*:fa-play,
renew.*:fa-repeat,
reset.*:fa-repeat,
categorise.*:fa-folder-open-o,
assign.*:fa-hand-o-right,
approve.*:fa-thumbs-o-up,
decline.*:fa-thumbs-o-down
    */
}
