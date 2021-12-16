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
import org.apache.isis.client.kroviz.to.*
import org.apache.isis.client.kroviz.ui.core.ViewManager

class DomainTypesAggregator(val url: String) : BaseAggregator() {

    init {
        dpm = DiagramDM(url)
    }

    override fun update(logEntry: LogEntry, subType: String) {
        when (val obj = logEntry.getTransferObject()) {
            is DomainTypes -> handleDomainTypes(obj)
            is DomainType -> handleDomainType(obj)
            is Property -> handleProperty(obj)
            is Action -> handleAction(obj)
            else -> log(logEntry)
        }

        if (dpm.canBeDisplayed()) {
            ViewManager.getRoStatusBar().updateDiagram(dpm as DiagramDM)
            dpm.isRendered = true
        }
    }

    private fun handleProperty(obj: Property) {
        dpm.addData(obj)
    }

    private fun handleAction(obj: Action) {
        console.log("[DTA.handleAction] $obj")
        throw Throwable("[DomainTypesAggregator.handleAction] not implemented yet")  //dsp.addData(obj)
    }

    private fun handleDomainType(obj: DomainType) {
        if (obj.isPrimitiveOrService()) {
            (dpm as DiagramDM).decNumberOfClasses()
        } else {
            dpm.addData(obj)
            val propertyList = obj.members.filter {
                it.value.isProperty()
            }
            (dpm as DiagramDM).incNumberOfProperties(propertyList.size)
            propertyList.forEach {
                invoke(it.value, this, referrer = "")
            }
        }
    }

    private fun handleDomainTypes(obj: DomainTypes) {
        val domainTypeLinkList = mutableListOf<Link>()
        obj.values.forEach { link ->
            val it = link.href
            when {
                it.contains("/org.apache.isis") -> {
                }
                it.contains("/isisApplib") -> {
                }
                it.contains("/java") -> {
                }
                it.contains("/void") -> {
                }
                it.contains("/boolean") -> {
                }
                it.contains("fixture") -> {
                }
                it.contains("service") -> {
                }
                it.contains("/homepage") -> {
                }
                it.endsWith("Menu") -> {
                }
                it.startsWith("demoapp.dom.annot") -> {
                }
                it.startsWith("demoapp.dom.types.javatime") -> {
                }
                else -> {
                    domainTypeLinkList.add(link)
                }
            }
        }
        (dpm as DiagramDM).numberOfClasses = domainTypeLinkList.size
        domainTypeLinkList.forEach {
            invoke(it, this, referrer = "")
        }
    }

    private fun DomainType.isPrimitiveOrService(): Boolean {
        val primitives = arrayOf("void", "boolean", "double", "byte", "long", "char", "float", "short", "int")
        return (primitives.contains(canonicalName) || extensions.isService)
    }

}

