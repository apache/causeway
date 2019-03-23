package org.ro.handler

import kotlinx.serialization.json.JSON
import org.ro.to.Member

class MemberHandler : AbstractHandler(), IResponseHandler {

    override fun canHandle(jsonStr: String): Boolean {
        var answer = false
        try {
            parse(jsonStr)
            answer = true
        } catch (ex: Exception) {
        }
        return answer
    }

    override fun doHandle(jsonStr: String) {
    }

    fun parse(jsonStr: String): Member {
        return JSON.parse(Member.serializer(), jsonStr)
    }
}