package org.ro.core.model

import org.ro.layout.Layout
import org.ro.layout.PropertyLt
import org.ro.to.Extensions
import org.ro.to.Property
import org.ro.to.TObject
import org.ro.to.TransferObject
import org.ro.to.bs3.Grid
import pl.treksoft.kvision.state.observableListOf

class DisplayList(override val title: String) : BaseDisplayable() {
    var data = observableListOf<Exposer>()
    override var layout: Layout? = null
    var grid: Grid? = null
    var propertyDescriptionList = mutableMapOf<String, String>()
    var propertyList = mutableListOf<Property>()
    var propertyLayoutList = mutableListOf<PropertyLt>()

    override fun canBeDisplayed(): Boolean {
        when {
            isRendered -> return false
            layout == null -> return false
            grid == null -> return false
            else -> {
                val lps = propertyList.size
                val pds = propertyDescriptionList.size
                //  val ps =  propertyList.size
                val descriptionsComplete = lps <= pds
                //  val propertiesComplete = lps <= ps
                console.log("[DL.canBeDisplayed] layout.properties: $lps")
                console.log("[DL.canBeDisplayed] propertyDescriptions: $pds")
                //   console.log("[DL.canBeDisplayed] properties: $ps")
                return descriptionsComplete //&& propertiesComplete
            }
        }
    }

    fun addLayout(layout: Layout) {
        this.layout = layout
        layout.row.forEach { r ->
            r.cols.forEach { cs ->
                cs.col.fieldSet.forEach { fs ->
                    fs.property.forEach { p ->
                        propertyLayoutList.add(p)
                    }
                }
                cs.col.row.forEach { r ->
                    r.cols.forEach { cs ->
                        cs.col.fieldSet.forEach { fs ->
                            fs.property.forEach { p ->
                                propertyLayoutList.add(p)
                            }
                        }
                    }
                }
            }
        }
        console.log("[DL.addLayout]")
        console.log(propertyLayoutList)
    }

    override fun addData(obj: TransferObject) {
        val exo = Exposer(obj as TObject)
        data.add(exo.dynamise())  //if exposer is not dynamised, data access in tables won't work
    }

    fun addPropertyDescription(p: Property) {
        val id = p.id
        val e: Extensions = p.extensions!!
        val friendlyName = e.friendlyName
        propertyDescriptionList.put(id, friendlyName)
    }

    fun addProperty(property: Property) {
        propertyList.add(property)
    }

    override fun reset() {
        isRendered = false
        data = observableListOf<Exposer>()
    }

}
