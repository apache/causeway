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

import kotlin.random.Random

class UUID() {
    var value: String

    // Returns a 36-character string in the form
    // XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
    // 12345678 9012 3456 7890 123456789012
    init {
        val abData = Random.Default.nextBytes(16)
        abData[6] = (0x40 or (abData[6].toInt() and 0xf)).toByte()
        abData[8] = (0x80 or (abData[8].toInt() and 0x3f)).toByte()
        val strHex = abData.toHexString()
        val s1 = strHex.substring(0, 8)
        val s2 = strHex.substring(8, 4)
        val s3 = strHex.substring(12, 4)
        val s4 = strHex.substring(16, 4)
        val s5 = strHex.substring(20, 12)
        value = "$s1-$s2-$s3-$s4-$s5"
    }

    constructor(string: String) : this() {
        value = string
    }

    @OptIn(kotlin.ExperimentalUnsignedTypes::class)
    fun ByteArray.toHexString() = asUByteArray().joinToString("") {
        it.toString(16).padStart(2, '0')
    }

}
