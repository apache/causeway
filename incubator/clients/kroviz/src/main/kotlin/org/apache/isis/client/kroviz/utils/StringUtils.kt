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

import org.apache.isis.client.kroviz.to.Argument
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.ui.core.Constants
import org.apache.isis.client.kroviz.ui.core.SessionManager
import org.apache.isis.client.kroviz.ui.core.UiManager

object StringUtils {

    fun enCamel(input: String): String {
        var output = ""
        val words = input.split(" ")
        for (w in words) {
            output += capitalize(w)
        }
        return decapitalize(output)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun capitalize(input: String): String {
        val output = input.substring(1, input.length)
        return input.first().uppercaseChar() + output
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun decapitalize(input: String): String {
        val output = input.substring(1, input.length)
        return input.first().lowercaseChar() + output
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

    fun shortTitle(url: String): String {
        val signature = Constants.restInfix
        val protocolHostPort = url.split(signature).first()
        var title = url
        if (title.contains(signature)) {
            // strip off protocol, host, port
            title = title.replace(protocolHostPort + signature, "")
            title = removeHexCode(title)
        }
        return title
    }

    fun removeHexCode(input: String): String {
        var output = ""
        val list: List<String> = input.split("/")
        //split string by "/" and remove parts longer than 40chars
        list.forEach { s ->
            output += "/"
            output += if (s.length > 40) "..." else s
        }
        return output
    }

    //IMPROVE use JSON.stringify on a Map consisting of member.id, member.value
    fun propertiesAsBody(tObject: TObject): String {
        val members = tObject.members
        val mutableProperties = members.filter { it.value.isReadWrite() }
        var body = "{"
        mutableProperties.forEach {
            val m = it.value
            body += quote(m.id) + ":"
            val content = m.value?.content.toString()
            body += enbrace("value", quote(content)) + "},"
        }
        val len = body.length
        body = body.replaceRange(len - 1, len, "}")
        return body
    }

    fun argumentsAsBody(link: Link): String {
        val args = link.argMap()!!
        var body = "{"
        args.forEach {
            val arg = it.value!!
            body = body + asBody(arg) + ","
        }
        val len = body.length
        body = body.replaceRange(len - 1, len, "}")
        return body
    }

    fun argumentsAsUrlParameter(link: Link): String {
        val args = link.argMap()
        return argumentsAsString(args, "?", "&", "")
    }

    fun argumentsAsList(link: Link): String {
        val args = link.args
        return argumentsAsList(args, "{", ",", "}")
    }

    internal fun argumentsAsString(
            args: Map<String, Argument?>?,
            start: String,
            sep: String,
            end: String
    ): String {
        return if (args.isNullOrEmpty()) "" else {
            var answer = start
            args.forEach { kv ->
                val arg = kv.value!!
                answer = answer + arg.key + "=" + arg.value + sep  //IMPROVE define a function
            }
            val len = answer.length
            answer = answer.replaceRange(len - 1, len, end)
            answer
        }
    }

    internal fun argumentsAsList(
            args: Map<String, Argument?>?,
            start: String,
            sep: String,
            end: String
    ): String {
        return if (args.isNullOrEmpty()) "" else {
            var answer = start
            args.forEach { kv ->
                val arg = kv.value!!
                answer = answer + arg.key + ":" + arg.value + sep  //IMPROVE define a function
            }
            val len = answer.length
            answer = answer.replaceRange(len - 1, len, end)
            answer
        }
    }

    internal fun asBody(arg: Argument): String {
        var v = arg.value!!
        val isHttp = isUrl(v)
        v = quote(v)
        if (isHttp) {
            v = enbrace("href", v)
        }
        return quote(arg.key) + ": " + enbrace("value", v)
    }

    fun isUrl(s: String): Boolean {
        return s.startsWith("http")
    }

    private fun enbrace(k: String, v: String): String {
        return "{" + quote(k) + ": " + v + "}"
    }

    private fun quote(s: String): String {
        return "\"" + s + "\""
    }

    fun format(jsonStr: String): String {
        return JSON.stringify(jsonStr, null, 2)
    }

    fun extractTitle(title: String): String {
        val strList = title.split("/")
        val len = strList.size
        return if (len > 2) strList[len - 2] else ""
    }

    /**
     * https://discuss.kotlinlang.org/t/kotlin-native-base64-en-decoder-code/10043
     */
    private val BASE64_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    /**
     * Base64 encode a string.
     */
    fun base64encoded(s: String): String {
        val pad = when (s.length % 3) {
            1 -> "=="
            2 -> "="
            else -> ""
        }
        var raw = s
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

    fun shorten(url: String): String {
        var result = url
        val signature = Constants.restInfix
        if (url.contains(signature)) {
            // strip off protocol, host, port
            val protocolHostPort = SessionManager.getBaseUrl()
            result = result.replace(protocolHostPort + signature, "")
            result = StringUtils.removeHexCode(result)
        }
        return result
    }

}
