package org.ro.view

import kotlinx.serialization.Serializable
import org.ro.core.Session
import org.ro.to.Link
import pl.treksoft.kvision.form.FormPanel
import pl.treksoft.kvision.form.FormPanel.Companion.formPanel
import pl.treksoft.kvision.form.text.Text
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.modal.Dialog
import pl.treksoft.kvision.utils.ENTER_KEY

@Serializable
data class HostCredential(
        var nix:String
)

class LoginDialog : Dialog<Any>(caption = "Connect", closeButton = true, escape = true, animation = true) {
    //Default values
    val url = "http://localhost:8080/"
    val username = "sven"
    val password = "pass"

    private val loginPanel: FormPanel<HostCredential>
    private val loginButton: Button
    private val cancelButton: Button

    init {
        loginPanel = formPanel {
            add(Text(label = "Url:", value = url))
            add(Text(label = "Login:", value = username))
            add(Text(label = "Password:", value = password))
            setEventListener {
                keydown = {
                    if (it.keyCode == ENTER_KEY) {
                        process()
                    }
                }
            }
        }
        cancelButton = Button("Cancel", "fa-times", ButtonStyle.DEFAULT).onClick {
            this.dispose()
        }
        loginButton = Button("Login", "fa-check", ButtonStyle.PRIMARY).onClick {
            process()
        }
        addButton(cancelButton)
        addButton(loginButton)
    }

    private fun process() {
        //TODO get data from input fields
/*        val urlIn: String = loginPanel.getData().get("Url")
        val userIn: String = loginPanel.getChildren()[1].getElement()!!.textContent!!
        val pwIn: String = if (loginPanel.getChildren()[2].getElement()!!.textContent != null) loginPanel.getChildren()[2].getElement()!!.textContent else throw NullPointerException("Expression 'loginPanel.getChildren()[2].getElement()!!.textContent' must not be null")*/
        Session.login(url, username, password)
        console.log("[LoginDialog.process: $Session]")
        this.dispose()

        val link = Link(href= "http://localhost:8080/restful/services/")
        link.invoke()
    }

}
