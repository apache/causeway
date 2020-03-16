package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.core.model.DiagramDM
import org.ro.to.DomainType
import org.ro.to.DomainTypes
import org.ro.to.Link
import org.ro.to.Property
import org.ro.ui.RoStatusBar

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

