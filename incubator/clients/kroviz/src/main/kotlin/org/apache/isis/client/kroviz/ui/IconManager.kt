package org.apache.isis.client.kroviz.ui

import org.apache.isis.client.kroviz.utils.Utils

object IconManager {
    val PREFIX = "fas fa-"
    val DEFAULT_ICON = PREFIX + "bolt"

    const val DANGER = "text-danger"
    const val DISABLED = "text-disabled"
    const val NORMAL = "text-normal"
    const val OK = "text-ok"
    const val WARN = "text-warn"

    /* Recreate from configuration value*/
    val word2Icon = mapOf<String, String>(
            "All" to "asterisk",
            "Actions" to "ellipsis-v",
            "Blobs" to "cloud",
            "Burger" to "bars",
            "Close" to "times",
            "Configuration" to "wrench",
            "Connect" to "plug",
            "Create" to "plus",
            "Delete" to "trash",
            "Diagram" to "project-diagram",
            "Download" to "download",
            "Factory" to "industry",
            "Featured" to "keyboard",
            "Featured Types" to "keyboard",
            "Edit" to "pencil",
            "Error Handling" to "bug",
            "Error" to "bug",
            "Experimental" to "flask",
            "Export" to "file-export",
            "Facet" to "gem",
            "Find" to "search",
            "History" to "history",
            "Hsql" to "database",
            "Isis" to "ankh",
            "JEE/CDI" to "jedi",
            "List" to "list",
            "Log" to "history",
            "Manager" to "manager",
            "Me" to "user",
            "Notification" to "bell",
            "Objects" to "cubes",
            "OK" to "check",
            "Open" to "book",
            "Other" to "asterisk",
            "Primitives" to "hashtag",
            "Prototyping" to "object-group",
            "Run" to "rocket",
            "Save" to "file",
            "Security" to "lock",
            "Simple" to "cubes",
            "Switch" to "power-off",
            "Text" to "font",
            "Toast" to "bread-slice", //comment-alt-plus/minus/exclamation
            "Tooltips" to "comment-alt",
            "Temporals" to "clock",
            "Trees" to "tree",
            "Types" to "typewriter",
            "Undo" to "undo",
            "Visualize" to "eye",
            "Wikipedia" to "wikipedia-w")

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
            actionName == "delete" -> return setOf(DANGER)
            actionName == "undo" -> return setOf(WARN)
            actionName == "save" -> return setOf(OK)
            else -> return setOf(NORMAL)
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
