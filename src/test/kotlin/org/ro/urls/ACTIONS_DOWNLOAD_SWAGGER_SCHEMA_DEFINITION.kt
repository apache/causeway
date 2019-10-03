package org.ro.urls

object ACTIONS_DOWNLOAD_SWAGGER_SCHEMA_DEFINITION : Response() {
    override val url = "http://localhost:8080/restful/services/isisApplib.SwaggerServiceMenu/actions/downloadSwaggerSchemaDefinition"
    override val str = """{
  "id" : "downloadSwaggerSchemaDefinition",
  "memberType" : "action",
  "links" : [ {
    "rel" : "self",
    "href" : "http://localhost:8080/restful/services/isisApplib.SwaggerServiceMenu/actions/downloadSwaggerSchemaDefinition",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
  }, {
    "rel" : "up",
    "href" : "http://localhost:8080/restful/services/isisApplib.SwaggerServiceMenu",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
    "title" : "Prototyping"
  }, {
    "rel" : "urn:org.restfulobjects:rels/invoke;action=\"downloadSwaggerSchemaDefinition\"",
    "href" : "http://localhost:8080/restful/services/isisApplib.SwaggerServiceMenu/actions/downloadSwaggerSchemaDefinition/invoke",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\"",
    "arguments" : {
      "filename" : {
        "value" : null
      },
      "visibility" : {
        "value" : null
      },
      "format" : {
        "value" : null
      }
    }
  }, {
    "rel" : "describedby",
    "href" : "http://localhost:8080/restful/domain-types/isisApplib.SwaggerServiceMenu/actions/downloadSwaggerSchemaDefinition",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
  } ],
  "extensions" : {
    "actionType" : "prototype",
    "actionSemantics" : "safe"
  },
  "parameters" : {
    "filename" : {
      "num" : 0,
      "id" : "filename",
      "name" : "Filename",
      "description" : "",
      "default" : "swagger"
    },
    "visibility" : {
      "num" : 1,
      "id" : "visibility",
      "name" : "Visibility",
      "description" : "",
      "choices" : [ "Public", "Private", "Private With Prototyping" ],
      "default" : "Private"
    },
    "format" : {
      "num" : 2,
      "id" : "format",
      "name" : "Format",
      "description" : "",
      "choices" : [ "Json", "Yaml" ],
      "default" : "Yaml"
    }
  }
} """
}
