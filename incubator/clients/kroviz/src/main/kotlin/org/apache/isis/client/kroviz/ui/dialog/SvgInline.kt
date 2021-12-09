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

import org.apache.isis.client.kroviz.core.aggregator.SvgDispatcher
import org.apache.isis.client.kroviz.core.event.ResourceProxy
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Method
import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.core.FormItem
import org.apache.isis.client.kroviz.ui.core.RoDialog
import org.apache.isis.client.kroviz.utils.UUID

class SvgInline : Controller() {
    private val formItems = mutableListOf<FormItem>()

    init {
        val callBack = UUID()
        val fiImg = FormItem(
                label = "svg",
                type = ValueType.SVG_INLINE,
                callBack = callBack)
        formItems.add(fiImg)
        dialog = RoDialog(
                widthPerc = 50,
                heightPerc = 50,
                caption = "Sample SVG Inline (Non-Interactive)",
                items = formItems,
                controller = this)
        val url = "https://upload.wikimedia.org/wikipedia/commons/6/6c/Trajans-Column-lower-animated.svg"
        val link = Link(href = url, method = Method.GET.operation)
        val agr = SvgDispatcher(callBack)
        ResourceProxy().fetch(link, agr, isRest = false)
    }

}
