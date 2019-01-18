package com.example

import pl.treksoft.kvision.form.FormPanel
import pl.treksoft.kvision.form.FormPanel.Companion.formPanel
import pl.treksoft.kvision.form.check.CheckBox
import pl.treksoft.kvision.form.text.Text
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.html.Button.Companion.button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.StackPanel
import pl.treksoft.kvision.utils.ENTER_KEY
import pl.treksoft.kvision.utils.px

object EditPanel : StackPanel() {

    private var editingIndex: Int? = null

    private val formPanel: FormPanel<Address>
    private val mainPanel: SimplePanel

    init {
        padding = 10.px

        formPanel = formPanel {
            add(Address::firstName, Text(label = "First name:"))
            add(Address::lastName, Text(label = "Last name:"))
            add(Address::email, Text(TextInputType.EMAIL, label = "E-mail:"))
            add(Address::favourite, CheckBox(label = "Mark as favourite"))

            add(HPanel(spacing = 10) {
                button("Save", "fa-check", ButtonStyle.PRIMARY).onClick {
                    save()
                }
                button("Cancel", "fa-times", ButtonStyle.DEFAULT).onClick {
                    cancel()
                }
            })
            setEventListener {
                keydown = { e ->
                    if (e.keyCode == ENTER_KEY) {
                        save()
                    }
                }
            }
        }
        mainPanel = simplePanel {
            button("Add new address", "fa-plus", style = ButtonStyle.PRIMARY) {
                onClick {
                    add()
                }
            }
        }
    }

    private fun add() {
        editingIndex = null
        formPanel.clearData()
        activeChild = formPanel
        formPanel.getControl(Address::firstName)?.focus()
    }

    fun edit(index: Int) {
        editingIndex = index
        formPanel.setData(Model.addresses[index])
        activeChild = formPanel
        formPanel.getControl(Address::firstName)?.focus()
    }

    private fun save() {
        editingIndex?.let {
            Model.addresses.set(it, formPanel.getData())
        } ?: Model.addresses.add(formPanel.getData())
        editingIndex = null
        activeChild = mainPanel
        Model.storeAddresses()
    }

    private fun cancel() {
        editingIndex = null
        activeChild = mainPanel
    }

    fun delete(index: Int) {
        Model.addresses.removeAt(index)
        editingIndex?.let {
            if (index == it) {
                cancel()
            } else if (index < it) {
                editingIndex?.dec()
            }
        }
        Model.storeAddresses()
    }
}
