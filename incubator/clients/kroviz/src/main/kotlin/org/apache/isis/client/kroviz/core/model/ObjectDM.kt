package org.apache.isis.client.kroviz.core.model

import org.apache.isis.client.kroviz.core.aggregator.BaseAggregator
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.core.event.RoXmlHttpRequest
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Method
import org.apache.isis.client.kroviz.to.TObject
import org.apache.isis.client.kroviz.to.TransferObject

class ObjectDM(override val title: String) : DisplayModelWithLayout() {
    var data: Exposer? = null
    private var dirty: Boolean = false

    override fun canBeDisplayed(): Boolean {
        return when {
            isRendered -> false
            layout == null -> false
            grid == null -> false
            else -> {
                true
            }
        }
    }

    fun setDirty(value: Boolean) {
        dirty = value
    }

    override fun addData(obj: TransferObject) {
        val exo = Exposer(obj as TObject)
        data = exo.dynamise() as? Exposer
    }

    override fun getObject(): TObject? {
        return (data as Exposer).delegate
    }

    fun save() {
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
            putLink.invokeWith(aggregator)

            // now data should be reloaded - wait for invoking PUT?
            getLink.invokeWith(aggregator)
            //refresh of display to be triggered?
        }
    }

    fun undo() {
        if (dirty) {
            //TODO reset()
        }
    }

    fun Link.invokeWith(aggregator: BaseAggregator) {
        RoXmlHttpRequest().invoke(this, aggregator)
    }

}
