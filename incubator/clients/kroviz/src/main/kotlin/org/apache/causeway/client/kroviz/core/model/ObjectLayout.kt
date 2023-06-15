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
package org.apache.causeway.client.kroviz.core.model

import org.apache.causeway.client.kroviz.core.aggregator.CollectionAggregator
import org.apache.causeway.client.kroviz.core.aggregator.ObjectAggregator
import org.apache.causeway.client.kroviz.core.event.ResourceProxy
import org.apache.causeway.client.kroviz.to.Link
import org.apache.causeway.client.kroviz.to.bs.CollectionBs
import org.apache.causeway.client.kroviz.to.bs.GridBs
import org.apache.causeway.client.kroviz.to.bs.RowBs

class ObjectLayout(val grid: GridBs, val aggregator: ObjectAggregator, val referrer: String) : BaseLayout() {
    init {
        grid.rows.forEach { r ->
            initRow(r)
        }
    }

    override fun readyToRender(): Boolean {
        console.log("[OL_readyToRender] constant:true")
        return true //TODO remove from protocol ?
    }

    private fun initRow(r: RowBs) {
        r.colList.forEach { c ->
            c.rowList.forEach { r2 ->
                r2.colList.forEach { c2 ->
                    c2.collectionList.forEach { col ->
                        initCollection(col)
                    }
                }
            }
            c.tabGroupList.forEach { tg ->
                tg.tabList.forEach { t ->
                    t.rowList.forEach { r3 ->
                        initRow(r3)
                    }
                }
            }
        }
    }

    private fun initCollection(collection: CollectionBs) {
        val href = collection.linkList.first().href // we assume, linklist has always one element
        val l = Link(href = href)
        val childAggt = CollectionAggregator("", aggregator)
        ResourceProxy().fetch(l, childAggt, referrer = referrer)
    }

}