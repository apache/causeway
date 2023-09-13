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
package org.apache.causeway.client.kroviz.ui.builder

import io.kvision.form.FormPanel
import org.apache.causeway.client.kroviz.to.TObject
import org.apache.causeway.client.kroviz.to.TypeMapper
import org.apache.causeway.client.kroviz.to.ValueType
import org.apache.causeway.client.kroviz.to.bs.FieldSetBs
import org.apache.causeway.client.kroviz.ui.core.FormItem
import org.apache.causeway.client.kroviz.ui.core.FormPanelFactory
import org.apache.causeway.client.kroviz.utils.js.AsciiDoctor

class FieldSetBuilder {

    fun create(
        fieldSetLayout: FieldSetBs,
        tObject: TObject,
        tab: RoDisplay
    ): FormPanel<String>? {

        val members = tObject.getProperties()
        val items = mutableListOf<FormItem>()

        for (p in fieldSetLayout.propertyList) {
            var label = p.id
            val member = members.firstOrNull() { it.id == label }
            if (member != null) {
                val memberType = TypeMapper.forType(member.type!!)
                var content = member.value?.content
                label = p.named
                if (memberType == ValueType.HTML && content is String) {
                    when {
                        content.startsWith(":Notice:") -> content = AsciiDoctor.convert(content)
                        content.startsWith("link:") -> content = content.replace("link:", "")
                        else -> {}
                    }
                }
                val size = maxOf(1, p.multiLine)
                val fi = FormItem(
                    label = label,
                    type = memberType,
                    content = content,
                    size = size,
                    description = p.describedAs,
                    member = member,
                    dspl = tab
                )
                items.add(fi)
            }
        }
        return FormPanelFactory(items).panel
    }

}
