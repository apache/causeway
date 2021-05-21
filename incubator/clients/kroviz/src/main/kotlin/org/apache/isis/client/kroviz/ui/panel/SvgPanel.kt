/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.isis.client.kroviz.ui.panel

import org.apache.isis.client.kroviz.utils.ScalableVectorGraphic
import io.kvision.maps.*
import io.kvision.panel.HPanel
import io.kvision.utils.pc

class SvgPanel : HPanel() {

    val map: Maps = maps(0, 0, 11, baseLayerProvider = BaseLayerProvider.EMPTY, crs = CRS.Simple) {
        width = 100.pc
        height = 100.pc
    }

    init {
        add(map)
    }

    fun renderSvg(str: String) {
        val svg = ScalableVectorGraphic(str)
        svg.scaleHorizontally()
        val svgDoc = svg.document
        val svgElement = svgDoc.documentElement!!

        val bounds = LatLngBounds(
                LatLng(0, 0),
                LatLng(0.05, 0.05))
        map.svgOverlay(svgElement, bounds)
    }

}
