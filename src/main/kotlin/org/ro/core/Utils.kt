package org.ro.core

import org.ro.to.Argument
import org.ro.to.Link
import org.ro.to.TObject
import kotlin.js.Date

external fun encodeURIComponent(encodedURI: String): String

object Utils {

    fun isXml(input: String): Boolean {
        return input.startsWith("<") && input.endsWith(">")
    }

    fun enCamel(input: String): String {
        var output = ""
        val words = input.split(" ")
        for (w in words) {
            output = output + w.capitalize()
        }
        return decapitalize(output)
    }

    private fun decapitalize(input: String): String {
        val output = input.substring(1, input.length)
        return input.first().toLowerCase() + output
    }

    fun deCamel(input: String): String {
        var output = ""
        var i = 0
        for (c in input) {
            if (i == 0) {
                output += c.toUpperCase()
            } else {
                val o = if (c.toUpperCase() == c) {
                    " $c"
                } else {
                    c.toString()
                }
                output += o
            }
            i++
        }
        return output
    }

    fun removeHexCode(input: String): String {
        var output = ""
        val list: List<String> = input.split("/")
        //split string by "/" and remove parts longer than 40chars
        list.forEach { s ->
            output += "/"
            output += if (s.length < 40) {
                s
            } else {
                "..."
            }
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
            end: String): String {
        if (args.isNullOrEmpty()) {
            return ""
        } else {
            var answer = start
            args.forEach { kv ->
                val arg = kv.value!!
                answer = answer + arg.key + "=" + arg.value + sep  //IMPROVE define a function
            }
            val len = answer.length
            answer = answer.replaceRange(len - 1, len, end)
            return answer
        }
    }

    internal fun argumentsAsList(
            args: Map<String, Argument?>?,
            start: String,
            sep: String,
            end: String): String {
        if (args.isNullOrEmpty()) {
            return ""
        } else {
            var answer = start
            args.forEach { kv ->
                val arg = kv.value!!
                answer = answer + arg.key + ":" + arg.value + sep  //IMPROVE define a function
            }
            val len = answer.length
            answer = answer.replaceRange(len - 1, len, end)
            return answer
        }
    }

    internal fun asBody(arg: Argument): String {
        var v = arg.value!!
        val isHttp = v.startsWith("http")
        v = quote(v)
        if (isHttp) {
            v = enbrace("href", v)
        }
        return quote(arg.key) + ": " + enbrace("value", v)
    }

    private fun enbrace(k: String, v: String): String {
        return "{" + quote(k) + ": " + v + "}"
    }

    private fun quote(s: String): String {
        return "\"" + s + "\""
    }

    //TODO move to Value.init?
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

    fun format(jsonStr: String): String {
        val s1 = JSON.parse<String>(jsonStr)
        return JSON.stringify(s1, null, 2)
    }

}
