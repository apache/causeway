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
package org.apache.causeway.viewer.restfulobjects.viewer.webmodule;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.viewer.restfulobjects.viewer.webmodule.CausewayRestfulObjectsInteractionFilter2.Config;

class CausewayRestfulObjectsSessionFilter_lookupPassThru_Test2 {

    CausewayRestfulObjectsInteractionFilter2 causewayInteractionFilter;

    CausewayRestfulObjectsInteractionFilter2.Config config;

    @BeforeEach
    void setUp() throws Exception {
        causewayInteractionFilter = new CausewayRestfulObjectsInteractionFilter2();
    }

    @Test
    void when_null() throws Exception {
        config = Config.builder().passThru(null).build();

        final List<String> x = causewayInteractionFilter.lookupAndParsePassThru(config);
        assertEquals(0, x.size());
    }

    @Test
    void when_none() throws Exception {
        config = Config.builder().passThru("").build();

        final List<String> x = causewayInteractionFilter.lookupAndParsePassThru(config);
        assertEquals(0, x.size());
    }

    @Test
    void when_one() throws Exception {
        config = Config.builder().passThru("/abc").build();

        final List<String> x = causewayInteractionFilter.lookupAndParsePassThru(config);
        assertEquals(1, x.size());
        assertEquals("/abc", x.get(0));
    }

    @Test
    void when_several() throws Exception {
        config = Config.builder().passThru("/abc,/def").build();

        final List<String> x = causewayInteractionFilter.lookupAndParsePassThru(config);
        assertEquals(2, x.size());
        assertEquals("/abc", x.get(0));
        assertEquals("/def", x.get(1));
    }

}
