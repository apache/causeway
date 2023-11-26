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
package org.apache.causeway.client.kroviz.vega_samples

object UML_DIAGRAM {
    val SCHEMA = "\$schema"

    val url = ""
    val str = """{
  "$SCHEMA": "https://vega.github.io/schema/vega/v5.json",
  "width": 800,
  "height": 300,
  "padding": 5,

  "data": [
    {
      "name": "tree",
      "values": [
        {"id": "1", "parent": null, "title": "Animal"},
        {"id": "2", "parent": "1", "title": "Duck"},
        {"id": "3", "parent": "1", "title": "Fish"},
        {"id": "4", "parent": "1", "title": "Zebra"}
      ],
      "transform": [
        {
          "type": "stratify",
          "key": "id",
          "parentKey": "parent"
        },
        {
          "type": "tree",
          "method": "tidy",
          "separation": true,
          "size": [{"signal": "width"}, {"signal": "height"}]
        }
      ]      
    },
    {
      "name": "links",
      "source": "tree",
      "transform": [
        { "type": "treelinks" },
        { "type": "linkpath",
          "shape": "diagonal"
          }
      ]
    }, 
    {
      "name": "tree-boxes",
      "source": "tree",
      "transform": [
          { 
            "type": "filter",
            "expr": "datum.parent == null"
          }
        ]
    },
    {
      "name": "tree-circles",
      "source": "tree",
      "transform": [
        {
          "type": "filter",
          "expr": "datum.parent != null"
        }
      ]
    }
  ],
  "marks": [
    {
      "type": "path",
      "from": {"data": "links"},
      "encode": {
        "enter": {
          "path": {"field": "path"}
        }
      }
    },
    {
      "type": "rect",
      "from": {"data": "tree-boxes"},
      "encode": {
        "enter": {
          "stroke": {"value": "black"},
          "width": {"value": 100},
          "height": {"value": 20},
          "x": {"field": "x"},
          "y": {"field": "y"}
        }
      }
    },
    {
      "type": "symbol",
      "from": {"data": "tree-circles"},
      "encode": {
        "enter": {
          "stroke": {"value": "black"},
          "width": {"value": 100},
          "height": {"value": 20},
          "x": {"field": "x"},
          "y": {"field": "y"}
        }
      }
    },
    {
      "type": "rect",
      "from": {"data": "tree"},
      "encode": {
        "enter": {
          "stroke": {"value": "black"},
          "width": {"value": 100},
          "height": {"value": 20},
          "x": {"field": "x"},
          "y": {"field": "y"}
        }
      }
    },
    {
      "type": "text",
      "from": {"data": "tree"},
      "encode": {
        "enter": {
          "stroke": {"value": "black"},
          "text": {"field": "title"},
          "x": {"field": "x"},
          "y": {"field": "y"},
          "dx": {"value":50},
          "dy": {"value":13},
          "align": {"value": "center"}
        }
      }
    }
  ]
}
"""
}
