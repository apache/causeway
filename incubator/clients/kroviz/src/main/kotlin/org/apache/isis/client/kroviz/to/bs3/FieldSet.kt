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
package org.apache.isis.client.kroviz.to.bs3

import org.w3c.dom.Node
import org.w3c.dom.asList

class FieldSet(node: Node) {
    var actionList = mutableListOf<Action>()
    var propertyList = mutableListOf<Property>()
    var name: String = ""
    var id: String = ""

    init {
        val dyNode = node.asDynamic()
        if (dyNode.hasOwnProperty("name")) {
            name = dyNode.getAttribute("name") as String
        }
        if (dyNode.hasOwnProperty("id")) {
            id = dyNode.getAttribute("id") as String
        }

        val nl = node.childNodes.asList()
        val actList = nl.filter { it.nodeName.equals("cpt:action") }
        for (n: Node in actList) {
            val act = Action(n)
            actionList.add(act)
        }

        val pNl = nl.filter { it.nodeName.equals("cpt:property") }
        for (n: Node in pNl) {
            val p = Property(n)
            propertyList.add(p)
        }
    }

}
