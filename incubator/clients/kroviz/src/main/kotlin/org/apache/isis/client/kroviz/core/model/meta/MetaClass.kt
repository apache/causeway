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
package org.apache.isis.client.kroviz.core.model.meta

import org.apache.isis.client.kroviz.to.DomainType

class MetaClass(val domainType: DomainType) {
    var name: String
    var pkg: MetaPackage
    val actions = mutableSetOf<MetaAction>()
    val properties = mutableSetOf<MetaProperty>()

    init {
        val cn = domainType.canonicalName
        name = cn.split(".").last()
        val pkgName = cn.replace(".$name", "")
        pkg = MetaPackage(pkgName)

        domainType.members.forEach { m ->
            val memberName = m.name()
            when {
                m.isProperty() -> properties.add(MetaProperty(memberName))
                m.isAction() -> actions.add(MetaAction(memberName))
                else -> {
                    console.log("[MetaClass.init] unexpected member type")
                    console.log(memberName)
                }
            }
        }
    }

}
