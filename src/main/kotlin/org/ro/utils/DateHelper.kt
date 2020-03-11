package org.ro.utils

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
