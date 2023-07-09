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
package org.apache.causeway.client.kroviz.core.model

import org.apache.causeway.client.kroviz.to.MemberType
import org.apache.causeway.client.kroviz.to.TObject
import org.apache.causeway.client.kroviz.utils.IconManager

/**
 * Makes properties of delegate available for display in tables.
 * For regular TObjects these are members (properties).
 * For FixtureResults these are: result, resultClass etc.
 *
 * Exposer bears some similarity to the JS "Revealing Module Pattern"
 * (see: https://addyosmani.com/resources/essentialjsdesignpatterns/book/),
 * but it goes further since it even reveals members of its delegate.
 */
class Exposer(val delegate: TObject) {
    val iconName: String = IconManager.findFor(delegate) //required by ColumnFactory

    init {
        val that = this.asDynamic()
        that["icon"] = null
        for (m in delegate.members) {
            val member = m.value
            if (member.memberType == MemberType.PROPERTY.type) {
                val key = member.id
                val value = member.value
                if (value != null) {
                    that[key] = value.content
                }
            }
        }
    }

    fun getWithDelegateProperties(): dynamic {
        return this.asDynamic()
    }

    fun get(propertyName: String): Any? {
        return this.delegate.getProperty(propertyName)?.value
    }

}
