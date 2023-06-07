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
package org.apache.causeway.client.kroviz.ui.builder

import io.kvision.core.*
import io.kvision.panel.FieldsetPanel
import io.kvision.panel.FlexPanel
import io.kvision.panel.HPanel
import io.kvision.panel.SimplePanel
import org.apache.causeway.client.kroviz.to.TObject
import org.apache.causeway.client.kroviz.to.bs.ColBs
import org.apache.causeway.client.kroviz.to.bs.CollectionBs
import org.apache.causeway.client.kroviz.to.bs.FieldSetBs
import org.apache.causeway.client.kroviz.ui.core.Constants
import org.apache.causeway.client.kroviz.ui.core.RoTable
import org.apache.causeway.client.kroviz.ui.menu.DropDownMenuBuilder
import org.apache.causeway.client.kroviz.utils.StringUtils
import kotlin.math.round

class ColBuilder(
    val col: ColBs,
    val tObject: TObject,
    private val dsp: RoDisplay
) : UiBuilder() {

    var panel: FlexPanel

    init {
        panel = buildPanel()
        addMenu()
        addTabgroups()
        addFieldSets()
        addRows()
        addCollections()
    }

    private fun addMenu() {
        if ((col.actionList.size > 0) && (col.domainObject != null)) {
            val menu = createMenu(tObject, dsp)
            assignWidth(menu, col)
            panel.add(menu)
        }
    }

    private fun addTabgroups() {
        col.tabGroupList.forEach { tg ->
            val tgCpt = TabGroupBuilder().create(tg, tObject, dsp)
            panel.add(tgCpt)
        }
    }

    private fun addFieldSets() {
        col.fieldSetList.forEach { fs ->
            if (fs.propertyList.size > 0) {
                val fsPanel = buildFieldSetPanel(fs)
                assignWidth(fsPanel, col)
                panel.add(fsPanel)
            }
        }
    }

    private fun addRows() {
        col.rowList.forEach { row ->
            val rowCpt = RowBuilder().create(row, tObject, dsp)
            panel.add(rowCpt)
        }
    }

    private fun addCollections() {
        console.log("[CB_addCollections]")
        if (dsp.displayModel.hasCollectionModels()) {
            col.collectionList.forEach {
                console.log(it)
                buildTableAndWrapInFsPanel(it)
            }
        }
    }

    private fun buildTableAndWrapInFsPanel(it: CollectionBs) {
        val objectDM = dsp.displayModel
        try {
            val cdm = objectDM.getCollectionDisplayModelFor(it.id)!!
            val fsPanel = FieldsetPanel(legend = cdm.getTitle())
            val table = RoTable(cdm)
            console.log(table)
            fsPanel.add(table)
            panel.add(fsPanel)
            cdm.isRendered = true
        } catch (npe: NullPointerException) {
            return
        }
    }

    private fun buildFieldSetPanel(fs: FieldSetBs): SimplePanel {
        val fsCpt = FieldSetBuilder().create(fs, tObject, dsp)!!
        val legend = extractLegend(fs)
        val fsPanel = FieldsetPanel(legend = legend)
        fsPanel.add(fsCpt)
        val tto = TooltipOptions(title = fs.id)
        fsPanel.enableTooltip(tto)
        return fsPanel
    }

    private fun extractLegend(fs: FieldSetBs): String {
        var legend = fs.name.trim()
        if (legend.isEmpty()) {
            legend = fs.id
        }
        return StringUtils.capitalize(legend)
    }

    private fun buildPanel(): FlexPanel {
        val panel = FlexPanel(
            FlexDirection.COLUMN,
            FlexWrap.NOWRAP,
            JustifyContent.FLEXSTART,
            AlignItems.FLEXSTART,
            AlignContent.FLEXSTART,
            spacing = Constants.spacing
        )
        panel.padding = CssSize(10, UNIT.px)
        return panel
    }

    private fun createMenu(tObject: TObject, dsp: RoDisplay): HPanel {
        val panel = HPanel()
        style(panel)
        val dd = DropDownMenuBuilder().buildForObjectWithSaveAndUndo(tObject)
        dsp.menu = dd
        panel.add(dd)
        return panel
    }

    private fun assignWidth(panel: SimplePanel, col: ColBs) {
        val proportion = col.span.toDouble().div(12)
        val percent = proportion * 100
        val rounded = round(percent)
        val cssWidth = CssSize(rounded, UNIT.perc)
        panel.flexBasis = cssWidth
        panel.flexGrow = 1
    }

}
