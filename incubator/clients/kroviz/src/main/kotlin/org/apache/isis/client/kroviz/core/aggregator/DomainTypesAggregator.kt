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
                link.href.contains("/org.apache.isis") -> {}
                link.href.contains("/isisApplib") -> {}
                link.href.contains("/java") -> {}
                link.href.contains("/void") -> {}
                link.href.contains("/boolean") -> {}
                link.href.contains("fixture") -> {}
                link.href.contains("service") -> {}
                link.href.contains("/homepage") -> {}
                link.href.endsWith("Menu") -> {}
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

