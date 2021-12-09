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

import org.apache.isis.client.kroviz.to.Action
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Parameter
import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.ui.core.FormItem
import org.apache.isis.client.kroviz.utils.Point
import org.apache.isis.client.kroviz.utils.StringUtils
import io.kvision.core.StringPair
import io.kvision.form.select.SimpleSelect
import io.kvision.form.text.TextArea
import org.apache.isis.client.kroviz.ui.core.RoDialog

class ActionPrompt(val action: Action) : Controller() {

    fun open(at: Point) {
        val formItems = buildFormItems()
        dialog = RoDialog(
                caption = buildLabel(),
                items = formItems,
                controller = this)
        dialog.open(at)
    }

    override fun execute(action:String?) {
        val l = extractUserInput()
        invoke(l)
    }

    private fun buildLabel(): String {
        val label = StringUtils.deCamel(action.id)
        return "Execute: $label"
    }

    private fun buildFormItems(): List<FormItem> {
        val formItems = mutableListOf<FormItem>()
        val parameterList: Collection<Parameter> = action.parameters.values
        for (p in parameterList) {
            val v = p.name
            var type = ValueType.TEXT_AREA
            var content: Any = ""
            if (p.choices.isNotEmpty()) {
                type = ValueType.SIMPLE_SELECT
                content = buildSelectionList(p)
            }
            val fi = FormItem(v, type, content)
            formItems.add(fi)
        }
        return formItems
    }

    private fun buildSelectionList(parameter: Parameter): List<StringPair> {
        val selectionList = mutableListOf<StringPair>()
        val arguments = parameter.getChoiceListKeys()
        for (s in arguments) {
            val sp = StringPair(s, s)
            selectionList.add(sp)
        }
        return selectionList
    }

    private fun extractUserInput(): Link {
        //IMPROVE function has a side effect, i.e. amends link with arguments
        val link = action.getInvokeLink()!!
        var value: String? = null
        var key: String? = null
        val formPanel = dialog.formPanel
        val kids = formPanel!!.getChildren()
        //iterate over FormItems (0,1) but not Buttons(2,3)
        for (i in kids) {
            when (i) {
                is TextArea -> {
                    key = i.label!!
                    value = i.getValue()
                }
                is SimpleSelect -> {
                    key = i.label!!
                    value = i.getValue()!!
                    val p: Parameter = action.findParameterByName(key.toLowerCase())!!
                    val href = p.getHrefByTitle(value)!!
                    value = href
                }
            }
            if (key != null) {
                link.setArgument(key, value)
            }
        }
        return link
    }

}
