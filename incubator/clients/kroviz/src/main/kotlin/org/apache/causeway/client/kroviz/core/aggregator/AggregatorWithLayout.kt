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
package org.apache.causeway.client.kroviz.core.aggregator

import org.apache.causeway.client.kroviz.core.event.LogEntry
import org.apache.causeway.client.kroviz.core.model.DisplayModelWithLayout
import org.apache.causeway.client.kroviz.layout.Layout
import org.apache.causeway.client.kroviz.to.Represention
import org.apache.causeway.client.kroviz.to.TObject
import org.apache.causeway.client.kroviz.ui.core.Constants
import org.apache.causeway.client.kroviz.ui.diagram.Tree

abstract class AggregatorWithLayout : BaseAggregator() {
    // parentUrl is to be set in update
    // and to be used in subsequent invocations
    var parentUrl: String? = null
    var tree: Tree? = null

    override fun update(logEntry: LogEntry, subType: String) {
        parentUrl = logEntry.url
    }

    protected fun handleLayout(layout: Layout, dm: DisplayModelWithLayout, referrer: String) {
        console.log("[AWL.handleLayout]")
        console.log(layout)
        if (dm.layout == null) {
            dm.addLayout(layout)
            dm.properties.propertyLayoutList.forEach { p ->
                val l = p.link
                if (l == null) {
                    console.log(p.id + " link empty")  // CAUSEWAY-2846
                    console.log(p)
                } else {
                    val isDn = l.href.contains("datanucleus")
                    if (!isDn) {
                        //invoking DN links leads to an error
                        invoke(l, this, referrer = referrer)
                    }
                }
            }
        }
    }

    protected fun invokeLayoutLink(obj: TObject, aggregator: AggregatorWithLayout, referrer: String) {
        val l = obj.getLayoutLink()
        if (l.representation() == Represention.OBJECT_LAYOUT_BS) {
            invoke(l, aggregator, Constants.subTypeXml, referrer)
        } else {
            invoke(l, aggregator, referrer = referrer)
        }
    }

    protected fun invokeIconLink(obj: TObject, aggregator: AggregatorWithLayout, referrer: String) {
        val l = obj.getIconLink()!!
        invoke(l, aggregator, referrer = referrer)
    }

}
