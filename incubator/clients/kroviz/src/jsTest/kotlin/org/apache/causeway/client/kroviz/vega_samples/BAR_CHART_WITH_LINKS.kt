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

object BAR_CHART_WITH_LINKS {
    val schema = "\$schema"

    val url = "https://stackoverflow.com/questions/72093927/how-do-you-add-a-clickable-hyperlink-to-a-mark"
    val str = """{
  "$schema": "https://vega.github.io/schema/vega/v5.json",
  "description": "A basic bar chart example, with value labels shown upon mouse hover.",
  "width": 400,
  "height": 200,
  "padding": 5,
  "data": [
    {
      "name": "table",
      "values": [
        {"category": "A", "amount": 28, "link": "http://www.google.com"},
        {"category": "B", "amount": 55, "link": "http://www.google.com"},
        {"category": "C", "amount": 43, "link": "http://www.google.com"},
        {"category": "D", "amount": 91, "link": "http://www.google.com"},
        {"category": "E", "amount": 81, "link": "http://www.google.com"},
        {"category": "F", "amount": 53, "link": "http://www.google.com"},
        {"category": "G", "amount": 19, "link": "http://www.google.com"},
        {"category": "H", "amount": 87, "link": "http://www.google.com"}
      ]
    }
  ],
  "scales": [
    {
      "name": "xscale",
      "type": "band",
      "domain": {"data": "table", "field": "category"},
      "range": "width",
      "padding": 0.05,
      "round": true
    },
    {
      "name": "yscale",
      "domain": {"data": "table", "field": "amount"},
      "nice": true,
      "range": "height"
    }
  ],
  "axes": [
    {"orient": "bottom", "scale": "xscale"},
    {"orient": "left", "scale": "yscale"}
  ],
  "marks": [
    {
      "type": "rect",
      "from": {"data": "table"},
      "encode": {
        "enter": {
          "x": {"scale": "xscale", "field": "category"},
          "width": {"scale": "xscale", "band": 1},
          "y": {"scale": "yscale", "field": "amount"},
          "y2": {"scale": "yscale", "value": 0}
        },
        "update": {
          "fill": {"value": "steelblue"},
          "href": {"signal": "datum.link"}
        },
        "hover": {"fill": {"value": "red"}}
      }
    },
    {
      "type": "text",
      "encode": {
        "enter": {
          "align": {"value": "center"},
          "baseline": {"value": "bottom"},
          "fill": {"value": "#333"}
        },
        "update": {"fillOpacity": [{"value": 1}]}
      }
    }
  ]
}
"""
}
