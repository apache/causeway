package com.example

import pl.treksoft.kvision.core.Border
import pl.treksoft.kvision.core.BorderStyle
import pl.treksoft.kvision.core.Col
import pl.treksoft.kvision.form.select.Select
import pl.treksoft.kvision.form.select.Select.Companion.select
import pl.treksoft.kvision.hmr.ApplicationBase
import pl.treksoft.kvision.i18n.DefaultI18nManager
import pl.treksoft.kvision.i18n.I18n
import pl.treksoft.kvision.i18n.I18n.tr
import pl.treksoft.kvision.panel.Root
import pl.treksoft.kvision.panel.TabPanel.Companion.tabPanel
import pl.treksoft.kvision.require
import pl.treksoft.kvision.utils.auto
import pl.treksoft.kvision.utils.perc
import pl.treksoft.kvision.utils.px

object ShowcaseOriginal : ApplicationBase {

    private lateinit var root: Root

    override fun start(state: Map<String, Any>) {

        I18n.manager =
                DefaultI18nManager(mapOf("pl" to require("./messages-pl.json"), "en" to require("./messages-en.json")))

        root = Root("showcase") {
            tabPanel {
                width = 80.perc
                margin = 20.px
                marginLeft = auto
                marginRight = auto
                padding = 20.px
                border = Border(2.px, BorderStyle.SOLID, Col.SILVER)
                addTab(tr("Basic formatting"), BasicTab(), "fa-bars", route = "/basic")
                addTab(tr("Forms"), FormTab(), "fa-edit", route = "/forms")
                addTab(tr("Buttons"), ButtonsTab(), "fa-check-square-o", route = "/buttons")
                addTab(tr("Dropdowns & Menus"), DropDownTab(), "fa-arrow-down", route = "/dropdowns")
                addTab(tr("Containers"), ContainersTab(), "fa-database", route = "/containers")
                addTab(tr("Layouts"), LayoutsTab(), "fa-th-list", route = "/layouts")
                addTab(tr("Modals"), ModalsTab(), "fa-window-maximize", route = "/modals")
                addTab(tr("Data binding"), DataTab(), "fa-retweet", route = "/data")
                addTab(tr("Windows"), WindowsTab(), "fa-window-restore", route = "/windows")
                addTab(tr("Drag & Drop"), DragDropTab(), "fa-arrows-alt", route = "/dragdrop")
            }
            select(listOf("en" to tr("English"), "pl" to tr("Polish")), I18n.language) {
                width = 300.px
                marginLeft = auto
                marginRight = auto
                setEventListener<Select> {
                    change = {
                        I18n.language = self.value ?: "en"
                    }
                }
            }
        }
    }

    override fun dispose(): Map<String, Any> {
        root.dispose()
        return mapOf()
    }

    val css = require("css/kroviz.css")
}