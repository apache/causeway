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

import org.apache.isis.client.kroviz.utils.XmlHelper

enum class ValueType(val type: String) {
    BUTTON("Button"),
    BOOLEAN("Boolean"),
    DATE("Date"),
    HTML("Html"),
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
    IFRAME("IFrame")
}

class TypeMapper {

    fun match(member: Member): String {
        val mf = member.format
        val mex = member.extensions?.xIsisFormat
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
            else -> ValueType.TEXT.type
        }
    }

    fun forType(type: String): ValueType {
        when {
            type == ValueType.BOOLEAN.type -> return ValueType.BOOLEAN
            type == ValueType.DATE.type -> return ValueType.DATE
            type == ValueType.HTML.type -> return ValueType.HTML
            type == ValueType.IMAGE.type -> return ValueType.IMAGE
            type == ValueType.NUMERIC.type -> return ValueType.NUMERIC
            type == ValueType.PASSWORD.type -> return ValueType.PASSWORD
            type == ValueType.TEXT.type -> return ValueType.TEXT
            type == ValueType.TEXT_AREA.type -> return ValueType.TEXT_AREA
            type == ValueType.TIME.type -> return ValueType.TIME
            type == ValueType.SIMPLE_SELECT.type -> return ValueType.SIMPLE_SELECT
            type == ValueType.SLIDER.type -> return ValueType.SLIDER
            type == ValueType.SVG_MAPPED.type -> return ValueType.SVG_MAPPED
            type == ValueType.SVG_INLINE.type -> return ValueType.SVG_INLINE
            type == ValueType.IFRAME.type -> return ValueType.IFRAME
            else -> {
                return ValueType.TEXT
            }
        }
    }

}
