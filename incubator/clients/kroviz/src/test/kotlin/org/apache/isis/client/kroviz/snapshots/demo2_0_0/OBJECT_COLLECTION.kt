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

package org.apache.isis.client.kroviz.snapshots.demo2_0_0

import org.apache.isis.client.kroviz.snapshots.Response

object OBJECT_COLLECTION: Response(){
    override val url = "http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/collections/entities"
    override val str = """
{
  "id": "entities",
  "memberType": "collection",
  "links": [
    {
      "rel": "self",
      "href": "http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/collections/entities",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-collection\""
    },
    {
      "rel": "up",
      "href": "http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
      "title": "String data type"
    },
    {
      "rel": "describedby",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStrings/collections/entities",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/collection-description\""
    }
  ],
  "extensions": {
    "collectionSemantics": "list"
  },
  "value": [
    {
      "rel": "urn:org.restfulobjects:rels/value",
      "href": "http://localhost:8080/restful/objects/demo.JavaLangStringEntity/1",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
      "title": "StringJDO entity: Hello"
    },
    {
      "rel": "urn:org.restfulobjects:rels/value",
      "href": "http://localhost:8080/restful/objects/demo.JavaLangStringEntity/2",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
      "title": "StringJDO entity: world"
    }
  ]
}
    """
}
