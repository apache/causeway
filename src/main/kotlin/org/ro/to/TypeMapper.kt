package org.ro.to

import org.ro.core.Utils

enum class TypeMapperType(val type: String) {
    BOOLEAN("Boolean"),
    DATE("Date"),
    HTML("Html"),
    NUMERIC("Numeric"),
    TEXT("Text"),
    TEXT_AREA("TextArea")
}

class TypeMapper {

    fun match(member: Member): String {
        val mf = member.format
        val mex = member.extensions?.xIsisFormat
        return when {
            mf == "int" -> TypeMapperType.NUMERIC.type
            mf == "date" -> TypeMapperType.DATE.type
            mf == "date-time" -> TypeMapperType.DATE.type
            mex == "boolean" -> TypeMapperType.BOOLEAN.type
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
            ISO_DATE.matches(contentStr) -> TypeMapperType.DATE.type
            //           JAVA_LANG_LONG.matches(contentStr) -> NUMERIC
            Utils.isXml(contentStr) -> TypeMapperType.HTML.type
            else -> TypeMapperType.TEXT.type
        }
    }

}
