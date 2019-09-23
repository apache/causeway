package org.ro.org.ro.bs3.parser

import org.ro.to.TransferObject

interface XmlHandler {
    fun handle(xmlStr: String)
    fun canHandle(xmlStr: String): Boolean
    fun doHandle()
    fun parse(xmlStr: String): TransferObject?
}

