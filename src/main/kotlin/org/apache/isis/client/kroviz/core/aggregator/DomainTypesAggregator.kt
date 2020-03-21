package org.apache.isis.client.kroviz.core.aggregator

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.model.DiagramDM
import org.apache.isis.client.kroviz.to.DomainType
import org.apache.isis.client.kroviz.to.DomainTypes
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Property
import org.apache.isis.client.kroviz.ui.kv.RoStatusBar


class DomainTypesAggregator(val url: String) : BaseAggregator() {

    init {
        dsp = DiagramDM(url)
    }

    override fun update(logEntry: LogEntry, subType: String) {
        when (val obj = logEntry.getTransferObject()) {
            is DomainTypes -> handleDomainTypes(obj)
            is DomainType -> handleDomainType(obj)
            is Property -> handleProperty(obj)
            else -> log(logEntry)
        }

        if (dsp.canBeDisplayed()) {
            RoStatusBar.updateDiagram(dsp as DiagramDM)
        }
    }

    private fun handleProperty(obj: Property) {
        dsp.addData(obj)
    }

    private fun handleDomainType(obj: DomainType) {
        if (obj.isPrimitiveOrService()) {
            (dsp as DiagramDM).decNumberOfClasses()
        } else {
            dsp.addData(obj)
            val propertyList = obj.members.filter {
                it.isProperty()
            }
            (dsp as DiagramDM).incNumberOfProperties(propertyList.size)
            propertyList.forEach { p ->
                p.invokeWith(this)
            }
        }
    }

    private fun handleDomainTypes(obj: DomainTypes) {
        val domainTypeLinkList = mutableListOf<Link>()
        obj.values.forEach { link ->
            when {
                link.href.contains("/org.apache.isis") -> noop()
                link.href.contains("/isisApplib") -> noop()
                link.href.contains("/java") -> noop()
                link.href.contains("/void") -> noop()
                link.href.contains("/boolean") -> noop()
                link.href.contains("fixture") -> noop()
                link.href.contains("service") -> noop()
                link.href.contains("/homepage") -> noop()
                link.href.endsWith("Menu") -> noop()
                else -> {
                    domainTypeLinkList.add(link)
                }
            }
        }
        (dsp as DiagramDM).numberOfClasses = domainTypeLinkList.size
        domainTypeLinkList.forEach {
            it.invokeWith(this)
        }
    }

    fun DomainType.isPrimitiveOrService(): Boolean {
        val primitives = arrayOf("void", "boolean", "double", "byte", "long", "char", "float", "short", "int")
        return (primitives.contains(canonicalName) || extensions.isService)
    }

}

