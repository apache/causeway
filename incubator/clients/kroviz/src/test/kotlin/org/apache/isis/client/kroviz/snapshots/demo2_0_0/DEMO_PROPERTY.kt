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
package org.apache.isis.client.kroviz.snapshots.demo2_0_0

import org.apache.isis.client.kroviz.snapshots.Response

object DEMO_PROPERTY : Response(){
    override val url = "http://localhost:8080/restful/objects/demo.Text/AR-LCAAAAAAAAACFkLEOgkAMhneeorlJB0Xj4nBASIybi9EHOKExF3s9wx0Kby8ERKIkdvrTv_36pzKpDMEDC6ctR2K9XAlAzmyu-RqJ82m_2ApwXnGuyDJGokYnkjiQOzQ2DqAp6XzRTMcKOgGzjHR2A4NzGfbeePBQktekGeMUzFv3u4G7K-ZWbKBtu-UE7QMYY4-ocstUNzmKXg6BLqUHRWSfDjxWHhwSZn4ADquTMQc3_QZPpf1z6pcayLD75QsMtJyWiwEAAA==/properties/string"
    override val str = """{
    "id": "string",
    "memberType": "property",
    "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/objects/demo.Text/AR-LCAAAAAAAAACFkLEOgkAMhneeorlJB0Xj4nBASIybi9EHOKExF3s9wx0Kby8ERKIkdvrTv_36pzKpDMEDC6ctR2K9XAlAzmyu-RqJ82m_2ApwXnGuyDJGokYnkjiQOzQ2DqAp6XzRTMcKOgGzjHR2A4NzGfbeePBQktekGeMUzFv3u4G7K-ZWbKBtu-UE7QMYY4-ocstUNzmKXg6BLqUHRWSfDjxWHhwSZn4ADquTMQc3_QZPpf1z6pcayLD75QsMtJyWiwEAAA==/properties/string",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
        },
        {
            "rel": "up",
            "href": "http://localhost:8080/restful/objects/demo.Text/AR-LCAAAAAAAAACFkLEOgkAMhneeorlJB0Xj4nBASIybi9EHOKExF3s9wx0Kby8ERKIkdvrTv_36pzKpDMEDC6ctR2K9XAlAzmyu-RqJ82m_2ApwXnGuyDJGokYnkjiQOzQ2DqAp6XzRTMcKOgGzjHR2A4NzGfbeePBQktekGeMUzFv3u4G7K-ZWbKBtu-UE7QMYY4-ocstUNzmKXg6BLqUHRWSfDjxWHhwSZn4ADquTMQc3_QZPpf1z6pcayLD75QsMtJyWiwEAAA==",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "TextDemo"
        },
        {
            "rel": "urn:org.restfulobjects:rels/modify;property=\"string\"",
            "href": "http://localhost:8080/restful/objects/demo.Text/AR-LCAAAAAAAAACFkLEOgkAMhneeorlJB0Xj4nBASIybi9EHOKExF3s9wx0Kby8ERKIkdvrTv_36pzKpDMEDC6ctR2K9XAlAzmyu-RqJ82m_2ApwXnGuyDJGokYnkjiQOzQ2DqAp6XzRTMcKOgGzjHR2A4NzGfbeePBQktekGeMUzFv3u4G7K-ZWbKBtu-UE7QMYY4-ocstUNzmKXg6BLqUHRWSfDjxWHhwSZn4ADquTMQc3_QZPpf1z6pcayLD75QsMtJyWiwEAAA==/properties/string",
            "method": "PUT",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\"",
            "arguments": {
                "value": null
            }
        },
        {
            "rel": "urn:org.restfulobjects:rels/clear;property=\"string\"",
            "href": "http://localhost:8080/restful/objects/demo.Text/AR-LCAAAAAAAAACFkLEOgkAMhneeorlJB0Xj4nBASIybi9EHOKExF3s9wx0Kby8ERKIkdvrTv_36pzKpDMEDC6ctR2K9XAlAzmyu-RqJ82m_2ApwXnGuyDJGokYnkjiQOzQ2DqAp6XzRTMcKOgGzjHR2A4NzGfbeePBQktekGeMUzFv3u4G7K-ZWbKBtu-UE7QMYY4-ocstUNzmKXg6BLqUHRWSfDjxWHhwSZn4ADquTMQc3_QZPpf1z6pcayLD75QsMtJyWiwEAAA==/properties/string",
            "method": "DELETE",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/demo.Text/properties/string",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
        }
    ],
    "value": "a string (click me)",
    "extensions": {
        "x-isis-format": "string"
    }
}"""
}
