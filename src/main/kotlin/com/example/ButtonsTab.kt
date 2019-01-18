package com.example

import pl.treksoft.kvision.form.check.CheckBox.Companion.checkBox
import pl.treksoft.kvision.form.check.CheckBoxStyle
import pl.treksoft.kvision.form.check.Radio.Companion.radio
import pl.treksoft.kvision.form.check.RadioStyle
import pl.treksoft.kvision.html.Button.Companion.button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.html.Label.Companion.label
import pl.treksoft.kvision.i18n.I18n.tr
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.HPanel.Companion.hPanel
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.VPanel.Companion.vPanel
import pl.treksoft.kvision.toolbar.ButtonGroup.Companion.buttonGroup
import pl.treksoft.kvision.toolbar.Toolbar.Companion.toolbar
import pl.treksoft.kvision.utils.px

class ButtonsTab : SimplePanel() {
    init {
        this.marginTop = 10.px
        hPanel(wrap = FlexWrap.WRAP, spacing = 100) {
            vPanel(spacing = 7) {
                button(tr("Default button"), style = ButtonStyle.DEFAULT) { width = 200.px }
                button(tr("Primary button"), style = ButtonStyle.PRIMARY) { width = 200.px }
                button(tr("Success button"), style = ButtonStyle.SUCCESS) { width = 200.px }
                button(tr("Info button"), style = ButtonStyle.INFO) { width = 200.px }
                button(tr("Warning button"), style = ButtonStyle.WARNING) { width = 200.px }
                button(tr("Danger button"), style = ButtonStyle.DANGER) { width = 200.px }
                button(tr("Link button"), style = ButtonStyle.LINK) { width = 200.px }
            }
            vPanel {
                checkBox(true, label = tr("Default checkbox")) { style = CheckBoxStyle.DEFAULT }
                checkBox(true, label = tr("Primary checkbox")) { style = CheckBoxStyle.PRIMARY }
                checkBox(true, label = tr("Success checkbox")) { style = CheckBoxStyle.SUCCESS }
                checkBox(true, label = tr("Info checkbox")) { style = CheckBoxStyle.INFO }
                checkBox(true, label = tr("Warning checkbox")) { style = CheckBoxStyle.WARNING }
                checkBox(true, label = tr("Danger checkbox")) { style = CheckBoxStyle.DANGER }
                checkBox(true, label = tr("Circled checkbox")) { circled = true }
            }
            vPanel {
                radio(name = "radio", label = tr("Default radiobutton")) { style = RadioStyle.DEFAULT }
                radio(name = "radio", label = tr("Primary radiobutton")) { style = RadioStyle.PRIMARY }
                radio(name = "radio", label = tr("Success radiobutton")) { style = RadioStyle.SUCCESS }
                radio(name = "radio", label = tr("Info radiobutton")) { style = RadioStyle.INFO }
                radio(name = "radio", label = tr("Warning radiobutton")) { style = RadioStyle.WARNING }
                radio(name = "radio", label = tr("Danger radiobutton")) { style = RadioStyle.DANGER }
                radio(name = "radio", label = tr("Squared radiobutton")) { squared = true }
            }
        }
        toolbar {
            buttonGroup {
                button("<<")
            }
            buttonGroup {
                button("1", disabled = true)
                button("2")
                button("3")
            }
            buttonGroup {
                label("...")
            }
            buttonGroup {
                button("10")
            }
            buttonGroup {
                button(">>")
            }
        }
    }
}
