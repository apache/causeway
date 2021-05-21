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
package org.apache.isis.client.kroviz.core

import org.apache.isis.client.kroviz.ui.core.UiManager

/**
 * Keep track of connected server.
 */
class Session {
    private var user: String = ""
    private var pw: String = ""
    var url: String = ""

    fun login(url: String, user: String, pw: String) {
        this.user = user
        this.pw = pw
        this.url = url
        UiManager.updateUser(user)
//        UiManager.updatePower("Powered By: Apache Isis")
    }

    fun getCredentials(): String {
        return "$user:$pw".base64encoded
    }

    /**
     * https://discuss.kotlinlang.org/t/kotlin-native-base64-en-decoder-code/10043
     */
    private val BASE64_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    /**
     * Base64 encode a string.
     */
    val String.base64encoded: String
        get() {
            val pad = when (this.length % 3) {
                1 -> "=="
                2 -> "="
                else -> ""
            }
            var raw = this
            (1..pad.length).forEach { raw += 0.toChar() }
            return StringBuilder().apply {
                (0 until raw.length step 3).forEach {
                    val n: Int = (0xFF.and(raw[it].toInt()) shl 16) +
                            (0xFF.and(raw[it + 1].toInt()) shl 8) +
                            0xFF.and(raw[it + 2].toInt())
                    listOf<Int>((n shr 18) and 0x3F,
                            (n shr 12) and 0x3F,
                            (n shr 6) and 0x3F,
                            n and 0x3F).forEach { append(BASE64_SET[it]) }
                }
            }.dropLast(pad.length)
                    .toString() + pad
        }

}
