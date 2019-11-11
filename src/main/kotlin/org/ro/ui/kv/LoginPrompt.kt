package org.ro.ui.kv

import org.ro.core.Session
import org.ro.org.ro.ui.kv.RoDialog
import org.ro.to.Link
import org.ro.ui.Command
import org.ro.ui.uicomp.FormItem
import pl.treksoft.kvision.form.text.Password
import pl.treksoft.kvision.form.text.Text

class LoginPrompt() : Command {

    lateinit var form: RoDialog

    //Default values
    private var url = "http://localhost:8080/"
    private var username = "sven"
    private var password = "pass"

    fun open() {
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("Url", "Text", url))
        formItems.add(FormItem("User", "Text", username))
        formItems.add(FormItem("Password", "Password", password))
        form = RoDialog(caption = "Connect", items = formItems, command = this)
        form.show()
    }

    override fun execute() {
        extractUserInput()
        Session.login(url, username, password)
        val link = Link(href = url + "restful/services/")
        invoke(link)
        UiManager.closeDialog(form)
    }

    fun extractUserInput() {
        //TODO function has a sideeffect, ie. changes variable values
        var key: String?
        val formPanel = form.panel
        val kids = formPanel!!.getChildren()
        //iterate over FormItems (0,1,2) but not Buttons(3,4)
        for (i in kids) {
            when (i) {
                is Text -> {
                    key = i.label!!
                    if (key.equals("Url"))
                        url = i.getValue()!!
                    if (key.equals("User"))
                        username = i.getValue()!!
                }
                is Password -> {
                    password = i.getValue()!!
                }
            }
        }
    }

}
