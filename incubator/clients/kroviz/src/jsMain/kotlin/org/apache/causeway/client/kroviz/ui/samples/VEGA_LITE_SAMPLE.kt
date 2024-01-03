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
package org.apache.causeway.client.kroviz.ui.samples

object VEGA_LITE_SAMPLE {
    val schema = "\$schema"

    val url = "https://vega.github.io/vega/examples/packed-bubble-chart/"
    val str = """{
  "$schema" : "https://vega.github.io/schema/vega-lite/v5.json",
  "data" : {
    "values" : [
      {
        "a" : "C",
        "b" : 2
      },
      {
        "a" : "C",
        "b" : 7
      },
      {
        "a" : "C",
        "b" : 4
      },
      {
        "a" : "D",
        "b" : 1
      },
      {
        "a" : "D",
        "b" : 2
      },
      {
        "a" : "D",
        "b" : 6
      },
      {
        "a" : "E",
        "b" : 8
      },
      {
        "a" : "E",
        "b" : 4
      },
      {
        "a" : "E",
        "b" : 7
      }
    ]
  },
  "mark" : "point",
  "encoding" : {
    "x" : {
      "field" : "a",
      "type" : "nominal"
    },
    "y" : {
      "field" : "b",
      "type" : "quantitative"
    }
  }
}
"""
}
