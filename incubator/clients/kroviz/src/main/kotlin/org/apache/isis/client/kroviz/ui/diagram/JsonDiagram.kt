package org.apache.isis.client.kroviz.ui.diagram

object JsonDiagram {

    fun build(json: String): String {
        return "@startjson" + "\n" + json + "\n" + "@endjson"
    }

}
