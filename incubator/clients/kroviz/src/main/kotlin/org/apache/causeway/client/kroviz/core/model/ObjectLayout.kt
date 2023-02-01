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

import org.apache.causeway.client.kroviz.core.aggregator.AggregatorWithLayout
import org.apache.causeway.client.kroviz.core.event.ResourceProxy
import org.apache.causeway.client.kroviz.to.Link
import org.apache.causeway.client.kroviz.to.ObjectProperty
import org.apache.causeway.client.kroviz.to.PropertyDescription
import org.apache.causeway.client.kroviz.to.bs.GridBs
import org.apache.causeway.client.kroviz.to.bs.RowBs

class ObjectLayout : BaseLayout() {

    var grid: GridBs? = null
    private val collectionLayoutMap = mutableMapOf<String, CollectionLayout>()

    override fun readyToRender(): Boolean {
        console.log("[OL_readyToRender]")
        return when (grid) {
            null -> false
            else -> {
                var answer = true
                collectionLayoutMap.values.forEach {
                    console.log(it)
                    if (!it.readyToRender()) answer = false
                }
                answer
            }
        }
    }

    override fun addObjectProperty(
        property: ObjectProperty,
        aggregator: AggregatorWithLayout,
        referrer: String
    ) {
        TODO("Not yet implemented")
    }

    override fun addPropertyDescription(
        property: PropertyDescription,
        aggregator: AggregatorWithLayout,
        referrer: String
    ) {
        TODO("Not yet implemented")
    }

    fun addGrid(grid: GridBs, aggregator: AggregatorWithLayout?, referrer: String?) {
        this.grid = grid
        grid.rows.forEach { r ->
            initRow(r, aggregator!!, referrer = referrer!!)
        }
    }

    private fun initRow(r: RowBs, aggregator: AggregatorWithLayout, referrer: String) {
        r.colList.forEach { c ->
            c.collectionList.forEach { col ->
                collectionLayoutMap[col.id] =
                    CollectionLayout() // create empty entry for id - for later usage when response arrives
                val href = col.linkList.first().href // we assume, linklist has always one element
                val l = Link(href = href)
                ResourceProxy().fetch(l, aggregator, referrer = referrer)
            }
            c.tabGroupList.forEach { tg ->
                tg.tabList.forEach { t ->
                    t.rowList.forEach { r2 ->
                        initRow(r2, aggregator, referrer)
                    }
                }
            }
        }
    }

}