package org.ro.core

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

}
