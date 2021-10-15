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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.isis.client.kroviz.core.aggregator.AggregatorWithLayout
import org.apache.isis.client.kroviz.core.aggregator.BaseAggregator
import org.apache.isis.client.kroviz.main
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Represention

class ReplayCommand {

    fun execute() {
        val events = copyEvents(EventStore.log)
        EventStore.reset()
        main()

        console.log("[ReplayCommand.execute]")
        console.log(events)
        console.log(events.size)
        val userActions = events.filter {
            (it.type == Represention.HOMEPAGE.type) ||
                    it.type == Represention.OBJECT_ACTION.type ||
                    it.type == Represention.OBJECT.type ||
                    it.hasDisplayModel()
        }
        console.log(userActions)
        console.log(userActions.size)

        userActions.forEach {
            val link = Link(href = it.url)
            var aggregator: BaseAggregator? = null
            if (it.nOfAggregators > 0) {
                val clazz = it.getAggregator()::class
                aggregator = clazz.createInstance<BaseAggregator>()
            }
            //eventually put a thinkTime here
            wait(1000)
            console.log(link)
            console.log(aggregator)
            ResourceProxy().fetch(link, aggregator, it.subType)
        }

    }

    private fun copyEvents(inputList: List<LogEntry>): List<LogEntry> {
        val outputList = mutableListOf<LogEntry>()
        inputList.forEach {
            outputList.add(copyEvent(it))
        }
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

    fun wait(milliseconds: Long) {
        GlobalScope.launch {
            delay(milliseconds)
        }
    }

    private fun LogEntry.hasDisplayModel(): Boolean {
        if (this.nOfAggregators > 0) {
            val aggregator = this.getAggregator()
            return (aggregator is AggregatorWithLayout)
        }
        return false
    }

}