package org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0

import org.apache.isis.client.kroviz.snapshots.Response

object ACTIONS_DOWNLOAD_LAYOUTS : Response() {
    override val url = "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu/actions/downloadLayouts"
    override val str = """{
  "id" : "downloadLayouts",
  "memberType" : "action",
  "links" : [ {
    "rel" : "self",
    "href" : "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu/actions/downloadLayouts",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
  }, {
    "rel" : "up",
    "href" : "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
    "title" : "Prototyping"
  }, {
    "rel" : "urn:org.restfulobjects:rels/invoke;action=\"downloadLayouts\"",
    "href" : "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu/actions/downloadLayouts/invoke",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\"",
    "arguments" : {
      "style" : {
        "value" : null
      }
    }
  }, {
    "rel" : "describedby",
    "href" : "http://localhost:8080/restful/domain-types/isisApplib.LayoutServiceMenu/actions/downloadLayouts",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
  } ],
  "extensions" : {
    "actionType" : "prototype",
    "actionSemantics" : "safe"
  },
  "parameters" : {
    "style" : {
      "num" : 0,
      "id" : "style",
      "name" : "Style",
      "description" : "",
      "choices" : [ "Current", "Complete", "Normalized", "Minimal" ],
      "default" : "Normalized"
    }
  }
}
"""
}
