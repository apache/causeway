package org.apache.isis.client.kroviz.ui.kv

import pl.treksoft.kvision.chart.*
import pl.treksoft.kvision.core.Col
import pl.treksoft.kvision.core.Color
import pl.treksoft.kvision.i18n.I18n.tr
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.gridPanel
import pl.treksoft.kvision.utils.obj
import pl.treksoft.kvision.utils.px
import kotlin.math.sin

class ChartTab : SimplePanel() {
    init {
        this.marginTop = 10.px

        gridPanel(templateColumns = "50% 50%", columnGap = 30, rowGap = 30) {
            @Suppress("UnsafeCastFromDynamic")
            chart(
                    Configuration(
                            ChartType.SCATTER,
                            listOf(
                                    DataSets(
                                            pointBorderColor = listOf(Color.hex(0xC0504D)),
                                            backgroundColor = listOf(Color.hex(0x9BBB59)),
                                            data = (-60..60).map {
                                                obj {
                                                    x = it.toDouble() / 10
                                                    y = sin(it.toDouble() / 10)
                                                }
                                            }
                                    )
                            ),
                            options = ChartOptions(legend = LegendOptions(display = false), showLines = true)
                    )
            )
            chart(
                    Configuration(
                            ChartType.BAR,
                            listOf(
                                    DataSets(
                                            data = listOf(6, 12, 19, 13, 7, 3),
                                            backgroundColor = listOf(
                                                    Color.hex(0xC0504D),
                                                    Color.hex(0xF79646),
                                                    Color.hex(0x9BBB59),
                                                    Color.hex(0x4BACC6),
                                                    Color.hex(0x4F81BD),
                                                    Color.hex(0x8064A2)
                                            )
                                    )
                            ),
                            listOf(
                                    tr("Africa"),
                                    tr("Asia"),
                                    tr("Europe"),
                                    tr("Latin America"),
                                    tr("North America"),
                                    tr("Australia")
                            ),
                            ChartOptions(legend = LegendOptions(display = false), scales = ChartScales(yAxes = listOf(obj {
                                ticks = obj {
                                    suggestedMin = 0
                                    suggestedMax = 20
                                }
                            })), title = TitleOptions(display = false))
                    )
            )
            chart(
                    Configuration(
                            ChartType.PIE,
                            listOf(
                                    DataSets(
                                            data = listOf(6, 12, 19, 13, 7, 3),
                                            backgroundColor = listOf(
                                                    Color.hex(0xC0504D),
                                                    Color.hex(0xF79646),
                                                    Color.hex(0x9BBB59),
                                                    Color.hex(0x4BACC6),
                                                    Color.hex(0x4F81BD),
                                                    Color.hex(0x8064A2)
                                            )
                                    )
                            ), listOf(
                            tr("Africa"),
                            tr("Asia"),
                            tr("Europe"),
                            tr("Latin America"),
                            tr("North America"),
                            tr("Australia")
                    )
                    )
            )
            chart(
                    Configuration(
                            ChartType.POLARAREA,
                            listOf(
                                    DataSets(
                                            data = listOf(6, 12, 19, 13, 7, 3),
                                            backgroundColor = listOf(
                                                    Color.hex(0xC0504D),
                                                    Color.hex(0xF79646),
                                                    Color.hex(0x9BBB59),
                                                    Color.hex(0x4BACC6),
                                                    Color.hex(0x4F81BD),
                                                    Color.hex(0x8064A2)
                                            )
                                    )
                            ), listOf(
                            tr("Africa"),
                            tr("Asia"),
                            tr("Europe"),
                            tr("Latin America"),
                            tr("North America"),
                            tr("Australia")
                    )
                    )
            )
        }
    }
}
