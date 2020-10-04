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

import org.apache.isis.client.kroviz.utils.XmlHelper
import org.w3c.dom.Node

class Row(node: Node) {
    val colList = mutableListOf<Col>()
    var id: String = ""

    init {
        val dyNode = node.asDynamic()
        if (dyNode.hasOwnProperty("id")) {
            id = dyNode.getAttribute("id") as String
        }

        val nodeList = XmlHelper.nonTextChildren(node)
        val cl = nodeList.filter { it.nodeName.equals("bs3:col") }
        for (n: Node in cl) {
            val col = Col(n)
            colList.add(col)
        }
    }

    fun getPropertyList(): List<Property> {
        val list = mutableListOf<Property>()
        colList.forEach { c ->
            list.addAll(c.getPropertyList())
        }
        return list
    }

}
