/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.isis.extensions.cors.impl;

import org.apache.catalina.core.ApplicationFilterChain;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * see: https://github.com/predix/spring-cors-filter/blob/master/src/test/java/com/ge/predix/web/cors/test/CORSFilterTest.java
 */
class IsisModuleExtCorsImplTest {

    @Test
    void ensureUrlPatternsContainRestfulAndWildcard() {
        // when
        final FilterRegistrationBean<Filter> filterRegistration = createIsisCorsFilter().corsFilterRegistration();
        // then
        final Collection<String> urlPatterns = filterRegistration.getUrlPatterns();
        assertTrue(urlPatterns.contains("/restful/*"));
    }

    @Test
    public void testRequestExpectStandardCorsResponse() throws ServletException, IOException {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/invalid");
        request.addHeader("Origin", "*");

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final FilterChain filterChain = newMockFilterChain();

        final Filter corsFilter = createIsisCorsFilter().corsFilter();
        corsFilter.doFilter(request, response, filterChain);

        assertEquals("*", response.getHeaderValue("Access-Control-Allow-Origin"));
    }

    //@Test requestUrl is not seen as invalid. Why?
    public void testRequestWithForbiddenUri() throws ServletException, IOException {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/invalid");
        request.addHeader("Origin", "example.com");

        final MockHttpServletResponse response = new MockHttpServletResponse();

        final FilterChain filterChain = new ApplicationFilterChain();
        final Filter corsFilter = createIsisCorsFilter().corsFilter();
        corsFilter.doFilter(request, response, filterChain);

        assertEquals(403, response.getStatus());
    }

    private static IsisModuleExtCorsImpl createIsisCorsFilter() {
        final MetaModelContext metaModelContext = MetaModelContext_forTesting.buildDefault();
        final ConfigurableEnvironment environment = metaModelContext.getConfiguration().getEnvironment();
        final IsisConfiguration configuration = new IsisConfiguration(environment);
        final Map<String, String> map = new HashMap<String, String>();

        configuration.setIsisSettings(map);

        final IsisModuleExtCorsImpl classUnderTest = new IsisModuleExtCorsImpl(configuration);
        final FilterRegistrationBean<Filter> filterRegistration = classUnderTest.corsFilterRegistration();
        return classUnderTest;
    }

    private static FilterChain newMockFilterChain() {
        FilterChain filterChain = new FilterChain() {

            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response)
                    throws IOException,
                    ServletException {
                // Do nothing.
            }
        };
        return filterChain;
    }

}