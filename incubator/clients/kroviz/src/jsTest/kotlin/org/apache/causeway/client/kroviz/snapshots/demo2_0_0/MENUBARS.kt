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
package org.apache.causeway.client.kroviz.snapshots.demo2_0_0

import org.apache.causeway.client.kroviz.snapshots.Response

object MENUBARS : Response(){
    override val url = "http://localhost:8080/restful/menuBars"
    override val str = """{
  "primary": {
    "menu": [
      {
        "named": "Basic Types",
        "cssClassFa": null,
        "section": [
          {
            "named": "Primitives",
            "serviceAction": [
              {
                "objectType": "demo.PrimitiveTypesMenu",
                "id": "shorts",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PrimitiveTypesMenu/1/actions/shorts",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PrimitiveTypesMenu",
                "id": "ints",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PrimitiveTypesMenu/1/actions/ints",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PrimitiveTypesMenu",
                "id": "longs",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PrimitiveTypesMenu/1/actions/longs",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PrimitiveTypesMenu",
                "id": "bytes",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PrimitiveTypesMenu/1/actions/bytes",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PrimitiveTypesMenu",
                "id": "floats",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PrimitiveTypesMenu/1/actions/floats",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PrimitiveTypesMenu",
                "id": "doubles",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PrimitiveTypesMenu/1/actions/doubles",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PrimitiveTypesMenu",
                "id": "chars",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PrimitiveTypesMenu/1/actions/chars",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PrimitiveTypesMenu",
                "id": "booleans",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PrimitiveTypesMenu/1/actions/booleans",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "Wrappers",
            "serviceAction": [
              {
                "objectType": "demo.JavaLangWrapperTypesMenu",
                "id": "bytes",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaLangWrapperTypesMenu/1/actions/bytes",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.JavaLangWrapperTypesMenu",
                "id": "shorts",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaLangWrapperTypesMenu/1/actions/shorts",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.JavaLangWrapperTypesMenu",
                "id": "integers",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaLangWrapperTypesMenu/1/actions/integers",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.JavaLangWrapperTypesMenu",
                "id": "longs",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaLangWrapperTypesMenu/1/actions/longs",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.JavaLangWrapperTypesMenu",
                "id": "floats",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaLangWrapperTypesMenu/1/actions/floats",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.JavaLangWrapperTypesMenu",
                "id": "doubles",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaLangWrapperTypesMenu/1/actions/doubles",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.JavaLangWrapperTypesMenu",
                "id": "characters",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaLangWrapperTypesMenu/1/actions/characters",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.JavaLangWrapperTypesMenu",
                "id": "booleans",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaLangWrapperTypesMenu/1/actions/booleans",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "Common",
            "serviceAction": [
              {
                "objectType": "demo.JavaLangTypesMenu",
                "id": "strings",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaLangTypesMenu/1/actions/strings",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.JavaLangTypesMenu",
                "id": "voids",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaLangTypesMenu/1/actions/voids",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          }
        ],
        "unreferencedActions": null
      },
      {
        "named": "Temporal Types",
        "cssClassFa": null,
        "section": [
          {
            "named": "java.sql",
            "serviceAction": [
              {
                "objectType": "demo.JavaSqlTypesMenu",
                "id": "dates",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaSqlTypesMenu/1/actions/dates",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.JavaSqlTypesMenu",
                "id": "timestamps",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaSqlTypesMenu/1/actions/timestamps",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "java.time",
            "serviceAction": [
              {
                "objectType": "demo.JavaTimeTypesMenu",
                "id": "localDates",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaTimeTypesMenu/1/actions/localDates",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.JavaTimeTypesMenu",
                "id": "localDateTimes",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaTimeTypesMenu/1/actions/localDateTimes",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.JavaTimeTypesMenu",
                "id": "offsetDateTimes",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaTimeTypesMenu/1/actions/offsetDateTimes",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.JavaTimeTypesMenu",
                "id": "offsetTimes",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaTimeTypesMenu/1/actions/offsetTimes",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.JavaTimeTypesMenu",
                "id": "zonedDateTimes",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaTimeTypesMenu/1/actions/zonedDateTimes",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "java.util",
            "serviceAction": [
              {
                "objectType": "demo.JavaUtilTypesMenu",
                "id": "dates",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaUtilTypesMenu/1/actions/dates",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "JodaTime",
            "serviceAction": [
              {
                "objectType": "demo.JodaTimeTypesMenu",
                "id": "localDates",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JodaTimeTypesMenu/1/actions/localDates",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.JodaTimeTypesMenu",
                "id": "localDateTimes",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JodaTimeTypesMenu/1/actions/localDateTimes",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.JodaTimeTypesMenu",
                "id": "dateTimes",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JodaTimeTypesMenu/1/actions/dateTimes",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          }
        ],
        "unreferencedActions": null
      },
      {
        "named": "More Types",
        "cssClassFa": null,
        "section": [
          {
            "named": "java.awt",
            "serviceAction": [
              {
                "objectType": "demo.JavaAwtTypesMenu",
                "id": "images",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaAwtTypesMenu/1/actions/images",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "java.math",
            "serviceAction": [
              {
                "objectType": "demo.JavaMathTypesMenu",
                "id": "bigDecimals",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaMathTypesMenu/1/actions/bigDecimals",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.JavaMathTypesMenu",
                "id": "bigIntegers",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaMathTypesMenu/1/actions/bigIntegers",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "java.net",
            "serviceAction": [
              {
                "objectType": "demo.JavaNetTypesMenu",
                "id": "urls",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaNetTypesMenu/1/actions/urls",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "java.util",
            "serviceAction": [
              {
                "objectType": "demo.JavaUtilTypesMenu",
                "id": "uuids",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JavaUtilTypesMenu/1/actions/uuids",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "Apache Causeway Core",
            "serviceAction": [
              {
                "objectType": "demo.CausewayTypesMenu",
                "id": "blobs",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.CausewayTypesMenu/1/actions/blobs",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.CausewayTypesMenu",
                "id": "clobs",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.CausewayTypesMenu/1/actions/clobs",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.CausewayTypesMenu",
                "id": "localResourcePaths",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.CausewayTypesMenu/1/actions/localResourcePaths",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.CausewayTypesMenu",
                "id": "markups",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.CausewayTypesMenu/1/actions/markups",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.CausewayTypesMenu",
                "id": "passwords",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.CausewayTypesMenu/1/actions/passwords",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "Apache Causeway Extensions",
            "serviceAction": [
              {
                "objectType": "demo.CausewayExtTypesMenu",
                "id": "asciiDocs",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.CausewayExtTypesMenu/1/actions/asciiDocs",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.CausewayExtTypesMenu",
                "id": "markdowns",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.CausewayExtTypesMenu/1/actions/markdowns",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          }
        ],
        "unreferencedActions": null
      },
      {
        "named": "Domain Annot",
        "cssClassFa": null,
        "section": [
          {
            "named": "@DomainObject",
            "serviceAction": [
              {
                "objectType": "demo.DomainObjectMenu",
                "id": "publishing",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.DomainObjectMenu/1/actions/publishing",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "@Action",
            "serviceAction": [
              {
                "objectType": "demo.ActionMenu",
                "id": "associateWith",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.ActionMenu/1/actions/associateWith",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.ActionMenu",
                "id": "command",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.ActionMenu/1/actions/command",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.ActionMenu",
                "id": "domainEvent",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.ActionMenu/1/actions/domainEvent",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.ActionMenu",
                "id": "hidden",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.ActionMenu/1/actions/hidden",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.ActionMenu",
                "id": "publishing",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.ActionMenu/1/actions/publishing",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.ActionMenu",
                "id": "typeOf",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.ActionMenu/1/actions/typeOf",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "@Property",
            "serviceAction": [
              {
                "objectType": "demo.PropertyMenu",
                "id": "command",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyMenu/1/actions/command",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyMenu",
                "id": "domainEvent",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyMenu/1/actions/domainEvent",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyMenu",
                "id": "editing",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyMenu/1/actions/editing",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyMenu",
                "id": "fileAccept",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyMenu/1/actions/fileAccept",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyMenu",
                "id": "hidden",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyMenu/1/actions/hidden",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyMenu",
                "id": "maxLength",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyMenu/1/actions/maxLength",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyMenu",
                "id": "mustSatisfy",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyMenu/1/actions/mustSatisfy",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyMenu",
                "id": "optionality",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyMenu/1/actions/optionality",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyMenu",
                "id": "publishing",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyMenu/1/actions/publishing",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyMenu",
                "id": "regexPattern",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyMenu/1/actions/regexPattern",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          }
        ],
        "unreferencedActions": null
      },
      {
        "named": "Layout Annot",
        "cssClassFa": null,
        "section": [
          {
            "named": "@ActionLayout",
            "serviceAction": [
              {
                "objectType": "demo.ActionLayoutMenu",
                "id": "position",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.ActionLayoutMenu/1/actions/position",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.ActionLayoutMenu",
                "id": "promptStyle",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.ActionLayoutMenu/1/actions/promptStyle",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "@PropertyLayout",
            "serviceAction": [
              {
                "objectType": "demo.PropertyLayoutMenu",
                "id": "cssClass",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyLayoutMenu/1/actions/cssClass",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyLayoutMenu",
                "id": "describedAs",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyLayoutMenu/1/actions/describedAs",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyLayoutMenu",
                "id": "hidden",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyLayoutMenu/1/actions/hidden",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyLayoutMenu",
                "id": "labelPosition",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyLayoutMenu/1/actions/labelPosition",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyLayoutMenu",
                "id": "multiLine",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyLayoutMenu/1/actions/multiLine",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyLayoutMenu",
                "id": "named",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyLayoutMenu/1/actions/named",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyLayoutMenu",
                "id": "navigable",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyLayoutMenu/1/actions/navigable",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyLayoutMenu",
                "id": "renderDay",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyLayoutMenu/1/actions/renderDay",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyLayoutMenu",
                "id": "repainting",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyLayoutMenu/1/actions/repainting",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.PropertyLayoutMenu",
                "id": "typicalLength",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.PropertyLayoutMenu/1/actions/typicalLength",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          }
        ],
        "unreferencedActions": null
      },
      {
        "named": "Services",
        "cssClassFa": null,
        "section": [
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "demo.ServicesMenu",
                "id": "wrapperFactory",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.ServicesMenu/1/actions/wrapperFactory",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          }
        ],
        "unreferencedActions": null
      },
      {
        "named": "View Models",
        "cssClassFa": null,
        "section": [
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "demo.ViewModelMenu",
                "id": "stateful",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.ViewModelMenu/1/actions/stateful",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.ViewModelMenu",
                "id": "statefulRefsEntity",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.ViewModelMenu/1/actions/statefulRefsEntity",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          }
        ],
        "unreferencedActions": null
      },
      {
        "named": "Actions",
        "cssClassFa": null,
        "section": [
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "demo.AssociatedActionMenu",
                "id": "associatedActions",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.AssociatedActionMenu/1/actions/associatedActions",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.AsyncActionMenu",
                "id": "asyncActions",
                "named": "Background (Async) Actions",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.AsyncActionMenu/1/actions/asyncActions",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.DependentArgsActionMenu",
                "id": "dependentArgsActions",
                "named": "Dependent Arguments",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.DependentArgsActionMenu/1/actions/dependentArgsActions",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.MixinMenu",
                "id": "mixinDemo",
                "named": "Mixins",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.MixinMenu/1/actions/mixinDemo",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.MixinLegacyMenu",
                "id": "mixinLegacyDemo",
                "named": "Mixins (Legacy)",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.MixinLegacyMenu/1/actions/mixinLegacyDemo",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          }
        ],
        "unreferencedActions": null
      },
      {
        "named": "Misc",
        "cssClassFa": null,
        "section": [
          {
            "named": "Tooltips",
            "serviceAction": [
              {
                "objectType": "demo.TooltipMenu",
                "id": "tooltipDemo",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.TooltipMenu/1/actions/tooltipDemo",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "Events",
            "serviceAction": [
              {
                "objectType": "demo.EventsDemoMenu",
                "id": "eventsDemo",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.EventsDemoMenu/1/actions/eventsDemo",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "Error Handling",
            "serviceAction": [
              {
                "objectType": "demo.ErrorMenu",
                "id": "errorHandling",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.ErrorMenu/1/actions/errorHandling",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "Tabs",
            "serviceAction": [
              {
                "objectType": "demo.TabMenu",
                "id": "tabDemo",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.TabMenu/1/actions/tabDemo",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          }
        ],
        "unreferencedActions": null
      },
      {
        "named": "Extensions",
        "cssClassFa": null,
        "section": [
          {
            "named": "SecMan",
            "serviceAction": [
              {
                "objectType": "demo.CausewayExtSecManMenu",
                "id": "appTenancy",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.CausewayExtSecManMenu/1/actions/appTenancy",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          }
        ],
        "unreferencedActions": null
      },
      {
        "named": "Experimental",
        "cssClassFa": null,
        "section": [
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "demo.TupleDemoMenu",
                "id": "tupleDemo",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.TupleDemoMenu/1/actions/tupleDemo",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demo.JeeMenu",
                "id": "jeeInjectDemo",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.JeeMenu/1/actions/jeeInjectDemo",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          }
        ],
        "unreferencedActions": null
      },
      {
        "named": "Other",
        "cssClassFa": null,
        "unreferencedActions": true
      }
    ]
  },
  "secondary": {
    "menu": [
      {
        "named": "Prototyping",
        "cssClassFa": null,
        "section": [
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "causewayExtFixtures.FixtureScripts",
                "id": "runFixtureScript",
                "named": "Run Fixture Script",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayExtFixtures.FixtureScripts/1/actions/runFixtureScript",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewayExtFixtures.FixtureScripts",
                "id": "recreateObjectsAndReturnFirst",
                "named": "Recreate Objects And Return First",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayExtFixtures.FixtureScripts/1/actions/recreateObjectsAndReturnFirst",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "causewayApplib.LayoutServiceMenu",
                "id": "downloadLayouts",
                "named": "Download Object Layouts (ZIP)",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayApplib.LayoutServiceMenu/1/actions/downloadLayouts",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewayApplib.LayoutServiceMenu",
                "id": "downloadMenuBarsLayout",
                "named": "Download Menu Bars Layout (XML)",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayApplib.LayoutServiceMenu/1/actions/downloadMenuBarsLayout",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "causewayApplib.MetaModelServiceMenu",
                "id": "downloadMetaModelXml",
                "named": "Download Meta Model (XML)",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayApplib.MetaModelServiceMenu/1/actions/downloadMetaModelXml",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewayApplib.MetaModelServiceMenu",
                "id": "downloadMetaModelCsv",
                "named": "Download Meta Model (CSV)",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayApplib.MetaModelServiceMenu/1/actions/downloadMetaModelCsv",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "causewayJdoDn5.JdoMetamodelMenu",
                "id": "downloadMetamodels",
                "named": "Download JDO Metamodels (ZIP)",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayJdoDn5.JdoMetamodelMenu/1/actions/downloadMetamodels",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "causewayApplib.SwaggerServiceMenu",
                "id": "openSwaggerUi",
                "named": "Open Swagger Ui",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayApplib.SwaggerServiceMenu/1/actions/openSwaggerUi",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewayApplib.SwaggerServiceMenu",
                "id": "openRestApi",
                "named": "Open Rest Api",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayApplib.SwaggerServiceMenu/1/actions/openRestApi",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewayApplib.SwaggerServiceMenu",
                "id": "downloadSwaggerSchemaDefinition",
                "named": "Download Swagger Schema Definition",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayApplib.SwaggerServiceMenu/1/actions/downloadSwaggerSchemaDefinition",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "causewayApplib.TranslationServicePoMenu",
                "id": "downloadTranslations",
                "named": "Download Translations",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayApplib.TranslationServicePoMenu/1/actions/downloadTranslations",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewayApplib.TranslationServicePoMenu",
                "id": "resetTranslationCache",
                "named": "Clear translation cache",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayApplib.TranslationServicePoMenu/1/actions/resetTranslationCache",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewayApplib.TranslationServicePoMenu",
                "id": "switchToReadingTranslations",
                "named": "Switch To Reading Translations",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayApplib.TranslationServicePoMenu/1/actions/switchToReadingTranslations",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewayApplib.TranslationServicePoMenu",
                "id": "switchToWritingTranslations",
                "named": "Switch To Writing Translations",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayApplib.TranslationServicePoMenu/1/actions/switchToWritingTranslations",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "causewayExtH2Console.H2ManagerMenu",
                "id": "openH2Console",
                "named": "H2 Console",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayExtH2Console.H2ManagerMenu/1/actions/openH2Console",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "Prototype Actions",
            "serviceAction": [
              {
                "objectType": "demoapp.PrototypeActionsVisibilityAdvisor",
                "id": "showPrototypeActions",
                "named": "Show",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demoapp.PrototypeActionsVisibilityAdvisor/1/actions/showPrototypeActions",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demoapp.PrototypeActionsVisibilityAdvisor",
                "id": "doNotShowPrototypeActions",
                "named": "Do not Show",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demoapp.PrototypeActionsVisibilityAdvisor/1/actions/doNotShowPrototypeActions",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          }
        ],
        "unreferencedActions": null
      },
      {
        "named": "Activity",
        "cssClassFa": null,
        "section": [
          {
            "named": "Command Log",
            "serviceAction": [
              {
                "objectType": "causewayExtensionsCommandLog.CommandServiceMenu",
                "id": "activeCommands",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayExtensionsCommandLog.CommandServiceMenu/1/actions/activeCommands",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewayExtensionsCommandLog.CommandServiceMenu",
                "id": "findCommands",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayExtensionsCommandLog.CommandServiceMenu/1/actions/findCommands",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewayExtensionsCommandLog.CommandServiceMenu",
                "id": "findCommandById",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayExtensionsCommandLog.CommandServiceMenu/1/actions/findCommandById",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewayExtensionsCommandLog.CommandServiceMenu",
                "id": "truncateLog",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayExtensionsCommandLog.CommandServiceMenu/1/actions/truncateLog",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "Command Replay - Primary",
            "serviceAction": [
              {
                "objectType": "causewayExtensionsCommandReplayPrimary.CommandReplayOnPrimaryService",
                "id": "findCommands",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayExtensionsCommandReplayPrimary.CommandReplayOnPrimaryService/1/actions/findCommands",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewayExtensionsCommandReplayPrimary.CommandReplayOnPrimaryService",
                "id": "downloadCommands",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayExtensionsCommandReplayPrimary.CommandReplayOnPrimaryService/1/actions/downloadCommands",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewayExtensionsCommandReplayPrimary.CommandReplayOnPrimaryService",
                "id": "downloadCommandById",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayExtensionsCommandReplayPrimary.CommandReplayOnPrimaryService/1/actions/downloadCommandById",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "Command Replay - Secondary",
            "serviceAction": [
              {
                "objectType": "causewayExtensionsCommandReplaySecondary.CommandReplayOnSecondaryService",
                "id": "findMostRecentReplayed",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayExtensionsCommandReplaySecondary.CommandReplayOnSecondaryService/1/actions/findMostRecentReplayed",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewayExtensionsCommandReplaySecondary.CommandReplayOnSecondaryService",
                "id": "uploadCommands",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayExtensionsCommandReplaySecondary.CommandReplayOnSecondaryService/1/actions/uploadCommands",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": "Demo Replay Controller",
            "serviceAction": [
              {
                "objectType": "demoapp.web.DemoReplayController",
                "id": "pauseReplay",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demoapp.web.DemoReplayController/1/actions/pauseReplay",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "demoapp.web.DemoReplayController",
                "id": "resumeReplay",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demoapp.web.DemoReplayController/1/actions/resumeReplay",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          }
        ],
        "unreferencedActions": null
      },
      {
        "named": "Security",
        "cssClassFa": null,
        "section": [
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "causewaysecurity.ApplicationRoleMenu",
                "id": "allRoles",
                "named": "All Roles",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.ApplicationRoleMenu/1/actions/allRoles",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewaysecurity.ApplicationRoleMenu",
                "id": "newRole",
                "named": "New Role",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.ApplicationRoleMenu/1/actions/newRole",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewaysecurity.ApplicationRoleMenu",
                "id": "findRoles",
                "named": "Find Roles",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.ApplicationRoleMenu/1/actions/findRoles",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "causewaysecurity.ApplicationTenancyMenu",
                "id": "newTenancy",
                "named": "New Tenancy",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.ApplicationTenancyMenu/1/actions/newTenancy",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewaysecurity.ApplicationTenancyMenu",
                "id": "findTenancies",
                "named": "Find Tenancies",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.ApplicationTenancyMenu/1/actions/findTenancies",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewaysecurity.ApplicationTenancyMenu",
                "id": "allTenancies",
                "named": "All Tenancies",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.ApplicationTenancyMenu/1/actions/allTenancies",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "causewaysecurity.ApplicationPermissionMenu",
                "id": "findOrphanedPermissions",
                "named": "Find Orphaned Permissions",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.ApplicationPermissionMenu/1/actions/findOrphanedPermissions",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewaysecurity.ApplicationPermissionMenu",
                "id": "allPermissions",
                "named": "All Permissions",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.ApplicationPermissionMenu/1/actions/allPermissions",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "causewaysecurity.ApplicationFeatureViewModels",
                "id": "allProperties",
                "named": "All Properties",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.ApplicationFeatureViewModels/1/actions/allProperties",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewaysecurity.ApplicationFeatureViewModels",
                "id": "allClasses",
                "named": "All Classes",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.ApplicationFeatureViewModels/1/actions/allClasses",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewaysecurity.ApplicationFeatureViewModels",
                "id": "allPackages",
                "named": "All Packages",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.ApplicationFeatureViewModels/1/actions/allPackages",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewaysecurity.ApplicationFeatureViewModels",
                "id": "allActions",
                "named": "All Actions",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.ApplicationFeatureViewModels/1/actions/allActions",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewaysecurity.ApplicationFeatureViewModels",
                "id": "allCollections",
                "named": "All Collections",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.ApplicationFeatureViewModels/1/actions/allCollections",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "causewaysecurity.ApplicationUserMenu",
                "id": "findUsers",
                "named": "Find Users",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.ApplicationUserMenu/1/actions/findUsers",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewaysecurity.ApplicationUserMenu",
                "id": "newLocalUser",
                "named": "New Local User",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.ApplicationUserMenu/1/actions/newLocalUser",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewaysecurity.ApplicationUserMenu",
                "id": "allUsers",
                "named": "All Users",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.ApplicationUserMenu/1/actions/allUsers",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewaysecurity.ApplicationUserMenu",
                "id": "newDelegateUser",
                "named": "New Delegate User",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.ApplicationUserMenu/1/actions/newDelegateUser",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          }
        ],
        "unreferencedActions": null
      }
    ]
  },
  "tertiary": {
    "menu": [
      {
        "named": "",
        "cssClassFa": null,
        "section": [
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "causewaysecurity.MeService",
                "id": "me",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaysecurity.MeService/1/actions/me",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              },
              {
                "objectType": "causewayApplib.ConfigurationMenu",
                "id": "configuration",
                "named": null,
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewayApplib.ConfigurationMenu/1/actions/configuration",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          },
          {
            "named": null,
            "serviceAction": [
              {
                "objectType": "causewaySecurityApi.LogoutMenu",
                "id": "logout",
                "named": "Logout",
                "namedEscaped": null,
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/causewaySecurityApi.LogoutMenu/1/actions/logout",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                }
              }
            ]
          }
        ],
        "unreferencedActions": null
      }
    ]
  },
  "metadataError": null
}
"""
}
