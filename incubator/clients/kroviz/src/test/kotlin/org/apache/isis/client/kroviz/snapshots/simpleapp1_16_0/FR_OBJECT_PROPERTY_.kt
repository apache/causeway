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
package org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0

import org.apache.isis.client.kroviz.snapshots.Response

object FR_OBJECT_PROPERTY_ : Response() {
    override val url = "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGZpeHR1cmVTY3JpcHRDbGFzc05hbWU-ZG9tYWluYXBwLm1vZHVsZXMuc2ltcGxlLmZpeHR1cmUuU2ltcGxlT2JqZWN0X3BlcnNvbmEkUGVyc2lzdEFsbDwvZml4dHVyZVNjcmlwdENsYXNzTmFtZT48a2V5PmRvbWFpbi1hcHAtZGVtby9wZXJzaXN0LWFsbC9pdGVtLTE8L2tleT48b2JqZWN0LmJvb2ttYXJrPnNpbXBsZS5TaW1wbGVPYmplY3Q6MjcwPC9vYmplY3QuYm9va21hcms-PC9tZW1lbnRvPg==/properties/object"
    override val str = """{
        "id": "object",
        "memberType": "property",
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGZpeHR1cmVTY3JpcHRDbGFzc05hbWU-ZG9tYWluYXBwLm1vZHVsZXMuc2ltcGxlLmZpeHR1cmUuU2ltcGxlT2JqZWN0X3BlcnNvbmEkUGVyc2lzdEFsbDwvZml4dHVyZVNjcmlwdENsYXNzTmFtZT48a2V5PmRvbWFpbi1hcHAtZGVtby9wZXJzaXN0LWFsbC9pdGVtLTE8L2tleT48b2JqZWN0LmJvb2ttYXJrPnNpbXBsZS5TaW1wbGVPYmplY3Q6MjcwPC9vYmplY3QuYm9va21hcms-PC9tZW1lbnRvPg==/properties/object",
                "method": "GET",
                "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
            },
            {
                "rel": "up",
                "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGZpeHR1cmVTY3JpcHRDbGFzc05hbWU-ZG9tYWluYXBwLm1vZHVsZXMuc2ltcGxlLmZpeHR1cmUuU2ltcGxlT2JqZWN0X3BlcnNvbmEkUGVyc2lzdEFsbDwvZml4dHVyZVNjcmlwdENsYXNzTmFtZT48a2V5PmRvbWFpbi1hcHAtZGVtby9wZXJzaXN0LWFsbC9pdGVtLTE8L2tleT48b2JqZWN0LmJvb2ttYXJrPnNpbXBsZS5TaW1wbGVPYmplY3Q6MjcwPC9vYmplY3QuYm9va21hcms-PC9tZW1lbnRvPg==",
                "method": "GET",
                "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "domain-app-demo/persist-all/item-1:  Object: Foo"
            },
            {
                "rel": "describedby",
                "href": "http://localhost:8080/restful/domain-types/isisApplib.FixtureResult/properties/object",
                "method": "GET",
                "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
            }
        ],
        "value": {
            "rel": "urn:org.restfulobjects:rels/value",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/270",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Object: Foo"
        },
        "disabledReason": "Non-cloneable view models are read-only; Immutable"
    }"""
}
