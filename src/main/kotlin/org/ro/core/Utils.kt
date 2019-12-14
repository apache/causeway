package org.ro.core

import org.ro.to.Argument
import org.ro.to.Link

object Utils {

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
        for (s in list) {
            output += "/"
            output += if (s.length < 40) {
                s
            } else {
                "..."
            }
        }
        return output
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

}
