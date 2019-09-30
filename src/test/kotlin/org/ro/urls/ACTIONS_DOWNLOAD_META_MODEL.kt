package org.ro.urls

object ACTIONS_DOWNLOAD_META_MODEL  : Response() {
    override val url = "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu/actions/downloadLayouts"
    override val str = """{
  "id" : "downloadMetaModel",
  "memberType" : "action",
  "links" : [ {
    "rel" : "self",
    "href" : "http://localhost:8080/restful/services/isisApplib.MetaModelServicesMenu/actions/downloadMetaModel",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
  }, {
    "rel" : "up",
    "href" : "http://localhost:8080/restful/services/isisApplib.MetaModelServicesMenu",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
    "title" : "Prototyping"
  }, {
    "rel" : "urn:org.restfulobjects:rels/invoke;action=\"downloadMetaModel\"",
    "href" : "http://localhost:8080/restful/services/isisApplib.MetaModelServicesMenu/actions/downloadMetaModel/invoke",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\"",
    "arguments" : {
      "" : {
        "csvFileName" : {
          "value" : null
        }
      }
    }
  }, {
    "rel" : "describedby",
    "href" : "http://localhost:8080/restful/domain-types/isisApplib.MetaModelServicesMenu/actions/downloadMetaModel",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
  } ],
  "extensions" : {
    "actionType" : "prototype",
    "actionSemantics" : "safe"
  },
  "parameters" : {
    ".csvFileName" : {
      "num" : 0,
      "id" : ".csvFileName",
      "name" : ".csv file name",
      "description" : "",
      "default" : "metamodel.csv"
    }
  }
}"""
}
