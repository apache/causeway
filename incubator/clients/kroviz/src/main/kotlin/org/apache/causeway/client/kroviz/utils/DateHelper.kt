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
package org.apache.causeway.client.kroviz.utils

import kotlin.js.Date

object DateHelper {

    fun toDate(content: Any?): Date {
        val result = when (content) {
            is String -> {
                var s = content
                if (!s.contains("-")) {
                    s = convertJavaOffsetDateTimeToISO(content)
                }
                val millis = Date.parse(s)
                Date(millis)
            }
            is Long -> {
                Date(content as Number)
            }
            else -> {
                Date()
            }
        }
        return result
    }

    fun convertJavaOffsetDateTimeToISO(input: String): String {
        val year = input.substring(0, 4)
        val month = input.substring(4, 6)
        val dayEtc = input.substring(6, 11)
        val minutes = input.substring(11, 13)
        val rest = input.substring(13, input.length)
        val output = "$year-$month-$dayEtc:$minutes:$rest"
        return output
    }

}
