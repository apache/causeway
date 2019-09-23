package org.ro.to.bs3

import org.w3c.dom.*
import kotlin.dom.isElement

// See: https://github.com/Kotlin/kotlinx.dom/blob/master/src/main/kotlin.js/DomJS.kt

/** Searches for elements using the element name, an element ID (if prefixed with dot) or element class (if prefixed with #) */
fun Document?.search(selector: String): List<HTMLElement> {
    return this?.querySelectorAll(selector)?.asList()?.filterElements() ?: emptyList()
}

/** Searches for elements using the element name, an element ID (if prefixed with dot) or element class (if prefixed with #) */
fun Element.search(selector: String): List<HTMLElement> {
    return querySelectorAll(selector).asList().filterElements()
}

private class HTMLCollectionListView(val collection: HTMLCollection) : AbstractList<HTMLElement>() {
    override val size: Int get() = collection.length

    override fun get(index: Int): HTMLElement =
            when {
                index in 0..size - 1 -> collection.item(index) as HTMLElement
                else -> throw IndexOutOfBoundsException("index $index is not in range [0 .. ${size - 1})")
            }
}

fun HTMLCollection.asList(): List<HTMLElement> = HTMLCollectionListView(this)

private class DOMTokenListView(val delegate: DOMTokenList) : AbstractList<String>() {
    override val size: Int get() = delegate.length

    override fun get(index: Int) =
            when {
                index in 0..size - 1 -> delegate.item(index)!!
                else -> throw IndexOutOfBoundsException("index $index is not in range [0 .. ${size - 1})")
            }
}

fun DOMTokenList.asList(): List<String> = DOMTokenListView(this)
internal fun HTMLCollection.asElementList(): List<HTMLElement> = asList()

/*
@Suppress("UNCHECKED_CAST")
        /** Returns an [Iterator] of all the next [Element] siblings */
fun Node.nextElements(): List<HTMLElement> = nextSiblings().filter { it.isElement }.unsafeCast<List<HTMLElement>>()

@Suppress("UNCHECKED_CAST")
        /** Returns an [Iterator] of all the previous [Element] siblings */
fun Node.previousElements(): List<HTMLElement> = nextSiblings().filter { it.isElement } as List<HTMLElement>

private val Node.isElement: Boolean
    get() {}  */
var Element.id: String
    get() = this.getAttribute("id") ?: ""
    set(value) {
        this.setAttribute("id", value)
    }

@Suppress("UNCHECKED_CAST")
fun List<Node>.filterElements(): List<HTMLElement> = filter { it.isElement } as List<HTMLElement>

fun NodeList.filterElements(): List<HTMLElement> = asList().filterElements()
