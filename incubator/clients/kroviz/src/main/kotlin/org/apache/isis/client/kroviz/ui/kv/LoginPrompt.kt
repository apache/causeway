package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.ui.Command
import org.apache.isis.client.kroviz.ui.FormItem
import pl.treksoft.kvision.form.text.Password
import pl.treksoft.kvision.form.text.Text

class LoginPrompt : Command() {

    private lateinit var form: RoDialog

    //Default values
    private var url = "http://localhost:8080/"
    private var username = "sven"
    private var password = "pass"

    fun open(at: org.apache.isis.client.kroviz.ui.Point) {
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("Url", "Text", url))
        formItems.add(FormItem("User", "Text", username))
        formItems.add(FormItem("Password", "Password", password))
        form = RoDialog(caption = "Connect", items = formItems, command = this)
        form.open(at)
    }

    override fun execute() {
        extractUserInput()
        UiManager.login(url, username, password)
        val link = Link(href = url + "restful/")
        invoke(link)
        UiManager.closeDialog(form)
    }

    private fun extractUserInput() {
        //TODO function has a side effect, ie. changes variable values
        var key: String?
        val formPanel = form.formPanel
        val kids = formPanel!!.getChildren()
        //iterate over FormItems (0,1,2) but not Buttons(3,4)
        for (i in kids) {
            when (i) {
                is Text -> {
                    key = i.label!!
                    if (key == "Url")
                        url = i.getValue()!!
                    if (key == "User")
                        username = i.getValue()!!
                }
                is Password -> {
                    password = i.getValue()!!
                }
            }
        }
    }

}
