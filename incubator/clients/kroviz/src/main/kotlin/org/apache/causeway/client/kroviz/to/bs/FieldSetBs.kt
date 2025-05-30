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
package org.apache.causeway.client.kroviz.to.bs

import org.w3c.dom.Node
import org.w3c.dom.asList

class FieldSetBs(node: Node) : XmlLayout() {
    var actionList = mutableListOf<ActionBs>()
    var propertyList = mutableListOf<PropertyBs>()
    var name: String = ""
    var id: String = ""

    init {
        val dyNode = node.asDynamic()
        if (dyNode.hasOwnProperty("name") as Boolean) {
            name = dyNode.getAttribute("name") as String
        }
        if (dyNode.hasOwnProperty("id") as Boolean) {
            id = dyNode.getAttribute("id") as String
        }
        val nl = node.childNodes.asList()
        val actList = nl.filter { it.nodeName == "$nsCpt:action" }
        for (n: Node in actList) {
            val act = ActionBs(n)
            actionList.add(act)
        }

        val pNl = nl.filter { it.nodeName == "$nsCpt:property" }
        for (n: Node in pNl) {
            val p = PropertyBs(n)
            if (p.hidden != "") {
                propertyList.add(p)
            }
        }
    }

}
