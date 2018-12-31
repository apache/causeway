package org.ro

import pl.treksoft.kvision.hmr.ApplicationBase
import pl.treksoft.kvision.html.TAG
import pl.treksoft.kvision.html.Tag.Companion.tag
import pl.treksoft.kvision.i18n.DefaultI18nManager
import pl.treksoft.kvision.i18n.I18n
import pl.treksoft.kvision.i18n.I18n.tr
import pl.treksoft.kvision.panel.FlexDir
import pl.treksoft.kvision.panel.FlexJustify
import pl.treksoft.kvision.panel.FlexPanel.Companion.flexPanel
import pl.treksoft.kvision.panel.Root
import pl.treksoft.kvision.require
import pl.treksoft.kvision.utils.px

object App : ApplicationBase {

    private lateinit var root: Root

    override fun start(state: Map<String, Any>) {

        I18n.manager =
                DefaultI18nManager(
                    mapOf(
                        "en" to require("./messages-en.json"),
                        "pl" to require("./messages-pl.json"),
                        "de" to require("./messages-de.json"),
                        "es" to require("./messages-pl.json"),
                        "fr" to require("./messages-pl.json"),
                        "ru" to require("./messages-pl.json"),
                        "ja" to require("./messages-pl.json"),
                        "ko" to require("./messages-pl.json")
                    )
                )

        root = Root("kroViz") {
            flexPanel(FlexDir.ROW, justify = FlexJustify.CENTER) {
                tag(TAG.DIV, tr("Hello world!"), classes = setOf("kroViz")) {
                    marginTop = 50.px
                    fontSize = 50.px
                }
            }
        }
    }

    override fun dispose(): Map<String, Any> {
        root.dispose()
        return mapOf()
    }

    val css = require("./css/helloworld.css")
}
