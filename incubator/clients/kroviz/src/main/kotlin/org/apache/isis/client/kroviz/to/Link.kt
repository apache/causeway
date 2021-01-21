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

import kotlinx.serialization.Serializable
import org.apache.isis.client.kroviz.utils.Utils

@Serializable
data class Link(val rel: String = "",
                val method: String = Method.GET.operation,
                val href: String,
                val type: String = "",
        //IMPROVE RO SPEC? "args" should be changed to "arguments" - RO spec or SimpleApp?
                val args: Map<String, Argument> = emptyMap(),
        /* arguments can either be:
         * -> empty Map {}
         * -> Map with "value": null (cf. SO_PROPERTY)
         * -> Map with empty key "" (cf. ACTIONS_DOWNLOAD_META_MODEL)
         * -> Map with key,<VALUE> (cf. ACTIONS_RUN_FIXTURE_SCRIPT, ACTIONS_FIND_BY_NAME, ACTIONS_CREATE) */
                val arguments: Map<String, Argument?> = emptyMap(),
                val title: String = "")
    : TransferObject {

    private val relPrefix = "urn:org.restfulobjects:rels/"

    fun argMap(): Map<String, Argument?>? {
        return when {
            arguments.isNotEmpty() -> arguments
            args.isNotEmpty() -> args
            else -> null
        }
    }

    fun setArgument(key: String, value: String?) {
        val k = Utils.enCamel(key)
        val arg = argMap()!!.get(k)
        arg!!.key = k
        arg.value = value
    }

    fun hasArguments(): Boolean {
        return !argMap().isNullOrEmpty()
    }

    fun isProperty(): Boolean {
        return rel.endsWith("/property")
    }

    fun isAction(): Boolean {
        return rel.endsWith("/action")
    }

    fun name(): String {
        return href.split("/").last()
    }

}
