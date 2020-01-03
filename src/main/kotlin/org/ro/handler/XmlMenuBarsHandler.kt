package org.ro.handler

import org.ro.core.Utils
import org.ro.to.TransferObject
import org.ro.to.XmlHelper
import org.ro.to.mb3.Menubars

class XmlMenuBarsHandler : BaseHandler(), IResponseHandler {

    override fun doHandle() {
        console.log("[XmlMenuBarsHandler.doHandle]")
        console.log(logEntry)

//        logEntry.addAggregator(RestfulAggregator())
        update()
    }

    override fun parse(response: String): TransferObject? {
        val isXml =  Utils.isXml(response)
        console.log("[XmlMenuBarsHandler.parse] isXml: $isXml")
        if (isXml) {
            val doc = XmlHelper().parseXml(response)
            console.log(doc)
            val menuBars = Menubars(doc)
            console.log(menuBars)
            return menuBars
        } else {
            throw Exception()
        }
    }

}
