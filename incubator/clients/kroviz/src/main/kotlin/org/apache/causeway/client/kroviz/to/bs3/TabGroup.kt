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
package org.apache.causeway.client.kroviz.to.bs3

import org.w3c.dom.Node
import org.w3c.dom.asList

class TabGroup(node: Node) {
    var tabList = mutableListOf<Tab>()
    lateinit var metadataError: String
    lateinit var cssClass: String

    init {
        val nodeList = node.childNodes.asList()

        val tnList = nodeList.filter { it.nodeName.equals("bs3:tab") }
        for (n: Node in tnList) {
            val tab =Tab(n)
            tabList.add(tab)
        }
    }

    fun getPropertyList(): List<Property> {
        val list = mutableListOf<Property>()
        tabList.forEach { t ->
            list.addAll(t.getPropertyList())
        }
        return list
    }

}
