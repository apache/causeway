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

import org.apache.isis.client.kroviz.core.aggregator.AggregatorWithLayout
import org.apache.isis.client.kroviz.main
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Relation
import org.apache.isis.client.kroviz.to.Represention

class ReplayCommand {

    fun execute() {
        val events = copyEvents(EventStore.log)
        EventStore.reset()
        main() // re-creates the UI, but keeps the UiManager(singleton/object) and the session

        val userActions = filterUserActions(events)
        replay(userActions)
    }

    private fun replay(userActions: List<LogEntry>) {
        console.log("[ReplayCommand.replay]")
        userActions.forEach {
            val link = Link(href = it.url)
            console.log(link)
            ResourceProxy().fetch(link, null, it.subType)
        }
    }

    private fun filterUserActions(events: List<LogEntry>): List<LogEntry> {
        console.log("[ReplayCommand.filterUserActions]")
        val userActions = events.filter {
            (it.type == Represention.HOMEPAGE.type) ||
                    it.type == Represention.OBJECT_ACTION.type ||
                    it.type == Relation.OBJECT_LAYOUT.type ||
                    it.hasDisplayModel()
        }
        console.log(userActions)
        console.log(userActions.size)
        return userActions
    }

    private fun copyEvents(inputList: List<LogEntry>): List<LogEntry> {
        console.log("[ReplayCommand.copyEvents]")
        val outputList = mutableListOf<LogEntry>()
        inputList.forEach {
            outputList.add(copyEvent(it))
        }
        console.log(outputList)
        console.log(outputList.size)
        return outputList
    }

    private fun copyEvent(input: LogEntry): LogEntry {
        val resourceSpecification = ResourceSpecification(input.url, input.subType)
        val output = LogEntry(
            resourceSpecification,
            input.title,
            input.request,
            input.createdAt
        )
        output.type = input.type
        return output
    }

    private fun LogEntry.hasDisplayModel(): Boolean {
        return (this.nOfAggregators > 0) &&
                (this.getAggregator() is AggregatorWithLayout)
    }

}