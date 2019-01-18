package com.example

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import pl.treksoft.kvision.form.FormPanel.Companion.formPanel
import pl.treksoft.kvision.form.check.CheckBox
import pl.treksoft.kvision.form.check.Radio
import pl.treksoft.kvision.form.check.RadioGroup
import pl.treksoft.kvision.form.select.AjaxOptions
import pl.treksoft.kvision.form.select.Select
import pl.treksoft.kvision.form.spinner.Spinner
import pl.treksoft.kvision.form.text.Password
import pl.treksoft.kvision.form.text.RichText
import pl.treksoft.kvision.form.text.Text
import pl.treksoft.kvision.form.text.TextArea
import pl.treksoft.kvision.form.time.DateTime
import pl.treksoft.kvision.form.upload.Upload
import pl.treksoft.kvision.html.Button.Companion.button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.i18n.I18n.tr
import pl.treksoft.kvision.modal.Alert
import pl.treksoft.kvision.modal.Confirm
import pl.treksoft.kvision.panel.FlexAlignItems
import pl.treksoft.kvision.panel.FlexWrap
import pl.treksoft.kvision.panel.HPanel
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.progress.ProgressBar
import pl.treksoft.kvision.types.KFile
import pl.treksoft.kvision.utils.obj
import pl.treksoft.kvision.utils.px
import kotlin.js.Date

@Serializable
data class Form(
    val text: String? = null,
    val password: String? = null,
    val password2: String? = null,
    val textarea: String? = null,
    val richtext: String? = null,
    val date: Date? = null,
    val time: Date? = null,
    val checkbox: Boolean = false,
    val radio: Boolean = false,
    val select: String? = null,
    val ajaxselect: String? = null,
    val spinner: Double? = null,
    val radiogroup: String? = null,
    val upload: List<KFile>? = null
)

class FormTab : SimplePanel() {
    init {

        this.marginTop = 10.px
        val formPanel = formPanel<Form> {
            add(
                Form::text,
                Text(label = tr("Required text field with regexp [0-9] validator")).apply {
                    placeholder = tr("Enter your age")
                },
                required = true,
                requiredMessage = tr("Value is required"),
                validatorMessage = { tr("Only numbers are allowed") }) {
                it.getValue()?.matches("^[0-9]+$")
            }
            add(Form::password, Password(label = tr("Password field with minimum length validator")),
                validatorMessage = { tr("Password too short") }) {
                (it.getValue()?.length ?: 0) >= 8
            }
            add(Form::password2, Password(label = tr("Password confirmation")),
                validatorMessage = { tr("Password too short") }) {
                (it.getValue()?.length ?: 0) >= 8
            }
            add(Form::textarea, TextArea(label = tr("Text area field")))
            add(
                Form::richtext,
                RichText(label = tr("Rich text field with a placeholder")).apply { placeholder = tr("Add some info") })
            add(
                Form::date,
                DateTime(format = "YYYY-MM-DD", label = tr("Date field with a placeholder")).apply {
                    placeholder = tr("Enter date")
                })
            add(
                Form::time,
                DateTime(format = "HH:mm", label = tr("Time field"))
            )
            add(
                Form::checkbox,
                CheckBox(label = tr("Required checkbox")),
                validatorMessage = { tr("Value is required") }
            ) { it.getValue() }
            add(Form::radio, Radio(label = tr("Radio button")))
            add(
                Form::select, Select(
                    options = listOf("first" to tr("First option"), "second" to tr("Second option")),
                    label = tr("Simple select")
                )
            )

            add(Form::ajaxselect, Select(label = tr("Select with remote data source")).apply {
                emptyOption = true
                ajaxOptions = AjaxOptions("https://api.github.com/search/repositories", preprocessData = {
                    it.items.map { item ->
                        obj {
                            this.value = item.id
                            this.text = item.name
                            this.data = obj {
                                this.subtext = item.owner.login
                            }
                        }
                    }
                }, data = obj {
                    q = "{{{q}}}"
                }, minLength = 3, requestDelay = 1000)
            })
            add(Form::spinner, Spinner(label = tr("Spinner field 10 - 20"), min = 10, max = 20))
            add(
                Form::radiogroup, RadioGroup(
                    listOf("option1" to tr("First option"), "option2" to tr("Second option")),
                    inline = true, label = tr("Radio button group")
                )
            )
            add(Form::upload, Upload("/", multiple = true, label = tr("Upload files (images only)")).apply {
                explorerTheme = true
                dropZoneEnabled = false
                allowedFileTypes = setOf("image")
            })
            validator = {
                val result = it[Form::password] == it[Form::password2]
                if (!result) {
                    it.getControl(Form::password)?.validatorError = tr("Passwords are not the same")
                    it.getControl(Form::password2)?.validatorError = tr("Passwords are not the same")
                }
                result
            }
            validatorMessage = { tr("The passwords are not the same.") }
        }
        formPanel.add(HPanel(spacing = 10, alignItems = FlexAlignItems.CENTER, wrap = FlexWrap.WRAP) {
            val p = ProgressBar(0, striped = true) {
                marginBottom = 0.px
                width = 300.px
            }
            button(tr("Show data"), "fa-info", ButtonStyle.SUCCESS).onClick {
                console.log(formPanel.getDataJson())
                Alert.show(tr("Form data in plain JSON"), JSON.stringify(formPanel.getDataJson(), space = 1))
            }
            button(tr("Clear data"), "fa-times", ButtonStyle.DANGER).onClick {
                Confirm.show(
                    tr("Are you sure?"),
                    tr("Do you want to clear your data?"),
                    yesTitle = tr("Yes"),
                    noTitle = tr("No"),
                    cancelTitle = tr("Cancel")
                ) {
                    formPanel.clearData()
                    p.progress = 0
                }
            }
            button(tr("Validate"), "fa-check", ButtonStyle.INFO).onClick {
                GlobalScope.launch {
                    p.progress = 100
                    delay(500)
                    formPanel.validate()
                }
            }
            add(p)
        })
    }
}