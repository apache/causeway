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

class Tab(node: Node) {
    val rowList = mutableListOf<Row>()
    var name: String

    init {
        val dyNode = node.asDynamic()
        name = dyNode.getAttribute("name") as String

        val nl = node.childNodes.asList()

        val rNodes = nl.filter { it.nodeName.equals("bs3:row") }
        for (n: Node in rNodes) {
            val row = Row(n)
            rowList.add(row)
        }
    }

    fun getPropertyList(): List<Property> {
        val list = mutableListOf<Property>()
        rowList.forEach { r ->
            list.addAll(r.getPropertyList())
        }
        return list
    }

}
