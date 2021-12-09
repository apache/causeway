/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.isis.client.kroviz.ui.dialog

import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.core.FormItem
import org.apache.isis.client.kroviz.ui.core.RoDialog
import org.apache.isis.client.kroviz.utils.UUID

class About : Controller() {
    private val formItems = mutableListOf<FormItem>()
    private val _LI = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."

    init {
        // see: https://upload.wikimedia.org/wikipedia/commons/6/6c/Trajans-Column-lower-animated.svg
        val fiImg = FormItem(
                label = "svg",
                type = ValueType.SVG_INLINE,
                callBack = UUID())
        formItems.add(fiImg)
        val fiText = FormItem(
                label = "",
                content = _LI,
                type = ValueType.TEXT_AREA,
                size = 10)
        formItems.add(fiText)
        dialog = RoDialog(
                widthPerc = 50,
                caption = "About",
                items = formItems,
                controller = this)
    }

}
