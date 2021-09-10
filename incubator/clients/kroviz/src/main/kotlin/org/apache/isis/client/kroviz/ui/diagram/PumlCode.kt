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

class PumlCode() {

    private val NL = "\n"

    var code = ""

    fun add(s: String): PumlCode {
        code += s
        return this
    }

    fun addLine(s: String): PumlCode {
        code += s + NL
        return this
    }

    fun addStereotype(s: String): PumlCode {
        var result = "<<" + s + ">>"
        result = italic(result)
        result = center(result)
        code += result + NL
        return this
    }

    fun addLink(url: String, title: String): PumlCode {
        var result = "[[" + url + " "+ title + "]]"
        result = bold(result)
        code += result + NL
        return this
    }

    fun addClass(s: String): PumlCode {
        val result = underline("(C) "+ s)
        code += result + NL
        return this
    }

    fun toMindmap(): PumlCode {
        code += "@startmindmap$NL" + code + "@endmindmap$NL"
        return this
    }

    fun toMindmapNode(level:Int): PumlCode {
        val depth = "*".repeat(level)
        code = depth + ":" + code + ";" + NL
        return this
    }

    private fun center(s: String): String {
        return ".." + s + ".."
    }

    private fun italic(s: String): String {
        return "//" + s + "//"
    }

    private fun bold(s: String): String {
        return "**" + s + "**"
    }

    private fun underline(s: String): String {
        return "__" + s + "__"
    }

    fun addHorizontalLine(): PumlCode {
        code += "----" + NL
        return this
    }

    fun trim(): PumlCode {
        if (code.endsWith(NL)) {
            code = code.dropLast(1)
        }
        return this
    }

}
