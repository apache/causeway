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

class Col(node: Node) : XmlLayout() {
    val rowList = mutableListOf<Row>()
    var domainObject: DomainObject? = null
    var actionList = mutableListOf<Action>()
    val tabGroupList = mutableListOf<TabGroup>()
    var fieldSetList = mutableListOf<FieldSet>()
    var collectionList = mutableListOf<Collection>()
    var span: Int = 0

    init {
        val dyNode = node.asDynamic()
        span = dyNode.getAttribute("span") //as Int

        val nl = node.childNodes.asList()

        val rl = nl.filter { it.nodeName == "$nsBs:row" }
        for (n: Node in rl) {
            val row = Row(n)
            rowList.add(row)
        }

        val doNodes = nl.filter { it.nodeName == "$nsCpt:domainObject" }
        if (doNodes.isNotEmpty()) {
            domainObject = DomainObject(doNodes.first())
        }

        val actNodes = nl.filter { it.nodeName == "$nsCpt:action" }
        for (n: Node in actNodes) {
            val act = Action(n)
            actionList.add(act)
        }

        val tgNodes = nl.filter { it.nodeName == "$nsBs:tabGroup" }
        for (n: Node in tgNodes) {
            val tg = TabGroup(n)
            tabGroupList.add(tg)
        }

        val fsNodes = nl.filter { it.nodeName == "$nsCpt:fieldSet" }
        for (n: Node in fsNodes) {
            val fs = FieldSet(n)
            fieldSetList.add(fs)
        }

        val collNodes = nl.filter { it.nodeName == "$nsCpt:collection" }
        for (n: Node in collNodes) {
            val c = Collection(n)
            collectionList.add(c)
        }
    }

    fun getPropertyList(): List<Property> {
        val list = mutableListOf<Property>()
        rowList.forEach { r ->
            list.addAll(r.getPropertyList())
        }
        fieldSetList.forEach { fs ->
            list.addAll(fs.propertyList)
        }
        tabGroupList.forEach { tg ->
            list.addAll(tg.getPropertyList())
        }
        return list
    }

    override fun toString(): String {
        val c = this::class.simpleName!!
        return c + "\n" +
                "rowList: " + rowList.size + "\n" +
                "domainObject: " + domainObject.toString() + "\n" +
                "actionList: " + actionList.size + "\n" +
                "tabGroupList: " + tabGroupList.size + "\n" +
                "fieldSetList: " + fieldSetList.size + "\n" +
                "collectionList: " + collectionList.size + "\n" +
                "span: " + span + "\n"
    }

}
