/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
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
            dsp.isRendered = true
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

    private fun DomainType.isPrimitiveOrService(): Boolean {
        val primitives = arrayOf("void", "boolean", "double", "byte", "long", "char", "float", "short", "int")
        return (primitives.contains(canonicalName) || extensions.isService)
    }

}

