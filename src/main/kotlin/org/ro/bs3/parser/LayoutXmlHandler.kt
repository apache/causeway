package org.ro.bs3.parser

import org.ro.to.bs3.Bs3Object
import org.ro.bs3.parser.LayoutParser

/**
 * Delegates responses to handlers, acts as Facade.
 * @See: https://en.wikipedia.org/wiki/Chain-of-responsibility_pattern
 */
object LayoutXmlHandler {
    private var delegate: IXmlHandler;

    private var _0 = LayoutParser()
    private var _1 = TabHandler()
    private var _2 = RowHandler()
    private var _3 = TabGroupHandler()
    private var _4 = ColHandler()
    private var _5 = ClearFixHiddenHandler()
    private var _6 = ClearFixVisibleHandler()
    //Action
    //BookmarkPolicy
    //Collection
    private var _7 = DomainObjectHandler()
    //FieldSet
    //Grid
    private var _8 = LinkHandler()
    //Property
    //ServiceAction
    //SizeSpan

    //E CssClassFaPosition
    //E CssDisplay
    //E LabelPosition
    //E Position
    //E PrompStyle
    //E Size
    //E Where
    private var last = DefaultXmlHandler()

    init {
        _0.successor = _1
        _1.successor = _2
        _2.successor = _3
        _3.successor = _4
        _4.successor = _5
        _5.successor = _6
        _6.successor = _7
        _7.successor = _8
        _8.successor = last

        delegate = _1
    }

    fun parse(xmlStr: String): Bs3Object? {
        return delegate.parse(xmlStr)
    }

}
