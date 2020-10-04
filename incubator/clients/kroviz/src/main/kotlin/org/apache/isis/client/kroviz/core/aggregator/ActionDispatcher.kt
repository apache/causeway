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
import org.apache.isis.client.kroviz.to.Action
import org.apache.isis.client.kroviz.to.Link
import org.apache.isis.client.kroviz.to.Method
import org.apache.isis.client.kroviz.ui.kv.ActionPrompt
import org.apache.isis.client.kroviz.utils.Point
import org.apache.isis.client.kroviz.utils.Utils

class ActionDispatcher(private val at: Point = Point(100, 100)) : BaseAggregator() {

    override fun update(logEntry: LogEntry, subType: String) {
        val action = logEntry.getTransferObject() as Action
        action.links.forEach { link ->
            if (link.isInvokeAction()) {
                when (link.method) {
                    Method.GET.name -> process(action, link)
                    Method.POST.name -> {
                        val title = Utils.deCamel(action.id)
                        process(action, link, ObjectAggregator(title))
                    }
                    Method.PUT.name -> process(action, link)
                }
            }
        }
    }

    /**
     *  link.rel should neither be: (self | up | describedBy )
     */
    private fun Link.isInvokeAction(): Boolean {
        return rel.contains("invoke") && rel.contains("action")
    }

    private fun process(action: Action, link: Link, aggregator: BaseAggregator = this) {
        if (link.hasArguments()) {
            ActionPrompt(action = action).open(at)
        } else {
            link.invokeWith(aggregator)
        }
    }

    fun invoke(link: Link) {
        link.invokeWith(this)
    }

}
