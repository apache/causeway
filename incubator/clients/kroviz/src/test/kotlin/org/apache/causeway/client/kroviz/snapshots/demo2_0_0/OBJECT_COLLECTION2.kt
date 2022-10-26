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

package org.apache.causeway.client.causeway.snapshots.demo2_0_0

import org.apache.causeway.client.kroviz.snapshots.Response


object OBJECT_COLLECTION2: Response(){
    override val url = "http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/collections/entities"
    override val str = """
{
  "id" : "objects",
  "memberType" : "collection",
  "links" : [ {
    "rel" : "self",
    "href" : "http://localhost:9090/restful/objects/isis.applib.DomainObjectList/PAR-LCAAAAAAAAACFkMFOwzAMhu99iih3knFDU5uJy6QhEIeNBwip1WVKnFJ7g709jspgggM5Ob9_f7_ldvWRkzrBRLFgp2_NQivAUPqIQ6dfduubO62IPfY-FYROn4H0yjVtisRKZpGWoeRO75nHpbWRIhk_-rAHU6bBkhTZW7Hkgto1Sl7LkRO4hXrwJ68ePQ5qy5MEElBr5-Zs9IFlred3lOb6LaDrIRc_jqYv2fB5BDIHYSRBmAqrrF2VnwCPrf0zfk3d9I7m1ItRlGvD_TQcMyCTuxh-lNkHCeq3Jv633VfU95bbS_RvxkwurwcITFYObeulXfMJyMCkw6kBAAA=/collections/objects",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-collection\""
  }, {
    "rel" : "up",
    "href" : "http://localhost:9090/restful/objects/isis.applib.DomainObjectList/PAR-LCAAAAAAAAACFkMFOwzAMhu99iih3knFDU5uJy6QhEIeNBwip1WVKnFJ7g709jspgggM5Ob9_f7_ldvWRkzrBRLFgp2_NQivAUPqIQ6dfduubO62IPfY-FYROn4H0yjVtisRKZpGWoeRO75nHpbWRIhk_-rAHU6bBkhTZW7Hkgto1Sl7LkRO4hXrwJ68ePQ5qy5MEElBr5-Zs9IFlred3lOb6LaDrIRc_jqYv2fB5BDIHYSRBmAqrrF2VnwCPrf0zfk3d9I7m1ItRlGvD_TQcMyCTuxh-lNkHCeq3Jv633VfU95bbS_RvxkwurwcITFYObeulXfMJyMCkw6kBAAA=",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
    "title" : "Domain Object List"
  }, {
    "rel" : "describedby",
    "href" : "http://localhost:9090/restful/domain-types/isis.applib.DomainObjectList/collections/objects",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/collection-description\""
  } ],
  "extensions" : {
    "collectionSemantics" : "list"
  },
  "value" : [ ]
}
"""
}
