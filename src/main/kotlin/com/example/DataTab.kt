package com.example

import com.lightningkite.kotlin.observable.list.observableListOf
import pl.treksoft.kvision.core.FontWeight
import pl.treksoft.kvision.data.BaseDataComponent
import pl.treksoft.kvision.data.DataContainer
import pl.treksoft.kvision.form.check.CheckBox
import pl.treksoft.kvision.form.check.CheckBoxStyle
import pl.treksoft.kvision.form.text.TextInput
import pl.treksoft.kvision.form.text.TextInput.Companion.textInput
import pl.treksoft.kvision.form.text.TextInputType
import pl.treksoft.kvision.html.Button.Companion.button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.i18n.I18n.tr
import pl.treksoft.kvision.i18n.I18n.trans
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.VPanel.Companion.vPanel
import pl.treksoft.kvision.utils.px

class DataTab : SimplePanel() {
    init {
        this.marginTop = 10.px
        this.minHeight = 400.px

        val panel = vPanel(spacing = 5)

        class DataModel(checked: Boolean, text: String) : BaseDataComponent() {
            var checked: Boolean by obs(checked)
            var text: String by obs(text)
        }

        val list = observableListOf(
            DataModel(false, tr("January")),
            DataModel(false, tr("February")),
            DataModel(false, tr("March")),
            DataModel(false, tr("April")),
            DataModel(false, tr("May")),
            DataModel(false, tr("June")),
            DataModel(false, tr("July")),
            DataModel(false, tr("August")),
            DataModel(false, tr("September")),
            DataModel(false, tr("October")),
            DataModel(false, tr("November"))
        )

        var searchFilter: String? = null

        val dataContainer = DataContainer(list, { _, model ->
            CheckBox(
                value = model.checked,
                label = model.text
            ).apply {
                flabel.fontWeight = if (model.checked) FontWeight.BOLD else null
                style = CheckBoxStyle.PRIMARY
                onClick {
                    model.checked = this.value
                }
            }
        }, filter = { _, model ->
            searchFilter?.let {
                trans(model.text).contains(it, ignoreCase = true)
            } ?: true
        }, container = HPanel(spacing = 10, wrap = FlexWrap.WRAP))
        panel.add(dataContainer)

        panel.add(HPanel(spacing = 10, wrap = FlexWrap.WRAP) {
            textInput(type = TextInputType.SEARCH) {
                placeholder = tr("Search ...")
                setEventListener<TextInput> {
                    input = {
                        searchFilter = self.value
                        dataContainer.update()
                    }
                }
            }
            button(tr("Add December"), style = ButtonStyle.SUCCESS).onClick {
                list.add(DataModel(true, tr("December")))
            }
            button(tr("Check all"), style = ButtonStyle.INFO).onClick {
                list.forEach { it.checked = true }
            }
            button(tr("Uncheck all"), style = ButtonStyle.INFO).onClick {
                list.forEach { it.checked = false }
            }
            button(tr("Reverse list"), style = ButtonStyle.DANGER).onClick {
                list.reverse()
            }
            button(tr("Remove checked"), style = ButtonStyle.DANGER).onClick {
                list.filter { it.checked }.forEach { list.remove(it) }
            }
        })
    }
}
