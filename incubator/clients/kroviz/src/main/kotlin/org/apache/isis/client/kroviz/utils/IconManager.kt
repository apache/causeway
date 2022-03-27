/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.client.kroviz.utils

object IconManager {
    private const val PREFIX = "fas fa-"   //TODO far fa- ???
    const val DEFAULT_ICON = PREFIX + "play"

    const val DANGER = "text-danger"
    const val DISABLED = "text-disabled"
    private const val NORMAL = "text-normal"
    const val OK = "text-ok"
    const val WARN = "text-warn"

    /* Merge with configuration values*/
    private val word2Icon = mapOf(
        "About" to "info-circle",
        "Actions" to "ellipsis-v",
        "All" to "asterisk",
        "Basic" to "minus-circle",
        "Blobs" to "cloud",
        "Burger" to "bars",
        "Chart" to "magic",
        "Close" to "times",
        "Collections" to "list",
        "Compare" to "balance-scale",
        "Configuration" to "wrench",
        "Connect" to "plug",
        "Console" to "terminal",
        "Create" to "plus",
        "Css" to "css3",
        "Dates" to "calendar",
        "Debug" to "bug",
        "Delete" to "trash",
        "Demo" to "eye",
        "Details" to "info-circle",
        "Described" to "tag",
        "Diagram" to "project-diagram",
        "Download" to "download",
        "Factory" to "industry",
        "Featured" to "keyboard",
        "Featured Types" to "keyboard",
        "Edit" to "pencil",
        "Error Handling" to "bug",
        "Error" to "bug",
        "Event" to "bolt",
        "Experimental" to "flask",
        "Export" to "file-export",
        "Facet" to "gem",
        "Find" to "search",
        "Hidden" to "ban",
        "Hierarchy" to "sitemap",
        "History" to "history",
        "Hsql" to "database",
        "Import" to "file-import",
        "Isis" to "ankh",
        "JEE/CDI" to "jedi",
        "List" to "list",
        "Location" to "map-marker",
        "Log" to "history",
        "Logout" to "user-times",
        "Manager" to "manager",
        "Map" to "map",
        "Me" to "user",
        "Message" to "envelope",
        "More" to "plus-circle",
        "Named" to "book",
        "Notification" to "bell",
        "Notifications" to "bell",
        "Object" to "cube",
        "Objects" to "cubes",
        "OK" to "check",
        "Open" to "book",
        "Other" to "asterisk",
        "Pin" to "map-pin",
        "Primitives" to "hashtag",
        "Properties" to "indent",
        "Prototyping" to "object-group",
        "Queen" to "chess-queen",
        "Replay" to "fast-forward",
        "Run" to "rocket",
        "Save" to "file",
        "Security" to "lock",
        "Simple" to "cubes",
        "Strings" to "font",
        "Switch" to "power-off",
        "Target" to "bullseye",
        "Tab" to "folder",
        "Terminal" to "terminal",
        "Text" to "font",
        "Times" to "clock",
        "Toast" to "bread-slice", //comment-alt-plus/minus/exclamation
        "Toolbar" to "step-backward",
        "Tooltips" to "comment-alt",
        "Temporal" to "clock",
        "Tenancy" to "lock",
        "Trees" to "tree",
        "Types" to "typewriter",
        "Undo" to "undo",
        "Unknown" to "question",
        "Visualize" to "eye",
        "Wikipedia" to "wikipedia-w",
        "World" to "plane",
        "Xml" to "code"
    )

    @OptIn(ExperimentalStdlibApi::class)
    fun find(query: String): String {
        if (query.startsWith("fa")) return query
        val actionTitle = StringUtils.deCamel(query)
        val mixedCaseList = actionTitle.split(" ")
        for (w in mixedCaseList) {
            val hit = word2Icon[w]
            if (hit != null) {
                return PREFIX + hit
            }
        }
        return DEFAULT_ICON
    }

    fun findStyleFor(actionName: String): String {
        return when (actionName) {
            "delete" -> DANGER
            "undo" -> WARN
            "save" -> OK
            else -> NORMAL
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
