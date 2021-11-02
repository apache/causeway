/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.client.kroviz.ui.dialog

import io.kvision.form.text.Password
import io.kvision.form.text.Text
import kotlinx.browser.document
import kotlinx.browser.window
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.core.Constants
import org.apache.isis.client.kroviz.ui.core.FormItem
import org.apache.isis.client.kroviz.ui.core.RoDialog
import org.apache.isis.client.kroviz.ui.core.UiManager
import org.apache.isis.client.kroviz.utils.Point

class LoginPrompt : Command() {

    private lateinit var form: RoDialog

    //Default values
    private var url = Constants.demoUrl
    private var username = Constants.demoUser
    private var password = Constants.demoPass

    fun open() {
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("Url", ValueType.TEXT, url))
        formItems.add(FormItem("User", ValueType.TEXT, username))
        formItems.add(FormItem("Password", ValueType.PASSWORD, password))
        form = RoDialog(caption = "Connect", items = formItems, command = this, heightPerc = 27)
        val at = UiManager.position!!
        form.open(at)
    }

    override fun execute(action: String?) {
        extractUserInput()
        UiManager.login(url, username, password)
        val link = Link(href = url + Constants.restInfix)
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
