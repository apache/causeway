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
package org.apache.isis.client.kroviz.core.model

import org.apache.isis.client.kroviz.core.event.ResourceProxy
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.to.*
import org.apache.isis.client.kroviz.ui.core.SessionManager
import org.apache.isis.client.kroviz.ui.core.UiManager

class ObjectDM(override val title: String) : DisplayModelWithLayout() {
    var data: Exposer? = null
    val collections = mutableMapOf<String, CollectionDM>()
    private var dirty: Boolean = false

    override fun canBeDisplayed(): Boolean {
        //       debug()
        return when {
            isRendered -> false
            (layout == null) && (grid == null) -> false
            else -> true
        }
    }

    private fun debug() {
        console.log("[]")
        console.log("[ODM.debug] data / collections / layout / grid / properties / icon / aggregator / logEntries")
        console.log(data)
        console.log(collections)
        console.log(layout)
        console.log(grid)
        console.log(properties)
        console.log(icon)
        if (data != null) {
            val delegate = (data as Exposer).delegate
            val selfLink = delegate.getSelfLink()
            val rs = ResourceSpecification(selfLink.href)
            val es = SessionManager.getEventStore()
            val le = es.findBy(rs)!!
            val aggt = le.getAggregator()
            console.log(aggt)
            val logEntries = es.findAllBy(aggt)
            logEntries.forEach {
                console.log(it)
            }
        }
    }

    fun setDirty(value: Boolean) {
        dirty = value
    }

    fun addCollection(key: String, value: CollectionDM) {
        collections.put(key, value)
    }

    override fun addData(obj: TransferObject) {
        (obj as TObject)
        val exo = Exposer(obj)
        data = exo.dynamise() as? Exposer
        obj.getProperties().forEach { m ->
            val p = createPropertyFrom(m)
            addProperty(p)
        }
    }

    fun addResult(resultObject: ResultObject) {
        val tObj = createObjectFrom(resultObject)
        this.addData(tObj)
    }

    override fun getObject(): TObject {
        return (data as Exposer).delegate
    }

    fun save() {
        if (dirty) {
            val tObject = data!!.delegate
            val getLink = tObject.links.first()
            val href = getLink.href
            val reSpec = ResourceSpecification(href)
            val es = SessionManager.getEventStore()
            //WATCHOUT this is sequence dependent: GET and PUT share the same URL - if called after PUTting, it may fail
            val getLogEntry = es.findBy(reSpec)!!
            getLogEntry.setReload()

            val putLink = Link(method = Method.PUT.operation, href = href)
            val logEntry = es.findBy(reSpec)
            val aggregator = logEntry?.getAggregator()!!
            // there may be more than one aggt - which may break this code

            ResourceProxy().fetch(putLink, aggregator)
            // now data should be reloaded
            ResourceProxy().fetch(getLink, aggregator)
        }
    }

    fun undo() {
        if (dirty) {
            reset()
        }
    }

    private fun createPropertyFrom(m: Member): Property {
        return Property(
            id = m.id,
            memberType = m.memberType,
            links = m.links,
            optional = m.optional,
            title = m.id,
            value = m.value,
            extensions = m.extensions,
            format = m.format,
            disabledReason = m.disabledReason
        )
    }

    private fun createObjectFrom(resultObject: ResultObject): TObject {
        val r = resultObject.result!!
        return TObject(
            links = r.links,
            extensions = r.extensions!!,
            title = r.title,
            domainType = r.domainType,
            instanceId = r.instanceId.toString(),
            members = r.members
        )
    }

}
