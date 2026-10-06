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
package org.apache.causeway.core.webapp.modules.observation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.apache.causeway.core.config.observation.CausewayTraceClassifier;
import org.apache.causeway.core.config.observation.CausewayTraceClassifier.ExecutionMode;

class CausewayForegroundTraceFilterTest {
    @Test void classifiesEveryEntryPathBeforeDownstreamWork() throws Exception {
        var classifier = mock(CausewayTraceClassifier.class);
        var filter = new CausewayForegroundTraceFilter(classifier);
        for (String path : new String[]{"/wicket/", "/graphql", "/restful/", "/static/test.js"}) {
            clearInvocations(classifier);
            var request = new MockHttpServletRequest("GET", path);
            filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
                verify(classifier).classifyCurrentSpan(ExecutionMode.FOREGROUND);
                // A nested dispatch must not classify a child span.
                filter.doFilter(req, res, (nestedReq, nestedRes) -> {});
            });
            verifyNoMoreInteractions(classifier);
        }
    }
    @Test void propagatesFailureAndCleansRequestMarker() throws Exception {
        var classifier = mock(CausewayTraceClassifier.class);
        var filter = new CausewayForegroundTraceFilter(classifier);
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var failure = new ServletException("downstream");
        assertSame(failure, assertThrows(ServletException.class,
                () -> filter.doFilter(request, response, (req, res) -> { throw failure; })));
        filter.doFilter(request, response, (req, res) -> {});
        verify(classifier, times(2)).classifyCurrentSpan(ExecutionMode.FOREGROUND);
    }
    @Test void skipsAsyncAndErrorRedispatch() throws Exception {
        var classifier = mock(CausewayTraceClassifier.class);
        var filter = new CausewayForegroundTraceFilter(classifier);
        for (var type : new DispatcherType[]{DispatcherType.ASYNC, DispatcherType.ERROR}) {
            var request = new MockHttpServletRequest();
            request.setDispatcherType(type);
            if (type == DispatcherType.ERROR) request.setAttribute("jakarta.servlet.error.request_uri", "/failed");
            filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {});
        }
        verifyNoInteractions(classifier);
    }
}
