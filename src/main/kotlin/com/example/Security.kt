package com.example

import kotlinx.serialization.Serializable
import pl.treksoft.kvision.form.FormPanel
import pl.treksoft.kvision.form.FormPanel.Companion.formPanel
import pl.treksoft.kvision.form.text.Password
import pl.treksoft.kvision.form.text.Text
import pl.treksoft.kvision.html.Button
import pl.treksoft.kvision.html.ButtonStyle
import pl.treksoft.kvision.modal.Dialog
import pl.treksoft.kvision.remote.Credentials
import pl.treksoft.kvision.remote.LoginService
import pl.treksoft.kvision.remote.SecurityMgr
import pl.treksoft.kvision.utils.ENTER_KEY

@Serializable
data class User(
    val name: String? = null,
    val username: String? = null,
    val password: String? = null,
    val password2: String? = null
)

class LoginWindow : Dialog<Credentials>(closeButton = false, escape = false, animation = false) {

    private val loginPanel: FormPanel<Credentials>
    private val loginButton: Button
    private val userButton: Button
    private val registerPanel: FormPanel<User>
    private val registerButton: Button
    private val cancelButton: Button

    init {
        loginPanel = formPanel {
            add(Credentials::username, Text(label = "Login:"), required = true)
            add(Credentials::password, Password(label = "Password:"), required = true)
            setEventListener {
                keydown = {
                    if (it.keyCode == ENTER_KEY) {
                        processCredentials()
                    }
                }
            }
        }
        registerPanel = formPanel {
            add(User::name, Text(label = "Your name:"), required = true)
            add(User::username, Text(label = "Login:"), required = true)
            add(
                User::password, Password(label = "Password:"), required = true,
                validatorMessage = { "Password too short" }) {
                (it.getValue()?.length ?: 0) >= 8
            }
            add(User::password2, Password(label = "Confirm password:"), required = true,
                validatorMessage = { "Password too short" }) {
                (it.getValue()?.length ?: 0) >= 8
            }
            validator = {
                val result = it[User::password] == it[User::password2]
                if (!result) {
                    it.getControl(User::password)?.validatorError = "Passwords are not the same"
                    it.getControl(User::password2)?.validatorError = "Passwords are not the same"
                }
                result
            }
            validatorMessage = { "Passwords are not the same." }

        }
        cancelButton = Button("Cancel", "fa-times").onClick {
            hideRegisterForm()
        }
        registerButton = Button("Register", "fa-check", ButtonStyle.PRIMARY).onClick {
            processRegister()
        }
        loginButton = Button("Login", "fa-check", ButtonStyle.PRIMARY).onClick {
            processCredentials()
        }
        userButton = Button("Register user", "fa-user").onClick {
            showRegisterForm()
        }
        addButton(userButton)
        addButton(loginButton)
        addButton(cancelButton)
        addButton(registerButton)
        hideRegisterForm()
    }

    private fun showRegisterForm() {
        loginPanel.hide()
        registerPanel.show()
        registerPanel.clearData()
        loginButton.hide()
        userButton.hide()
        cancelButton.show()
        registerButton.show()
    }

    private fun hideRegisterForm() {
        loginPanel.show()
        registerPanel.hide()
        loginButton.show()
        userButton.show()
        cancelButton.hide()
        registerButton.hide()
    }

    private fun processCredentials() {
        if (loginPanel.validate()) {
            setResult(loginPanel.getData())
            loginPanel.clearData()
        }
    }

    private fun processRegister() {
        if (registerPanel.validate()) {
            val userData = registerPanel.getData()
 /*           GlobalScope.launch {
                if (Model.registerProfile(
                        Profile(
                            userData.username
                        ).apply {
                            username = userData.username
                            displayName = userData.name
                        },
                        userData.password ?: ""
                    )
                ) {
                    Alert.show(text = "User registered. You can now log in.") {
                        hideRegisterForm()
                    }
                } else {
                    Alert.show(text = "This login is not available. Please try again.")
                }
            } */
        }
    }
}

object Security : SecurityMgr() {

    private val loginService = LoginService()
    private val loginWindow = LoginWindow()

    override suspend fun login(): Boolean {
        return loginService.login(loginWindow.getResult())
    }

    override suspend fun afterLogin() {
//        Model.readProfile()
    }
}
