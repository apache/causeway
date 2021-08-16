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
package org.apache.isis.client.kroviz.core.model

import kotlinx.serialization.Serializable
import org.apache.isis.client.kroviz.to.Icon
import org.apache.isis.client.kroviz.to.MemberType
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.utils.IconManager

/**
 * Makes properties of delegate available for display in Lists.
 * For regular TObjects these are members (properties).
 * For FixtureResults these are: result, resultClass etc.
 *
 * Exposer bears some similarity to the JS "Revealing Module Pattern"
 * (see: https://addyosmani.com/resources/essentialjsdesignpatterns/book/),
 * but it goes further since it even reveals members of it's delegate.
 */
@Serializable
class Exposer(val delegate: TObject) {

    var iconName = ""  //required by ColumnFactory

    fun dynamise(): dynamic {
        val thys = this.asDynamic()
        for (m in delegate.members) {
            val member = m.value
            if (member.memberType == MemberType.PROPERTY.type) {
                val realValue = member.value
                if (realValue != null) {
                    thys[member.id] = realValue.content
                }
            }
        }
        iconName = IconManager.find(delegate.title)
        if (iconName == IconManager.DEFAULT_ICON) {
            iconName = IconManager.find(delegate.domainType)
        }
        return thys
    }

    // eg. for dataNucleusId
    fun get(propertyName: String): Any? {
        return this.delegate.getProperty(propertyName)?.value
    }

    fun setIcon(icon: Icon) {
        this.asDynamic()["icon"] = icon.image.src
    }

}
