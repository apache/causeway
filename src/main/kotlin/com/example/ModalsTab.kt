package com.example

import pl.treksoft.kvision.html.*
import pl.treksoft.kvision.html.Button.Companion.button
import pl.treksoft.kvision.i18n.I18n.tr
import pl.treksoft.kvision.modal.Alert
import pl.treksoft.kvision.modal.Confirm
import pl.treksoft.kvision.modal.Modal
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.VPanel.Companion.vPanel
import pl.treksoft.kvision.require
import pl.treksoft.kvision.utils.px

class ModalsTab : SimplePanel() {
    init {
        this.marginTop = 10.px
        this.minHeight = 400.px
        vPanel(spacing = 30) {
            button(tr("Alert dialog"), style = ButtonStyle.DANGER).onClick {
                Alert.show(
                    tr("Alert dialog"),
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce nec fringilla turpis, vel molestie dolor. Vestibulum ut ex eget orci porta gravida eu sit amet tortor."
                )
            }
            button(tr("Confirm dialog"), style = ButtonStyle.WARNING).onClick {
                Confirm.show(
                    tr("Confirm dialog"),
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce nec fringilla turpis, vel molestie dolor. Vestibulum ut ex eget orci porta gravida eu sit amet tortor.",
                    yesTitle = tr("Yes"),
                    noTitle = tr("No"),
                    cancelTitle = tr("Cancel"),
                    noCallback = {
                        Alert.show(tr("Result"), tr("You pressed NO button."))
                    }) {
                    Alert.show(tr("Result"), tr("You pressed YES button."))
                }
            }
            button(tr("Cancelable confirm dialog"), style = ButtonStyle.INFO).onClick {
                Confirm.show(
                    tr("Cancelable confirm dialog"),
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce nec fringilla turpis, vel molestie dolor. Vestibulum ut ex eget orci porta gravida eu sit amet tortor.",
                    align = Align.CENTER,
                    cancelVisible = true,
                    yesTitle = tr("Yes"),
                    noTitle = tr("No"),
                    cancelTitle = tr("Cancel"),
                    noCallback = {
                        Alert.show(tr("Result"), tr("You pressed NO button."))
                    }) {
                    Alert.show(tr("Result"), tr("You pressed YES button."))
                }
            }
            val modal = Modal(tr("Custom modal dialog"))
            modal.add(
                Tag(
                    TAG.H4,
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce nec fringilla turpis, vel molestie dolor. Vestibulum ut ex eget orci porta gravida eu sit amet tortor."
                )
            )
            modal.add(Image(require("img/dog.jpg")))
            modal.addButton(Button(tr("Close")).onClick {
                modal.hide()
            })
            button(tr("Custom modal dialog"), style = ButtonStyle.SUCCESS).onClick {
                modal.show()
            }
            button(tr("Alert dialog without animation"), style = ButtonStyle.PRIMARY).onClick {
                Alert.show(
                    tr("Alert dialog without animation"),
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce nec fringilla turpis, vel molestie dolor. Vestibulum ut ex eget orci porta gravida eu sit amet tortor.",
                    animation = false
                )
            }
        }
    }
}
