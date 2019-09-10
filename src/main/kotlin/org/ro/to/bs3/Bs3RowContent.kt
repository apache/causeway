package org.ro.to.bs3

//@XmlSeeAlso(Col::class, Bs3ClearFix::class)
abstract class Bs3RowContent(
        open var size: Size,
        override var cssClass: String) : Bs3ElementAbstract(cssClass)
