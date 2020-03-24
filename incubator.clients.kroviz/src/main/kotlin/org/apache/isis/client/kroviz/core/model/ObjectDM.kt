package org.apache.isis.client.kroviz.core.model

import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.to.Method

class ObjectDM(override val title: String) : DisplayModelWithLayout() {
    var data: Exposer? = null
    private var dirty: Boolean = false

    fun setDirty(value: Boolean) {
        console.log("[ObjectDM.setDirty] $value")
        dirty = value
    }

    override fun addData(obj:org.apache.isis.client.kroviz.to.TransferObject) {
        val exo = Exposer(obj as org.apache.isis.client.kroviz.to.TObject)
        data = exo.dynamise() as? Exposer
    }

    override fun getObject():org.apache.isis.client.kroviz.to.TObject? {
        return (data as Exposer).delegate
    }

    fun save() {
        console.log("[ObjectDM.save]")
        if (dirty) {
            val tObject = data!!.delegate
            val getLink = tObject.links.first()
            val href = getLink.href
            val reSpec = ResourceSpecification(href)
            //WATCHOUT this is sequence dependent: GET and PUT share the same URL - if called after PUTting, it may fail
            val getLogEntry = EventStore.find(reSpec)!!
            getLogEntry.setReload()

            val putLink =org.apache.isis.client.kroviz.to.Link(method = Method.PUT.operation, href = href)
            val logEntry = EventStore.find(reSpec)
            val aggregator = logEntry?.getAggregator()!!
            aggregator.invokeWith(putLink)

            // now data should be reloaded - wait for invoking PUT?
            aggregator.invokeWith(getLink)
            //refresh of display to be triggered?
        }
    }

    fun undo() {
        console.log("[displayModel.undo]")
        if (dirty) {
            //TODO reset()
        }
    }

}
