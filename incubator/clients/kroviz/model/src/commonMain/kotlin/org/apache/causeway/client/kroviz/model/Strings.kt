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
package org.apache.causeway.client.kroviz.model

/**
 * Case conversion between the identifiers used by the RESTful Objects API and the
 * labels shown to the user.  Free of any platform specific API, so that the model
 * layer compiles for JS as well as for the JVM.
 */
object Strings {

    @OptIn(ExperimentalStdlibApi::class)
    fun capitalize(input: String): String {
        return if (input.length > 0) {
            val output = input.substring(1, input.length)
            input.first().uppercaseChar() + output
        } else {
            input
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun decapitalize(input: String): String {
        val output = input.substring(1, input.length)
        return input.first().lowercaseChar() + output
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun enCamel(input: String): String {
        var output = ""
        val words = input.split(" ")
        for (w in words) {
            output += capitalize(w)
        }
        return decapitalize(output)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun deCamel(input: String): String {
        var output = ""
        for ((i, c) in input.withIndex()) {
            val cuc = c.uppercaseChar()
            if (i == 0) {
                output += cuc
            } else {
                val o = if (cuc == c) " $c" else c.toString()
                output += o
            }
        }
        // Skip acronyms like OK, USA
        val outputWithoutWhiteSpace = output.replace("\\s".toRegex(), "")
        return if (input == outputWithoutWhiteSpace) input else output
    }

}
