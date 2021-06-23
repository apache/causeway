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

import org.apache.isis.client.kroviz.to.Link
import org.w3c.dom.Node
import org.w3c.dom.asList

//IMPROVE class differs in many aspects from org.ro.to.Property - to be refactored?
class Property(node: Node) {
    var id: String
    var link: Link? = null
    var hidden: String = "" // USE ENUM Where? = null
    var typicalLength: Int = 0
    var multiLine: Int = 1
    var describedAs: String? = null
    var named = ""
    lateinit var action: Action

    init {
        val dn = node.asDynamic()
        if (dn.hasOwnProperty("hidden")) {
            id = dn.getAttribute("hidden") as String
        }
        id = dn.getAttribute("id") as String
        typicalLength = dn.getAttribute("typicalLength")
        multiLine = dn.getAttribute("multiLine")
        describedAs = dn.getAttribute("describedAs")

        val nodeList = node.childNodes.asList()
        val namedList = nodeList.filter { it.nodeName.equals("cpt:named") }
        if (namedList.isNotEmpty()) {
            val n = namedList.first()
            named = n.textContent as String
        }

        val actList = nodeList.filter { it.nodeName.equals("cpt:action") }
        if (actList.isNotEmpty()) {
            val n = actList.first()
            action = Action(n)
        }

        val linkList = nodeList.filter { it.nodeName.equals("cpt:link") }
        if (linkList.isNotEmpty()) {
            val n = linkList.first()
            val bs3l = Link(n)
            link = Link(bs3l.rel, bs3l.method, bs3l.href, bs3l.type)
        }
    }

}
