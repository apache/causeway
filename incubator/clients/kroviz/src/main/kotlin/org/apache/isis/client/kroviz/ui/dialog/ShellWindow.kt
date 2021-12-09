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

import io.kvision.panel.SimplePanel
import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.core.FormItem
import org.apache.isis.client.kroviz.ui.core.RoDialog
import org.apache.isis.client.kroviz.ui.core.UiManager
import org.apache.isis.client.kroviz.utils.DomUtil
import org.apache.isis.client.kroviz.utils.UUID
import org.apache.isis.client.kroviz.utils.js.Xterm

class ShellWindow(val host: String) : Controller() {
    val uuid = UUID()

    init {
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("SSH", ValueType.SHELL, host, callBack = uuid))
        dialog = RoDialog(
            caption = host,
            items = formItems,
            controller = this,
            widthPerc = 70,
            heightPerc = 70,
            defaultAction = "Pin"
        )
    }

    override fun open() {
        //https://stackoverflow.com/questions/61607823/how-to-create-interactive-ssh-terminal-and-enter-commands-from-the-browser-using/61632083#61632083
        super.open()
        Xterm().open(DomUtil.getById(uuid.toString())!!)
        Xterm().write("Hello from \\x1B[1;3;31mxterm.js\\x1B[0m $ ")
    }

    fun execute() {
        pin()
    }

    private fun pin() {
        UiManager.add(host, dialog.formPanel as SimplePanel)
        dialog.close()
    }

}
