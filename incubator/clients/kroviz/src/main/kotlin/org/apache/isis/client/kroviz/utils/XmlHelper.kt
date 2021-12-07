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
package org.apache.isis.client.kroviz.utils

import io.kvision.utils.obj
import org.apache.isis.client.kroviz.ui.core.Constants
import org.apache.isis.client.kroviz.utils.js.XmlBeautify
import org.apache.isis.client.kroviz.utils.js.XmlToJson
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.asList
import org.w3c.dom.parsing.DOMParser
import org.w3c.fetch.RequestDestination

inline val RequestDestination.Companion.XSLT: RequestDestination
    get() {
        TODO()
    }

object XmlHelper {

    fun isXml(input: String): Boolean {
        val s = input.trim()
        return s.startsWith("<") && s.endsWith(">")
    }

    fun nonTextChildren(node: Node): List<Node> {
        val match = "#text"
        val childNodes = node.childNodes.asList()
        return childNodes.filter { !it.nodeName.contains(match) }
    }

    fun firstChildMatching(node: Node, match: String): Node? {
        val childNodes = node.childNodes.asList()
        val list = childNodes.filter { it.nodeName.contains(match) }
        return list.firstOrNull()
    }

    fun parseXml(xmlStr: String): Document {
        val p = DOMParser()
        return p.parseFromString(xmlStr, Constants.xmlMimeType)
    }

    fun format(inputXml: String): String {
        val options = obj {
            indent = "  "
            useSelfClosingElement = true
        }
        return XmlBeautify().beautify(inputXml, options)
    }

    fun xml2json(xml: String): String {
        val json = XmlToJson.parseString(xml)
        return JSON.stringify(json)
    }

}
