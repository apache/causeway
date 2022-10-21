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
package org.apache.causeway.client.kroviz.ui.panel

import io.kvision.core.CssSize
import io.kvision.core.UNIT
import io.kvision.html.Button
import io.kvision.html.ButtonStyle
import io.kvision.maps.Maps
import io.kvision.maps.maps
import io.kvision.panel.HPanel
import io.kvision.utils.pc
import org.apache.causeway.client.kroviz.ui.core.Constants
import org.apache.causeway.client.kroviz.ui.core.ViewManager
import org.apache.causeway.client.kroviz.utils.IconManager

/**
 * Sample to be called from RoMenuBar
 */
class GeoMap : HPanel() {

    init {
        val m = maps(53.65425, 10.1545, 15) {
            width = 100.pc
            height = 100.pc
        }

        val home = LatLng(53.65425, 10.1545)
        m.addMarker(s = "Home")

        val office = LatLng(53.5403735, 10.0008355)
        m.addMarker(s = "Work<br><a href='https://en.wikipedia.org/wiki/Kuehne_%2B_Nagel'>KN</a>")

        ViewManager.getRoIconBar().add(createLocationIcon())

        setDropTargetData(Constants.stdMimeType) { id ->
            val mrk = parseMarker(id!!)
            if (mrk != null) m.addMarker(/*mrk.latLng,*/ s = mrk.title)
        }
    }

    private fun createLocationIcon(): Button {
        val loc = Button(
            text = "",
            icon = IconManager.find("Location"),
            style = ButtonStyle.LIGHT
        ).apply {
            padding = CssSize(-16, UNIT.px)
            margin = CssSize(0, UNIT.px)
            title = "Drag icon to map"
        }
        val location = "52.36393568#4.90446422#Zoku"
        loc.setDragDropData(Constants.stdMimeType, location)
        return loc
    }

    private fun parseMarker(id: String): Marker? {
        val raw = id.split("#")
        return if (raw.isNotEmpty()) {
            val lat = raw[0].toDouble()
            val lng = raw[1].toDouble()
//TODO implement extension function  and adopt new version of kvision.maps
            val latLng = LatLng(lat, lng)
            val title = if (raw.size >= 2) raw[2] else "no title set"
            Marker(title)
        } else null
    }

    private fun LatLng(lat: Double, lng: Double): Any {
        return { }
    }
}

private fun Maps.addMarker(latLang: Any? = null, s: String) {

}

class Marker(val title: String)


