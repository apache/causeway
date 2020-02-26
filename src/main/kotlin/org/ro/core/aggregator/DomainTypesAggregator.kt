package org.ro.core.aggregator

import org.ro.core.event.LogEntry
import org.ro.core.model.DiagramDisplay
import org.ro.to.DomainType
import org.ro.to.DomainTypes
import org.ro.to.Link
import org.ro.to.Property
import org.ro.ui.RoStatusBar

class DomainTypesAggregator(val url: String) : BaseAggregator() {

    init {
        dsp = DiagramDisplay(url)
    }

    override fun update(logEntry: LogEntry) {
        when (val obj = logEntry.getTransferObject()) {
            is DomainTypes -> handleDomainTypes(obj)
            is DomainType -> handleDomainType(obj)
            is Property -> handleProperty(obj)
            else -> log(logEntry)
        }

        if (dsp.canBeDisplayed()) {
            RoStatusBar.updateDiagram(dsp as DiagramDisplay)
        }
    }

    private fun handleProperty(obj: Property) {
        dsp.addData(obj)
    }

    private fun handleDomainType(obj: DomainType) {
        if (obj.isPrimitiveOrService()) {
            (dsp as DiagramDisplay).decNumberOfClasses()
        } else {
            dsp.addData(obj)
            val propertyList = obj.members.filter {
                it.isProperty()
            }
            (dsp as DiagramDisplay).incNumberOfProperties(propertyList.size)
            propertyList.forEach { p ->
                invoke(p)
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
        (dsp as DiagramDisplay).numberOfClasses = domainTypeLinkList.size
        domainTypeLinkList.forEach {
            invoke(it)
        }
    }

    fun DomainType.isPrimitiveOrService(): Boolean {
        val primitives = arrayOf("void", "boolean", "double", "byte", "long", "char", "float", "short", "int")
        return (primitives.contains(canonicalName) || extensions.isService)
    }

}

