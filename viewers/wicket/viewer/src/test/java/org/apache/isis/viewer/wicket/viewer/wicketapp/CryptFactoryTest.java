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
package org.apache.isis.viewer.wicket.viewer.wicketapp;

import java.util.stream.Stream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.ThreadContext;
import org.apache.wicket.authentication.strategy.DefaultAuthenticationStrategy;
import org.apache.wicket.mock.MockWebResponse;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.apache.wicket.util.crypt.ICrypt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import lombok.val;

/**
 * Requires {@code javax/servlet/http/LocalStrings.properties}
 */
class CryptFactoryTest {

    private ServletWebRequest servletWebRequest;
    private MockWebResponse mockWebResponse;
    private HttpServletRequest mockHttpServletRequest;

    @BeforeEach
    void setUp() throws Exception {

        // quite a bit of ceremony to allow Cookie testing ...

        mockHttpServletRequest = new org.springframework.mock.web.MockHttpServletRequest() {
            @Override
            public Cookie[] getCookies() {
                return mockWebResponse.getCookies().toArray(new Cookie[] {});
            }
        };

        servletWebRequest = new ServletWebRequest(mockHttpServletRequest, "", Url.parse("/"));
        mockWebResponse = new MockWebResponse();

        ThreadContext.setRequestCycle(new RequestCycle(new RequestCycleContext(
                servletWebRequest,
                mockWebResponse,
                mock(IRequestMapper.class),
                mock(IExceptionMapper.class))));

    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideCryptoCandidates")
    void authenticationStrategyRoundtrip(
            final String displayName,
            final ICrypt crypto) {

        val strategy1 =
                new DefaultAuthenticationStrategy("cookieKey", crypto);
        strategy1.save("hello", "world", "appendix");

        val strategy2 =
                new DefaultAuthenticationStrategy("cookieKey", crypto);
        val data = strategy2.load();

        assertNotNull(data);
        assertEquals(2, data.length);
        assertEquals("hello", data[0]);
        assertEquals("world", data[1]);
    }

    private static Stream<Arguments> provideCryptoCandidates() {

        return Stream.of(
          Arguments.of(
                  "sunJceCrypt",
                  _CryptFactory.sunJceCrypt("encryptionKey")),
          Arguments.of(
                  "aesCrypt",
                  _CryptFactory.aesCrypt("encryptionKey"))
        );
    }


}
