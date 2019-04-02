package org.ro.core.model

import org.ro.core.Utils
import org.ro.layout.Layout
import org.ro.to.Extensions
import org.ro.to.Property
import org.ro.to.TObject

class ObjectList : Visible {
    private var limit: Int = 0
    var list = mutableListOf<ObjectAdapter>()
    private var layout: Layout? = null

    fun initSize(limit: Int) {
        this.limit = limit
    }

    fun hasLayout(): Boolean {
        return layout != null
    }

    //TODO can/should layout be encapsulated more?
    fun setLayout(layout: Layout) {
        this.layout = layout
        initPropertyDescription()
    }

    fun getLayout(): Layout? {
        return layout
    }

    fun last(): ObjectAdapter {
        return list[length() - 1]
    }

    fun length(): Int {
        return list.size
    }

    //TODO public for test only, reduce visibility 
    fun add(oa: ObjectAdapter) {
        if (oa.adaptee is TObject) {
            val tObj = oa.adaptee.unsafeCast<TObject>()
            tObj.addMembersAsProperties()
        }
        list.add(oa)
    }

    fun isFull(): Boolean {
        return length() >= limit
    }

    private fun initPropertyDescription() {
        if (layout != null) {
            if (layout!!.arePropertyLabelsToBeSet()) {
                val pls = layout!!.properties!!
                for (pl in pls) {
                    val l = pl.link
                    l!!.invoke()
                }
            }
        }
    }

    fun handleProperty(p: Property) {
        if (layout == null) {
            //TODO should not happen ...
            //           layout = Layout()
        }
        val e: Extensions? = p.extensions
        layout!!.addPropertyLabel(p.id, e!!.friendlyName)
    }

    override fun tag(): String {
        var title = "FIXME"
        //FIXME
        /*
        val obj = last()
        if (obj.hasOwnProperty("domainType")) {
            title = obj.domainType
        } else if (obj.hasOwnProperty("name")) {
            title = obj.name
        } else {
            title = "noClassnameNorName"
        }  */
        title = Utils.deCamel(title)
        return "$title (${length()})"
    }

}