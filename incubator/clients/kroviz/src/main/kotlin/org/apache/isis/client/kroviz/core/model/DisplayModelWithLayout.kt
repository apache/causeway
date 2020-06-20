package org.apache.isis.client.kroviz.core.model

import org.apache.isis.client.kroviz.layout.Layout
import org.apache.isis.client.kroviz.layout.PropertyLt
import org.apache.isis.client.kroviz.layout.RowLt
import org.apache.isis.client.kroviz.to.Extensions
import org.apache.isis.client.kroviz.to.Property
import org.apache.isis.client.kroviz.to.bs3.Grid

abstract class DisplayModelWithLayout : DisplayModel() {

    var layout: Layout? = null
    var grid: Grid? = null
    var propertyDescriptionList = mutableMapOf<String, String>()
    var propertyList = mutableListOf<Property>()
    var propertyLayoutList = mutableListOf<PropertyLt>()

    override fun canBeDisplayed(): Boolean {
        return when {
            isRendered -> false
            layout == null -> false
            grid == null -> false
            else -> {
                val pls = propertyLayoutList.size
                val pds = propertyDescriptionList.size
                val descriptionsComplete = pds >= pls
                console.log("[DMWL.canBeDisplayed] $descriptionsComplete")
                console.log(this)
                descriptionsComplete
            }
        }
    }

    fun addLayout(layout:Layout) {
        this.layout = layout
        initPropertyLayoutList(layout)
    }

    private fun initPropertyLayoutList(layout:Layout) {
        layout.row.forEach { r ->
            initLayout4Row(r)
        }
    }

    private fun initLayout4Row(r:RowLt) {
        r.cols.forEach { cs ->
            val c = cs.getCol()
            c.fieldSet.forEach { fs ->
                propertyLayoutList.addAll(fs.property)
            }
            c.tabGroup.forEach { tg ->
                tg.tab.forEach { t ->
                    t.row.forEach { r2 ->
                        initLayout4Row(r2)
                    }
                }
            }
        }
    }

   fun addPropertyDescription(p:Property) {
        val id = p.id
        val e:Extensions = p.extensions!!
        val friendlyName = e.friendlyName
        addPropertyDescription(id, friendlyName)
    }

    fun addPropertyDescription(key: String, value: String) {
        propertyDescriptionList.put(key, value)
    }

    fun addProperty(property:Property) {
        propertyList.add(property)
    }

}
