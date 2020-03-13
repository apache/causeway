package org.ro.core.event

import org.ro.utils.Utils

class ResourceSpecification(
        val url: String,
        val subType: String = "json") {

    fun isRedundant(): Boolean {
        return when {
            url.contains("object-layout") -> true
            url.contains("/properties/") -> true
            else -> false
        }
    }

    fun matches(logEntry: LogEntry): Boolean {
        return subType.equals(logEntry.subType)
                && areEquivalent(url, logEntry.url)
    }

    private fun areEquivalent(searchUrl: String, compareUrl: String, allowedDiff: Int = 1): Boolean {
        val sl = Utils.removeHexCode(searchUrl)
        val cl = Utils.removeHexCode(compareUrl)
        val searchList: List<String> = sl.split("/")
        val compareList: List<String> = cl.split("/")
        if (compareList.size != searchList.size) {
            return false
        }

        var diffCnt = 0
        for ((i, s) in searchList.withIndex()) {
            val c = compareList[i]
            if (s != c) {
                diffCnt++
                val n = s.toIntOrNull()
                // if the difference is a String, it is not allowed and counts double
                if (n == null) {
                    diffCnt++
                }
            }
        }
        return diffCnt <= allowedDiff
    }

}
