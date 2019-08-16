package org.ro.core.model

import org.ro.to.Link
import org.ro.to.RelType
import org.ro.to.TObject

class ResultExposer(val delegate: TObject) : Exposer {

    fun dynamise(): dynamic {
        val that = this.asDynamic()
        // result
        var result = "dummy result"
        val link = selfLink(delegate)
        if (link != null) {
            result = link.resultTitle()
        }
        that["result"] = result

        // resultClass
        var resultClass = ""
        val member = delegate.getProperty("className")
        if (member != null) {
            val value = member.value
            if (value != null)
                resultClass = value.content.toString()
        }
        that["resultClass"] = resultClass

        // resultKey
        var resultKey = "dummy resultKey"
        if (link != null) {
            resultKey = link.resultKey()
        }
        that["resultKey"] = resultKey
        return that
    }

    var iconName = "fa-cube"  //TODO fixed value for FixtureResult only

    //TODO special handling required?

    var result: String
        set(arg: String) {
            console.log("[ResultExposer.result.set] tabulator requires setter $arg")
        }
        get() {
            var answer = "dummy result"
            val link = selfLink(delegate)
            if (link != null) {
                answer = link.resultTitle()
            }
            return answer
        }

    var resultClass: String
        set(arg: String) {
            console.log("[ResultExposer.resultClass.set] tabulator requires setter $arg")
        }
        get() {
            var answer = ""
            val member = delegate.getProperty("className")
            if (member != null) {
                val value = member.value
                if (value != null)
                    answer = value.content.toString()
            }
            return answer
        }

    var resultKey: String
        set(arg: String) {
            console.log("[ResultExposer.resultKey.set] tabulator requires setter $arg")
        }
        get() {
            var answer = "dummy resultKey"
            val link = selfLink(delegate)
            if (link != null) {
                answer = link.resultKey()
            }
            return answer
        }

    private fun selfLink(delegate: TObject): Link? {
        val answer = delegate.links.find {
            it.rel == RelType.SELF.type
        }
        return answer
    }

}
