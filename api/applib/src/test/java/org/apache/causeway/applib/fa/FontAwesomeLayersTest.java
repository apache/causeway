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
package org.apache.causeway.applib.fa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.commons.internal.base._NullSafe;

class FontAwesomeLayersTest {

    FontAwesomeLayers iconStackSample = FontAwesomeLayers.iconStack(
            "fa-lg",
            "width:1.25em",
            new FontAwesomeLayers.IconEntry("fa-solid fa-layer-group fa-stack-1x",
                    "color:darkgreen;"),
            new FontAwesomeLayers.IconEntry("fa-solid fa-circle-chevron-down fa-stack-1x",
                    "color:Tomato;"
                    + "font-size:0.5em;"
                    + "left:1em;"
                    + "top:1em;"
                    + "background-image:radial-gradient(circle at center, white 20%, transparent 20%);"),
            new FontAwesomeLayers.IconEntry("fa-solid fa-exclamation fa-stack-1x overlay", null));

    @Test
    void builder() {
        var iconStack = FontAwesomeLayers.stackBuilder()
                .containerCssClasses("fa-lg")
                .containerCssStyle("width:1.25em")
                .addIconEntry("fa-solid fa-layer-group fa-stack-1x", "color:darkgreen;")
                .addIconEntry("fa-solid fa-circle-chevron-down fa-stack-1x", "color:Tomato;"
                        + "font-size:0.5em;"
                        + "left:1em;"
                        + "top:1em;"
                        + "background-image:radial-gradient(circle at center, white 20%, transparent 20%);")
                .addIconEntry("fa-solid fa-exclamation fa-stack-1x overlay")
                .build();

        //debug
        //System.err.printf("%s%n", iconStack.toJson());

        assertEquals(iconStackSample, iconStack);
        assertEquals(iconStackSample.toJson(), iconStack.toJson());
    }

    @Test
    void jsonRoundtrip() {

        var iconStack = iconStackSample;

        assertEquals(3, _NullSafe.size(iconStack.getIconEntries()));

        //debug
        //System.err.printf("%s%n", iconStack.toJson());

        var iconStackAfterRoundtrip = FontAwesomeLayers.fromJson(iconStack.toJson());

        //debug
        //System.err.printf("%s%n", iconStackAfterRoundtrip.toJson());

        assertEquals(iconStack, iconStackAfterRoundtrip);

    }

    @Test
    void quickNotation() {
        var iconStackFromQuick = FontAwesomeLayers.fromQuickNotation(
                "solid layer-group .my-color, "
                + "solid circle-chevron-down .my-color .bottom-right-overlay");

        var iconStackReference = FontAwesomeLayers.stackBuilder()
                //.containerCssClasses("fa-lg")
                .containerCssStyle("width:1.25em")
                .addIconEntry("fa-solid fa-layer-group fa-stack-1x my-color")
                .addIconEntry("fa-solid fa-circle-chevron-down fa-stack-1x my-color bottom-right-overlay")
                .build();

        //debug
        //System.err.printf("%s%n", iconStackFromQuick.toJson());

        assertEquals(iconStackReference.toJson(), iconStackFromQuick.toJson());
    }

}
