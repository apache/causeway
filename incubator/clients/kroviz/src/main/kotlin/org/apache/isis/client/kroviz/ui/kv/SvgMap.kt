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
package org.apache.isis.client.kroviz.ui.kv

import org.apache.isis.client.kroviz.utils.ScalableVectorGraphic
import pl.treksoft.kvision.maps.*
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.utils.pc

class SvgMap : SimplePanel() {

    @Deprecated("pass in as arg")
    val str = """<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<svg xmlns="http://www.w3.org/2000/svg" contentScriptType="application/ecmascript" contentStyleType="text/css"
     height="203px" preserveAspectRatio="none" style="width:309px;height:203px;" version="1.1" viewBox="0 0 309 203"
     width="309px" zoomAndPan="magnify">
    <defs>
        <filter height="300%" id="f1xj00ih3jrk7f" width="300%" x="-1" y="-1">
            <feGaussianBlur result="blurOut" stdDeviation="2.0"/>
            <feColorMatrix in="blurOut" result="blurOut2" type="matrix"
                           values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 .4 0"/>
            <feOffset dx="4.0" dy="4.0" in="blurOut2" result="blurOut3"/>
            <feBlend in="SourceGraphic" in2="blurOut3" mode="normal"/>
        </filter>
    </defs>
    <g><!--MD5=[f621a9b5735c62a9e50fa7c1e42ea0f4]↵cluster domainapp.modules.simple.dom.impl-->
        <polygon fill="#FFFFFF" filter="url(#f1xj00ih3jrk7f)"
                 points="14,16,277,16,284,38.7999,287,38.7999,287,191,14,191,14,16"
                 style="stroke: #000000; stroke-width: 1.5;"/>
        <line style="stroke: #000000; stroke-width: 1.5;" x1="14" x2="284" y1="38.7999" y2="38.7999"/>
        <text fill="#000000" font-family="sans-serif" font-size="14" font-weight="bold" lengthAdjust="spacingAndGlyphs"
              textLength="257" x="18" y="31.9999">domainapp.modules.simple.dom.impl
        </text><!--MD5=[e6e8857f289496579330543e748c2106]↵class SimpleObject-->
        <rect fill="#FEFECE" filter="url(#f1xj00ih3jrk7f)" height="140.3993" id="SimpleObject"
              style="stroke: #A80036; stroke-width: 1.5;" width="129" x="85.5" y="43"/>
        <ellipse cx="112.2" cy="59" fill="#ADD1B2" rx="11" ry="11" style="stroke: #A80036; stroke-width: 1.0;"/>
        <path d="M111.5594,62.7813 Q112.3563,62.7813 112.9266,62.5859 Q113.4969,62.3906 113.7391,62.1641 Q113.9813,61.9375 114.2234,61.7422 Q114.4656,61.5469 114.6844,61.5469 Q115.0281,61.5469 115.2859,61.8047 Q115.5438,62.0625 115.5438,62.3906 Q115.5438,63.125 114.3797,63.8047 Q113.2156,64.4844 111.5125,64.4844 Q109.3563,64.4844 107.9344,63.2188 Q106.5125,61.9531 106.5125,60.0156 L106.5125,58.8906 Q106.5125,56.8594 107.8406,55.4766 Q109.1688,54.0938 111.1375,54.0938 Q112.325,54.0938 113.5438,54.6719 L113.7156,54.75 Q114.0125,54.3438 114.4344,54.3438 Q114.9188,54.3438 115.0984,54.6328 Q115.2781,54.9219 115.2781,55.4688 L115.2781,56.8906 Q115.2781,58.0156 114.4344,58.0156 Q114.1219,58.0156 113.9266,57.8438 Q113.7313,57.6719 113.6922,57.5234 Q113.6531,57.375 113.6063,57.1094 Q113.4969,56.5938 112.9813,56.2813 Q112.4656,55.9688 112.0203,55.8828 Q111.575,55.7969 111.2,55.7969 Q109.9188,55.7969 109.0672,56.6719 Q108.2156,57.5469 108.2156,58.8906 L108.2156,59.9844 Q108.2156,61.2656 109.1219,62.0234 Q110.0281,62.7813 111.5594,62.7813 Z "/>
        <text fill="#000000" font-family="sans-serif" font-size="12" lengthAdjust="spacingAndGlyphs" textLength="71"
              x="128.8" y="63.8">SimpleObject
        </text>
        <line style="stroke: #A80036; stroke-width: 1.5;" x1="86.5" x2="213.5" y1="75" y2="75"/>
        <line style="stroke: #A80036; stroke-width: 1.5;" x1="86.5" x2="213.5" y1="83" y2="83"/>
        <text fill="#000000" font-family="sans-serif" font-size="11" lengthAdjust="spacingAndGlyphs" textLength="93"
              x="91.5" y="97.9999">rebuildMetamodel()
        </text>
        <text fill="#000000" font-family="sans-serif" font-size="11" lengthAdjust="spacingAndGlyphs" textLength="117"
              x="91.5" y="111.1998">downloadJdoMetadata()
        </text>
        <text fill="#000000" font-family="sans-serif" font-size="11" lengthAdjust="spacingAndGlyphs" textLength="69"
              x="91.5" y="124.3997">openRestApi()
        </text>
        <text fill="#000000" font-family="sans-serif" font-size="11" lengthAdjust="spacingAndGlyphs" textLength="104"
              x="91.5" y="137.5996">downloadLayoutXml()
        </text>
        <text fill="#000000" font-family="sans-serif" font-size="11" lengthAdjust="spacingAndGlyphs" textLength="37"
              x="91.5" y="150.7995">delete()
        </text>
        <text fill="#000000" font-family="sans-serif" font-size="11" lengthAdjust="spacingAndGlyphs" textLength="70"
              x="91.5" y="163.9994">updateName()
        </text>
        <text fill="#000000" font-family="sans-serif" font-size="11" lengthAdjust="spacingAndGlyphs" textLength="56"
              x="91.5" y="177.1993">clearHints()
        </text><!--MD5=[903507c5920b0ecb2f83cd94e1c42ae5]↵@startuml
↵package domainapp.modules.simple.dom.impl {
↵class SimpleObject
↵SimpleObject : rebuildMetamodel()
↵SimpleObject : downloadJdoMetadata()
↵SimpleObject : openRestApi()
↵SimpleObject : downloadLayoutXml()
↵SimpleObject : delete()
↵SimpleObject : updateName()
↵SimpleObject : clearHints()
↵}
↵@enduml
↵↵PlantUML version 1.2020.04(Thu Mar 19 10:16:49 GMT 2020)↵(GPL source distribution)↵Java Runtime: OpenJDK Runtime Environment↵JVM: OpenJDK 64-Bit Server VM↵Java Version: 1.8.0_191-b12↵Operating System: Linux↵Default Encoding: UTF-8↵Language: en↵Country: US↵-->
    </g>
</svg>
"""

    init {
        // 0 0 is in the Atlantic Ocean, south of 'towel states' -> blue background
        val map = maps(0, 0, 11, baseLayerProvider = BaseLayerProvider.EMPTY, crs = CRS.Simple) {
            width = 100.pc
            height = 100.pc
        }

        val svg = ScalableVectorGraphic(str)
        svg.scaleHorizontally()
        val svgDoc = svg.document
        val svgElement = svgDoc.documentElement!!

        val bounds = LatLngBounds(
                LatLng(0, 0),
                LatLng(0.1, 0.1))
        map.svgOverlay(svgElement, bounds)
    }

}
