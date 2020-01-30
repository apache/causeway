package org.ro.to

import org.ro.core.Utils

enum class ValueType(val type: String) {
    BOOLEAN("Boolean"),
    DATE("Date"),
    HTML("Html"),
    NUMERIC("Numeric"),
    PASSWORD("Password"),
    TEXT("Text"),
    TEXT_AREA("TextArea"),
    TIME("Time"),
    SIMPLE_SELECT("SimpleSelect")
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

    private val ISO_DATE = Regex("^([\\+-]?\\d{4}(?!\\d{2}\\b))((-?)((0[1-9]|1[0-2])(\\3([12]\\d|0[1-9]|3[01]))?|W([0-4]\\d|5[0-2])(-?[1-7])?|(00[1-9]|0[1-9]\\d|[12]\\d{2}|3([0-5]\\d|6[1-6])))([T\\s]((([01]\\d|2[0-3])((:?)[0-5]\\d)?|24\\:?00)([\\.,]\\d+(?!:))?)?(\\17[0-5]\\d([\\.,]\\d+)?)?([zZ]|([\\+-])([01]\\d|2[0-3]):?([0-5]\\d)?)?)?)?\$")
    private val JAVA_LANG_LONG = Regex("\\d{19}")  //9223372036854775807
//    private val JAVA_OFFSET_DATE_TIME = Regex("\\d{19}")  //20200125T140705.356+0100

    private fun match(value: Value?): String {
        val contentStr = value?.content.toString()
        return when {
            ISO_DATE.matches(contentStr) -> ValueType.DATE.type
//            JAVA_LANG_LONG.matches(contentStr) -> TypeMapperType.NUMERIC.type
            Utils.isXml(contentStr) -> ValueType.HTML.type
            else -> ValueType.TEXT.type
        }
    }

}
