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
package org.apache.isis.client.kroviz.ui.kv

import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import pl.treksoft.kvision.form.formPanel
import pl.treksoft.kvision.form.select.AjaxOptions
import pl.treksoft.kvision.form.select.Select
import pl.treksoft.kvision.utils.obj
import pl.treksoft.kvision.utils.px
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.utils.pc
import kotlin.js.Date

@Serializable
data class Form(
        val select: String? = null,
        val ajaxselect: String? = null,
)

class DropdownSearch() : SimplePanel() {
    val cachedValues = listOf("1" to "About", "2" to "Base", "3" to "Blog", "4" to "Contact", "5" to "Custom", "6" to "Support", "7" to "Tools")
    init {
        this.marginTop = 10.px
        this.marginLeft = 40.px
        this.width = 100.pc
        val formPanel = formPanel<Form> {
            add(
                    Form::select, Select(
                    options = cachedValues,
                    label = "Dropdown search with in-memory values"
            ).apply {
                liveSearch = true}
            )
            add(Form::ajaxselect, Select(label = "Dropdown search on remote data source").apply {
                emptyOption = true
                ajaxOptions = AjaxOptions("https://api.github.com/search/repositories", preprocessData = {
                    it.items.map { item ->
                        obj {
                            this.value = item.id
                            this.text = item.name
                            this.data = obj {
                                this.subtext = item.owner.login
                            }
                        }
                    }
                }, data = obj {
                    q = "{{{q}}}"
                }, minLength = 3, requestDelay = 500)
            })
        }
    }
}