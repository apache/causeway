package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.to.Member

class MemberHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        var answer = false
        try {
            val obj = parse(jsonStr)
            logEntry.setObj(obj)
            answer = true
        } catch (ex: Exception) {
        }
        return answer
    }

    override fun doHandle() {
        console.log("[MemberHandler.doHandle()] has no body")
    }

    fun parse(jsonStr: String): Member {
        return JSON.parse(Member.serializer(), jsonStr)
    }
}