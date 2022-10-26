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

class Link(node: Node) : XmlLayout() {
    lateinit var rel: String
    lateinit var method: String
    lateinit var href: String
    lateinit var type: String

    init {
        val nodeList = node.childNodes.asList()

        val relList = nodeList.filter { it.nodeName == "$nsLnk:rel" }
        if (relList.isNotEmpty()) {
            val n = relList.first()
            rel = n.textContent as String
        }

        val methodList = nodeList.filter { it.nodeName == "$nsLnk:method" }
        if (methodList.isNotEmpty()) {
            val n = methodList.first()
            method = n.textContent as String
        }

        val hrefList = nodeList.filter { it.nodeName == "$nsLnk:href" }
        if (hrefList.isNotEmpty()) {
            val n = hrefList.first()
            href = n.textContent as String
        }

        val typeList = nodeList.filter { it.nodeName == "$nsLnk:type" }
        if (typeList.isNotEmpty()) {
            val n = typeList.first()
            type = n.textContent as String
        }
    }

    override fun toString(): String {
        val c = this::class.simpleName!!
        return c + "\n" +
                "rel: " + rel + "\n" +
                "method: " + method + "\n" +
                "href: " + href + "\n" +
                "type: " + type + "\n"
    }

}
