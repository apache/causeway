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

import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.core.FormItem
import org.apache.isis.client.kroviz.ui.core.RoDialog
import org.apache.isis.client.kroviz.ui.core.UiManager

class NotificationDialog(val message: String) : Controller() {

    override fun open() {
        val formItems = mutableListOf<FormItem>()
        val fi = FormItem("Message", ValueType.TEXT_AREA, message, size = 5)
        fi.readOnly = true
        formItems.add(fi)
        val label = "Notifications"
        RoDialog(
                caption = label,
                items = formItems,
                controller = this,
                widthPerc = 80).open()
    }

    override fun execute(action: String?) {
        UiManager.getRoStatusBar().acknowledge()
    }

}
