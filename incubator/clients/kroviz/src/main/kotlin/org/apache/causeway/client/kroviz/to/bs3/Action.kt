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

class Action(node: Node) {
    var bookmarking: String //BookmarkPolicy? = null use ENUM
    var cssClass: String
    var cssClassFa: String
    var cssClassFaPosition: String
    var hidden: String // USE ENUM Where? = null
    var id: String
    var position: String //USE ENUM Position? = null
    var named = ""
    var describedAs = ""

    init {
        val dyNode = node.asDynamic()
        bookmarking = dyNode.getAttribute("bookmarking")
        cssClass = dyNode.getAttribute("cssClass")
        cssClassFa = dyNode.getAttribute("cssClassFa")
        cssClassFaPosition = dyNode.getAttribute("cssClassFaPosition")
        hidden = dyNode.getAttribute("hidden")
        id = dyNode.getAttribute("id")
        position = dyNode.getAttribute("position")
        named = dyNode.getAttribute("named")
        describedAs = dyNode.getAttribute("describedAs")

        val nodeList = node.childNodes.asList()
        val namedList = nodeList.filter { it.nodeName.equals("cpt:named") }
        if (namedList.isNotEmpty()) {
            val n = namedList.first()
            named = n.textContent as String
        }
        val describedAsList = nodeList.filter { it.nodeName.equals("cpt:describedAs") }
        if (describedAsList.isNotEmpty()) {
            val n = describedAsList.first()
            describedAs = n.textContent as String
        }
    }
}
