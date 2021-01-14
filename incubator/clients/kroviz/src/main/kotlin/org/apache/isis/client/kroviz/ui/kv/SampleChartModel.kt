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
package org.apache.isis.client.kroviz.ui.kv

import pl.treksoft.kvision.chart.DataSets
import pl.treksoft.kvision.core.Color
import pl.treksoft.kvision.i18n.I18n

@Deprecated("simple sample")
class SampleChartModel() : ChartModel {

    override var bgColorList = mutableListOf(
            Color.hex(0xC0504D),
            Color.hex(0xF79646),
            Color.hex(0x9BBB59),
            Color.hex(0x4BACC6),
            Color.hex(0x4F81BD),
            Color.hex(0x8064A2)
    )

    override var bgColorList2 = mutableListOf(
            Color.hex(0xC0504D.or(0x303030)),
            Color.hex(0xF79646.or(0x303030)),
            Color.hex(0x9BBB59.or(0x203030)),
            Color.hex(0x4BACC6.or(0x301010)),
            Color.hex(0x4F81BD.or(0x101010)),
            Color.hex(0x8064A2.or(0x303030))
    )

    override var labelList = mutableListOf<String>(
            I18n.tr("BU 6"),
            I18n.tr("BU 5"),
            I18n.tr("BU 4"),
            I18n.tr("BU 3"),
            I18n.tr("BU 2"),
            I18n.tr("BU 1")
    )

    override var datasetList = mutableListOf<DataSets>()

    override var ds1 = DataSets(
            data = listOf(300, 727, 589, 537, 543, 574),
            backgroundColor = bgColorList,
            label = "initial"
    )

    override var ds2 = DataSets(
            data = listOf(400, 238, 553, 746, 884, 903),
            backgroundColor = bgColorList2,
            label = "duplicates"
    )

    init {
        datasetList.add(ds1)
        datasetList.add(ds2)
    }

}
