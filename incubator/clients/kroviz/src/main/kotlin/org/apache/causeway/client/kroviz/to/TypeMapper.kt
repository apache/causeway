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
package org.apache.causeway.client.kroviz.to

import org.apache.causeway.client.kroviz.utils.XmlHelper

enum class ValueType(val type: String) {
    BUTTON("Button"),
    BOOLEAN("Boolean"),
    DATE("Date"),
    HTML("Html"),
    IFRAME("IFrame"),
    IMAGE("Image"),
    NUMERIC("Numeric"),
    PASSWORD("Password"),
    TEXT("Text"),
    TEXT_AREA("TextArea"),
    TIME("Time"),
    SIMPLE_SELECT("SimpleSelect"),
    SLIDER("Slider"),
    SHELL("Shell"),
    SVG_MAPPED("Map"),
    SVG_INLINE("Inline"),
    VEGA("Vega"),
}

class TypeMapper {

    fun match(member: Member): String {
        val mf = member.format
        val mex = member.extensions?.xCausewayFormat
        return when {
            mf == "int" -> ValueType.NUMERIC.type
            mf == "date" -> ValueType.DATE.type
            mf == "date-time" -> ValueType.TIME.type
            mf == "utc-millisec" -> ValueType.TIME.type
            mex == "boolean" -> ValueType.BOOLEAN.type
            //mex == "javasqltimestamp" -> TypeMapperType.DATE.type
            //mex == "javautildate" -> TypeMapperType.DATE.type
            else -> {
                match(member.value)
            }
        }
    }

    private val isoDate = Regex("^([+-]?\\d{4}(?!\\d{2}\\b))((-?)((0[1-9]|1[0-2])(\\3([12]\\d|0[1-9]|3[01]))?|W([0-4]\\d|5[0-2])(-?[1-7])?|(00[1-9]|0[1-9]\\d|[12]\\d{2}|3([0-5]\\d|6[1-6])))([T\\s]((([01]\\d|2[0-3])((:?)[0-5]\\d)?|24:?00)([.,]\\d+(?!:))?)?(\\17[0-5]\\d([.,]\\d+)?)?([zZ]|([+-])([01]\\d|2[0-3]):?([0-5]\\d)?)?)?)?\$")

    private fun match(value: Value?): String {
        val contentStr = value?.content.toString()
        return when {
            isoDate.matches(contentStr) -> ValueType.DATE.type
            XmlHelper.isXml(contentStr) -> ValueType.HTML.type
            contentStr.startsWith(":Notice:") -> ValueType.HTML.type
            contentStr.startsWith("link:") -> ValueType.HTML.type
            else -> ValueType.TEXT.type
        }
    }

    fun forType(type: String): ValueType {
        console.log("[TM_forType] $type")
        when (type) {
            ValueType.BOOLEAN.type -> return ValueType.BOOLEAN
            ValueType.DATE.type -> return ValueType.DATE
            ValueType.HTML.type -> return ValueType.HTML
            ValueType.IFRAME.type -> return ValueType.IFRAME
            ValueType.IMAGE.type -> return ValueType.IMAGE
            ValueType.NUMERIC.type -> return ValueType.NUMERIC
            ValueType.PASSWORD.type -> return ValueType.PASSWORD
            ValueType.TEXT.type -> return ValueType.TEXT
            ValueType.TEXT_AREA.type -> return ValueType.TEXT_AREA
            ValueType.TIME.type -> return ValueType.TIME
            ValueType.SIMPLE_SELECT.type -> return ValueType.SIMPLE_SELECT
            ValueType.SLIDER.type -> return ValueType.SLIDER
            ValueType.SVG_MAPPED.type -> return ValueType.SVG_MAPPED
            ValueType.SVG_INLINE.type -> return ValueType.SVG_INLINE
            ValueType.VEGA.type -> return ValueType.VEGA
            else -> {
                return ValueType.TEXT
            }
        }
    }

}
