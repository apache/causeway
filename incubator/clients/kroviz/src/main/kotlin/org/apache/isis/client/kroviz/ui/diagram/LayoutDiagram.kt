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

object LayoutDiagram {

    @Deprecated("pass in as arg")
    val sampleCode = "@startsalt\n" +
            "{#\n" +
            ". | Column 2 | Column 3\n" +
            "Row header 1 | value 1 | value 2\n" +
            "Row header 2 | A long cell | *\n" +
            "}\n" +
            "@endsalt"

    fun build(json: String): String {
        var pumlCode = sampleCode
        return pumlCode
    }

    private fun buildRow(): String {
        val span = 1
        return ""
    }

}
