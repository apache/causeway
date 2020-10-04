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

import org.apache.isis.client.kroviz.to.TransferObject
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.asList

/**
 * For the Wicket Viewer the following layout is used:
 * * rows[0] (head) contains the object title and actions
 * * rows[1] contains data, tabs, collections, etc.
 * * there may be N other rows as well
 * Please note, that rows may be children of Tab as well (recursive)
 */
class Grid(document: Document) : TransferObject {
    var rows = ArrayList<Row>()

    init {
        val root = document.firstChild!!
        val kids = root.childNodes
        val rowNodes = kids.asList()
        val rowList = rowNodes.filter { it.nodeName.equals("bs3:row") }
        for (n: Node in rowList) {
            val row = Row(n)
            rows.add(row)
        }
    }

}
