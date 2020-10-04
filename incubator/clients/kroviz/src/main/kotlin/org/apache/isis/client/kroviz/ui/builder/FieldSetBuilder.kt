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
package org.apache.isis.client.kroviz.ui.builder

import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.ValueType
import org.apache.isis.client.kroviz.to.bs3.FieldSet
import org.apache.isis.client.kroviz.ui.FormItem
import org.apache.isis.client.kroviz.ui.kv.FormPanelFactory
import org.apache.isis.client.kroviz.ui.kv.RoDisplay
import pl.treksoft.kvision.form.FormPanel

class FieldSetBuilder {

    fun create(
            fieldSetLayout: FieldSet,
            tObject: TObject,
            tab: RoDisplay
    ): FormPanel<String>? {

        val members = tObject.getProperties()
        val items = mutableListOf<FormItem>()
        for (p in fieldSetLayout.propertyList) {
            val label = p.id

            val member = members.firstOrNull() { it.id == label }

            if (member != null) {
                var size = 1
                if (p.multiLine > 1) {
                    member.type = ValueType.TEXT_AREA.type
                    size = p.multiLine
                }

                val fi = FormItem(
                        label = label,
                        type = member.type!!,
                        content = member.value?.content,
                        size = size,
                        description = p.describedAs,
                        member = member,
                        dspl = tab)
                items.add(fi)
            }
        }
        return FormPanelFactory(items).panel
    }

}
