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

    @Test
    void jsonRoundtrip() {

        var iconStack = FontAwesomeLayers.iconStack(
                "fa-lg",
                "width:1.25em",
                new FontAwesomeLayers.IconEntry("fa-solid fa-layer-group fa-stack-1x",
                        "color:darkgreen;"),
                new FontAwesomeLayers.IconEntry("fa-solid fa-circle-chevron-down fa-stack-1x",
                        "color:Tomato;"
                        + "font-size:0.5em;"
                        + "left:1em;"
                        + "top:1em;"
                        + "background-image:radial-gradient(at center, white 20%, transparent 20%);"),
                new FontAwesomeLayers.IconEntry("fa-solid fa-circle-chevron-down fa-stack-1x",
                        "color:Tomato;"
                        + "font-size:0.5em;"
                        + "left:-1em;"
                        + "top:1em;"
                        + "background-image:radial-gradient(at center, white 20%, transparent 20%);"));

        assertEquals(3, _NullSafe.size(iconStack.getIconEntries()));


        //debug
        //System.err.printf("%s%n", iconStack.toJson());

        //TODO[CAUSEWAY-3646] work in progress
        //var iconStackAfterRoundtrip = FontAwesomeLayers.fromJson(iconStack.toJson());
        //assertEquals(iconStack, iconStackAfterRoundtrip);

    }

}
