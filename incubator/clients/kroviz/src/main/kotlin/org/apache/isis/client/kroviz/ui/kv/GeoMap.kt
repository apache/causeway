package org.apache.isis.client.kroviz.ui.kv

import pl.treksoft.kvision.maps.maps
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.utils.pc
import pl.treksoft.kvision.utils.vh

// see -> kvision-kvision-maps.js
// for 2.5D, buildings see https://osmbuildings.org/documentation/leaflet/
class GeoMap : SimplePanel() {

    init {
        maps(53.65425, 10.1545, 15) {
            width = 100.pc
            height = 89.vh
        }
    }
}
