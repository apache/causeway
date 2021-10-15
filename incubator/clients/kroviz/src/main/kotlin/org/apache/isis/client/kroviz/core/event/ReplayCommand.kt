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
package org.apache.isis.client.kroviz.core.event

import io.kvision.utils.createInstance
import org.apache.isis.client.kroviz.core.aggregator.BaseAggregator
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Represention
import org.apache.isis.client.kroviz.ui.core.RoApp

class ReplayCommand {

    fun execute() {
        val events = EventStore.log
        EventStore.reset()
        RoApp.reset()

        console.log("[ReplayCommand.execute]")
        console.log(events)
        val userActions = events.filter {
            it.type == Represention.HOMEPAGE.name ||
                    it.type == Represention.OBJECT_ACTION.name
        }

        userActions.forEach {
            val link = Link(href = it.url)
            val clazz = it.getAggregator()::class
            val aggregator = clazz.createInstance<BaseAggregator>()
            //eventually put a thinkTime here
            ResourceProxy().fetch(link,aggregator, it.subType)
        }

    }
}