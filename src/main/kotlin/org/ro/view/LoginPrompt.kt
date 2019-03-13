package org.ro.view

import org.ro.core.Session
import org.ro.to.Link

class LoginPrompt() : Command {
    //Default values
    val url = "http://localhost:8080/"
    val username = "sven"
    val password = "pass"

    fun open() {
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("Url", "Text", url))
        formItems.add(FormItem("User", "Text", username))
        formItems.add(FormItem("Password", "Text", password))
        RoDialog(label = "Login", items = formItems, command = this).show()
    }
    
    override fun execute() {
        //FIXME url, username, password to be taken from Dialog
        Session.login(url, username, password)
        console.log("[LoginPrompt.execute: $Session]")
        val link = Link(href= "http://localhost:8080/restful/services/")
        link.invoke()
    }
}