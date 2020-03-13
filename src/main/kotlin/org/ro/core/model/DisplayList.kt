package org.ro.core.model

import org.ro.layout.Layout
import org.ro.layout.PropertyLt
import org.ro.layout.RowLt
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
        // row[0] (head) contains the object title and actions
        // row[1] contains data, tabs, collections, etc.
        val secondRow = layout.row[1] // traditional C braintwist
        var colsLyt = secondRow.cols.first()
        var colLyt = colsLyt.getCol()
        val tgLyts = colLyt.tabGroup
        if (tgLyts.isNotEmpty()) {
            val tabGroup = tgLyts.first()
            val tab = tabGroup.tab.first()
            val row = tab.row.first()
            colsLyt = row.cols.first()
        }
        colLyt = colsLyt.getCol()
        val fsList = colLyt.fieldSet
        if (fsList.isNotEmpty()) {
            val fsLyt = fsList.first()
            propertyLayoutList.addAll(fsLyt.property)
        }
        console.log("[DL.addLayout]")
        console.log(propertyLayoutList)
    }

    fun addLayoutNew(layout: Layout) {
        this.layout = layout
        layout.row.forEach { r ->
            addLayout4Row(r)
        }
    }

    private fun addLayout4Row(r: RowLt) {
        r.cols.forEach { cs ->
            cs.getColList().forEach { c ->
                c.fieldSet.forEach { fs ->
                    propertyLayoutList.addAll(fs.property)
                }
            }
        }
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
