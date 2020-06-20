package org.apache.isis.client.kroviz.ui.kv

import pl.treksoft.kvision.chart.DataSets
import pl.treksoft.kvision.core.Color
import pl.treksoft.kvision.i18n.I18n

class ChartModel() {

    private val bgColorList = listOf(
            Color.hex(0xC0504D),
            Color.hex(0xF79646),
            Color.hex(0x9BBB59),
            Color.hex(0x4BACC6),
            Color.hex(0x4F81BD),
            Color.hex(0x8064A2)
    )

    private val bgColorList2 = listOf(
            Color.hex(0xC0504D.or(0x303030)),
            Color.hex(0xF79646.or(0x303030)),
            Color.hex(0x9BBB59.or(0x203030)),
            Color.hex(0x4BACC6.or(0x301010)),
            Color.hex(0x4F81BD.or(0x101010)),
            Color.hex(0x8064A2.or(0x303030))
    )

    val labelList = listOf<String>(
            I18n.tr("BU 6"),
            I18n.tr("BU 5"),
            I18n.tr("BU 4"),
            I18n.tr("BU 3"),
            I18n.tr("BU 2"),
            I18n.tr("BU 1")
    )

    val datasetList = mutableListOf<DataSets>()

    private val ds1 = DataSets(
            data = listOf(300, 727, 589, 537, 543, 574),
            backgroundColor = bgColorList,
            label = "initial"
    )
    private val ds2 = DataSets(
            data = listOf(400, 238, 553, 746, 884, 903),
            backgroundColor = bgColorList2,
            label = "duplicates"
    )

    init {
        datasetList.add(ds1)
        datasetList.add(ds2)
    }
}
