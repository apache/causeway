package org.ro.core.model

import org.ro.core.event.EventStore
import org.ro.layout.Layout
import org.ro.core.event.ResourceSpecification
import org.ro.to.Link
import org.ro.to.Method
import org.ro.to.TObject
import org.ro.to.TransferObject

class DisplayObject(override val title: String) : BaseDisplayable() {
    var data: Exposer? = null
    override var layout: Layout? = null
    private var dirty: Boolean = false

    fun setDirty(value: Boolean) {
        console.log("[DisplayObject.setDirty] $value")
        dirty = value
    }

    override fun canBeDisplayed(): Boolean {
        when {
            layout == null -> return false
            else -> return !isRendered
        }
    }

    fun addLayout(layout:Layout) {
        this.layout = layout
    }

    override fun addData(obj: TransferObject) {
        val exo = Exposer(obj as TObject)
        data = exo.dynamise() as? Exposer
    }

    override fun getObject(): TObject? {
        return (data as Exposer).delegate
    }

    fun save() {
        console.log("[DisplayObject.save]")
        if (dirty) {
            val tObject = data!!.delegate
            val getLink = tObject.links.first()
            val href = getLink.href
            val reSpec = ResourceSpecification(href)
            //WATCHOUT this is sequence dependent: GET and PUT share the same URL - if called after PUTting, it may fail
            val getLogEntry = EventStore.find(reSpec)!!
            getLogEntry.setReload()

            val putLink = Link(method = Method.PUT.operation, href = href)
            val logEntry = EventStore.find(reSpec)
            val aggregator = logEntry?.getAggregator()!!
            aggregator.invokeWith(putLink)

            // now data should be reloaded - wait for invoking PUT?
            aggregator.invokeWith(getLink)
            //refresh of display to be triggered?
        }
    }

    fun undo() {
        console.log("[DisplayObject.undo]")
        if (dirty) {
            //TODO reset()
        }
    }

}
