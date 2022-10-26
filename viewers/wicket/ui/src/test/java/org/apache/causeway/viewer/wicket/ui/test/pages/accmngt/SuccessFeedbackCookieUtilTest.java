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
package org.apache.causeway.viewer.wicket.ui.test.pages.accmngt;

import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.ThreadContext;
import org.apache.wicket.mock.MockWebResponse;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.causeway.viewer.wicket.ui.pages.accmngt.SuccessFeedbackCookieManager;

import lombok.val;

class SuccessFeedbackCookieUtilTest {

    private ServletWebRequest servletWebRequest;
    private MockWebResponse mockWebResponse;
    private HttpServletRequest mockHttpServletRequest;

    @BeforeEach
    void setUp() throws Exception {

        mockHttpServletRequest = mock(HttpServletRequest.class);
        servletWebRequest = new ServletWebRequest(mockHttpServletRequest, "", Url.parse("/"));
        mockWebResponse = new MockWebResponse();

        ThreadContext.setRequestCycle(new RequestCycle(new RequestCycleContext(
                servletWebRequest,
                mockWebResponse,
                mock(IRequestMapper.class),
                mock(IExceptionMapper.class))));
    }

    @Test
    void roundtrip() {

        val message = "Hallo x@abc.com what`s up 'dear'?!.";

        // store cookie with response
        SuccessFeedbackCookieManager.storeSuccessFeedback(message);

        // fake request to return cookies that were just written to the response
        when(mockHttpServletRequest.getCookies())
        .thenReturn(mockWebResponse.getCookies().toArray(new Cookie[] {}));


        // verify that we can read the message from the cookie
        val resultRef = new AtomicReference<String>();

        SuccessFeedbackCookieManager.drainSuccessFeedback(feedback->{
            resultRef.set(feedback);
        });

        assertEquals(message, resultRef.get());
    }

}
