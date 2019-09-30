package org.ro.urls

object ACTIONS_DOWNLOAD_MENUBARS_LAYOUT : Response() {
    override val url = "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu/actions/downloadLayouts"
    override val str = """{
  "id" : "downloadMenuBarsLayout",
  "memberType" : "action",
  "links" : [ {
    "rel" : "self",
    "href" : "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu/actions/downloadMenuBarsLayout",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
  }, {
    "rel" : "up",
    "href" : "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
    "title" : "Prototyping"
  }, {
    "rel" : "urn:org.restfulobjects:rels/invoke;action=\"downloadMenuBarsLayout\"",
    "href" : "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu/actions/downloadMenuBarsLayout/invoke",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\"",
    "arguments" : {
      "fileName" : {
        "value" : null
      },
      "type" : {
        "value" : null
      }
    }
  }, {
    "rel" : "describedby",
    "href" : "http://localhost:8080/restful/domain-types/isisApplib.LayoutServiceMenu/actions/downloadMenuBarsLayout",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
  } ],
  "extensions" : {
    "actionType" : "prototype",
    "actionSemantics" : "safe"
  },
  "parameters" : {
    "fileName" : {
      "num" : 0,
      "id" : "fileName",
      "name" : "File name",
      "description" : "",
      "default" : "menubars.layout.xml"
    },
    "type" : {
      "num" : 1,
      "id" : "type",
      "name" : "Type",
      "description" : "",
      "choices" : [ "Default", "Fallback" ],
      "default" : "Default"
    }
  }
}"""
}
