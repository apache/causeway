package org.ro.bs3.parser

import org.ro.to.bs3.Bs3Object

interface IXmlHandler {
    fun handle(xmlStr: String)
    fun canHandle(xmlStr: String): Boolean
    fun doHandle()
    fun parse(xmlStr: String): Bs3Object?
}

