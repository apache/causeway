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

import org.apache.causeway.client.kroviz.utils.XmlHelper
import org.w3c.dom.Node

class DomainObject(node: Node) {
    var named = ""
    var plural = ""
    lateinit var describedAs: String
    lateinit var metadataError: String
    lateinit var link:org.apache.causeway.client.kroviz.to.Link
    lateinit var cssClass: String
    lateinit var cssClassFa: String

    init {
        val nn = XmlHelper.firstChildMatching(node, "named")
        if (nn?.textContent != null) {
            named = nn.textContent!!.trim()
        }

        val pn = XmlHelper.firstChildMatching(node, "plural")
        if (pn?.textContent != null) {
            plural = pn.textContent!!.trim()
        }
    }

}
