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
import org.apache.isis.client.kroviz.to.*
import org.apache.isis.client.kroviz.ui.core.Constants
import org.apache.isis.client.kroviz.ui.dialog.ActionPrompt
import org.apache.isis.client.kroviz.utils.Point
import org.apache.isis.client.kroviz.utils.StringUtils

class ActionDispatcher(private val at: Point = Point(100, 100)) : BaseAggregator() {

    override fun update(logEntry: LogEntry, subType: String) {
        val to = logEntry.getTransferObject()
        val referrer = logEntry.url
        when {
            to is Action -> {
                to.links.forEach { link ->
                    if (link.isInvokeAction()) {
                        when (link.method) {
                            Method.GET.name -> process(to, link, referrer = referrer)
                            Method.POST.name -> invoke(to, link, referrer = referrer)
                            Method.PUT.name -> process(to, link, referrer = referrer)
                        }
                    }
                }
            }
            (to is TObject && to.domainType == "demo.CustomUiVm") -> {
                logEntry.aggregators.removeAt(0)
                val oa = ObjectAggregator(to.title)
                logEntry.aggregators.add(oa)
                oa.update(logEntry, Constants.subTypeJson)
            }
            to is Restful -> {}
            else -> {
                console.log(to)
                throw Throwable("[ActionDispatcher.update] ${to!!::class.simpleName}")
            }
        }
    }

    private fun process(action: Action, link: Link, aggregator: BaseAggregator = this, referrer: String) {
        when {
            link.hasArguments() -> ActionPrompt(action = action).open(at)
            link.relation() == Relation.INVOKE -> invoke(action, link, referrer)
            else -> invoke(link, aggregator, referrer = referrer)
        }
    }

    private fun invoke(action: Action, link: Link, referrer: String) {
        val title = StringUtils.deCamel(action.id)
        invoke(link, ObjectAggregator(title), referrer = referrer)
    }

    /**
     *  link.rel should neither be: (self | up | describedBy )
     */
    private fun Link.isInvokeAction(): Boolean {
        return relation() == Relation.INVOKE && rel.contains("action")
    }

}
