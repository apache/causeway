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
package org.apache.isis.client.kroviz.ui.diagram

class Tree(val root: Node) {

    fun addChildToParent(childUrl: String, parentUrl: String) {
        var p = find(root, parentUrl)
        if (p == null) {
            console.log("[Tree.addChildToParent] nothing found for parentUrl")
            console.log(childUrl)
            console.log(parentUrl)
            p = root
        }
        val c = Node(childUrl, p)
        p.children.add(c)
    }

    fun find(node: Node, url: String): Node? {
        if (node.name == url) {
            return node
        } else {
            node.children.forEach {
                val result: Node? = find(it, url)
                if (result != null) {
                    return result
                }
            }
            return null
        }
    }

}

class Node(val name: String, val parent: Node?) {

    val children = mutableListOf<Node>()

}

