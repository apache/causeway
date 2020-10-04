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
package org.apache.isis.client.kroviz.to

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Parameter(val id: String,
                val num: Int = 0,
                val description: String,
                val name: String,
        // choices either are a list of:
        // Links -> ACTIONS_RUN_FIXTURE_SCRIPT
        // Strings -> ACTIONS_DOWNLOAD_LAYOUTS
                val choices: List<Value> = emptyList(),
                @SerialName("default") val defaultChoice: Value? = null
) : TransferObject {

    fun getChoiceListKeys(): MutableList<String> {
        val result: MutableList<String> = mutableListOf()
        for (c in choices) {
            when (c.content) {
                is Link -> {
                    result.add((c.content as Link).title)
                }
                is String -> {
                    result.add(c.content as String)
                }
            }
        }
        return result
    }

    fun getHrefByTitle(title: String): String? {
        for (c in choices) {
            val l = c.content
            when (l) {
                is Link -> {
                    if (l.title == title) {
                        return l.href
                    }
                }
                is String -> {
                    return l
                }
            }
        }
        return null
    }

}
