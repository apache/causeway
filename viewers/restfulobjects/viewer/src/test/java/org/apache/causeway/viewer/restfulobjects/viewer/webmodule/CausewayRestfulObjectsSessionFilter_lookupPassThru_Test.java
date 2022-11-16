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

import javax.servlet.FilterConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CausewayRestfulObjectsSessionFilter_lookupPassThru_Test {

    CausewayRestfulObjectsInteractionFilter causewayInteractionFilter;

    FilterConfig mockFilterConfig;

    @BeforeEach
    void setUp() throws Exception {
        causewayInteractionFilter = new CausewayRestfulObjectsInteractionFilter();
        mockFilterConfig = Mockito.mock(FilterConfig.class);
    }

    @Test
    void when_null() throws Exception {

        Mockito
        .when(mockFilterConfig.getInitParameter(CausewayRestfulObjectsInteractionFilter.PASS_THRU_KEY))
        .thenReturn(null);

        final List<String> x = causewayInteractionFilter.lookupAndParsePassThru(mockFilterConfig);
        assertEquals(0, x.size());
    }

    @Test
    void when_none() throws Exception {

        Mockito
        .when(mockFilterConfig.getInitParameter(CausewayRestfulObjectsInteractionFilter.PASS_THRU_KEY))
        .thenReturn("");

        final List<String> x = causewayInteractionFilter.lookupAndParsePassThru(mockFilterConfig);
        assertEquals(0, x.size());
    }

    @Test
    void when_one() throws Exception {

        Mockito
        .when(mockFilterConfig.getInitParameter(CausewayRestfulObjectsInteractionFilter.PASS_THRU_KEY))
        .thenReturn("/abc");

        final List<String> x = causewayInteractionFilter.lookupAndParsePassThru(mockFilterConfig);
        assertEquals(1, x.size());
        assertEquals("/abc", x.get(0));
    }

    @Test
    void when_several() throws Exception {

        Mockito
        .when(mockFilterConfig.getInitParameter(CausewayRestfulObjectsInteractionFilter.PASS_THRU_KEY))
        .thenReturn("/abc,/def");

        final List<String> x = causewayInteractionFilter.lookupAndParsePassThru(mockFilterConfig);
        assertEquals(2, x.size());
        assertEquals("/abc", x.get(0));
        assertEquals("/def", x.get(1));
    }

}
