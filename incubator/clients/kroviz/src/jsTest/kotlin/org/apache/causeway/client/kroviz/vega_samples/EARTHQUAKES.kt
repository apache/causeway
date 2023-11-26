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

object EARTHQUAKES {
    val schema = "\$schema"

    val url = ""
    val str = """
{
    "$schema": "https://vega.github.io/schema/vega/v5.json",
    "description": "An interactive globe depicting earthquake locations and magnitudes.",
    "padding": 10,
    "width": 450,
    "height": 450,
    "autosize": "none",

    "signals": [
    {
        "name": "quakeSize", "value": 6,
        "bind": {"input": "range", "min": 0, "max": 12}
    },
    {
        "name": "rotate0", "value": 90,
        "bind": {"input": "range", "min": -180, "max": 180}
    },
    {
        "name": "rotate1", "value": -5,
        "bind": {"input": "range", "min": -180, "max": 180}
    }
    ],

    "data": [
    {
        "name": "sphere",
        "values": [
        {"type": "Sphere"}
        ]
    },
    {
        "name": "world",
        "url": "data/world-110m.json",
        "format": {
        "type": "topojson",
        "feature": "countries"
    }
    },
    {
        "name": "earthquakes",
        "url": "data/earthquakes.json",
        "format": {
        "type": "json",
        "property": "features"
    }
    }
    ],

    "projections": [
    {
        "name": "projection",
        "scale": 225,
        "type": "orthographic",
        "translate": {"signal": "[width/2, height/2]"},
        "rotate": [{"signal": "rotate0"}, {"signal": "rotate1"}, 0]
    }
    ],

    "scales": [
    {
        "name": "size",
        "type": "sqrt",
        "domain": [0, 100],
        "range": [0, {"signal": "quakeSize"}]
    }
    ],

    "marks": [
    {
        "type": "shape",
        "from": {"data": "sphere"},
        "encode": {
        "update": {
        "fill": {"value": "aliceblue"},
        "stroke": {"value": "black"},
        "strokeWidth": {"value": 1.5}
    }
    },
        "transform": [
        {
            "type": "geoshape",
            "projection": "projection"
        }
        ]
    },
    {
        "type": "shape",
        "from": {"data": "world"},
        "encode": {
        "update": {
        "fill": {"value": "mintcream"},
        "stroke": {"value": "black"},
        "strokeWidth": {"value": 0.35}
    }
    },
        "transform": [
        {
            "type": "geoshape",
            "projection": "projection"
        }
        ]
    },
    {
        "type": "shape",
        "from": {"data": "earthquakes"},
        "encode": {
        "update": {
        "opacity": {"value": 0.25},
        "fill": {"value": "red"}
    }
    },
        "transform": [
        {
            "type": "geoshape",
            "projection": "projection",
            "pointRadius": {"expr": "scale('size', exp(datum.properties.mag))"}
        }
        ]
    }
    ]
}
"""
}