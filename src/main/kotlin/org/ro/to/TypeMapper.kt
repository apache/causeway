package org.ro.to

import org.ro.core.Utils

class TypeMapper {

    val BOOLEAN = "Boolean"
    val DATE = "Date"
    val HTML = "Html"
    val NUMERIC = "Numeric"
    val TEXT = "Text"
    val TEXT_AREA = "TextArea"

    fun match(member: Member): String {
        val mf = member.format
        val mex = member.extensions?.xIsisFormat
        return when {
            mf == "int" -> NUMERIC
            mf == "date" -> DATE
            mf == "date-time" -> DATE
            mex == "boolean" -> BOOLEAN
            else -> {
               match(member.value)
            }
        }
    }

    private val ISO_DATE = Regex("/^([\\+-]?\\d{4}(?!\\d{2}\\b))((-?)((0[1-9]|1[0-2])(\\3([12]\\d|0[1-9]|3[01]))?|W([0-4]\\d|5[0-2])(-?[1-7])?|(00[1-9]|0[1-9]\\d|[12]\\d{2}|3([0-5]\\d|6[1-6])))([T\\s]((([01]\\d|2[0-3])((:?)[0-5]\\d)?|24\\:?00)([\\.,]\\d+(?!:))?)?(\\17[0-5]\\d([\\.,]\\d+)?)?([zZ]|([\\+-])([01]\\d|2[0-3]):?([0-5]\\d)?)?)?)?\$")
//    private val JAVA_LANG_LONG = Regex("\\d{19}")  //9223372036854775807

    private fun match(value: Value?): String {
        val contentStr = value?.content.toString()
        return when {
            ISO_DATE.matches(contentStr) -> DATE
 //           JAVA_LANG_LONG.matches(contentStr) -> NUMERIC
            Utils.isXml(contentStr) -> HTML
            else -> TEXT
        }
    }

}
