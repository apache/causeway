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
package org.apache.isis.testdomain.rest;

import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.extensions.cors.impl.IsisModuleExtCorsImpl;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.rospec.Configuration_usingRoSpec;
import org.apache.isis.testdomain.util.rest.RestEndpointService;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.IsisModuleViewerRestfulObjectsJaxrsResteasy4;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {RestEndpointService.class, IsisModuleExtCorsImpl.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
        Configuration_headless.class,
        Configuration_usingRoSpec.class,
        IsisModuleViewerRestfulObjectsJaxrsResteasy4.class,
})
// see: https://github.com/predix/spring-cors-filter/blob/master/src/test/java/com/ge/predix/web/cors/test/CORSFilterTest.java
// see: https://www.baeldung.com/intercepting-filter-pattern-in-java
class CorsFilterTest {

    @LocalServerPort
    private int port;

    @Test
    // FALSE_POSITIVE:
    // regardless what is set in IsisModuleExtCorsIml ("restful" vs. "restful/*)
    // a (sensible) json response is returned
    public void ensureUrlCanBeAccessed() throws Exception{
        final String url = "http://localhost:" + port + "/restful/user";
        final TestRestTemplate testRestTemplate = new TestRestTemplate();
        final ResponseEntity<String> response = testRestTemplate
                //Credentials are not checked, only occurrence of HTTP/BA field in request header
                .withBasicAuth("any", "any")
                .getForEntity(url, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertTrue(response.getBody().length() > 0);
    }

    @Autowired
    private WebApplicationContext wac;

    @Test
    public void testCors() throws Exception {
        final RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("http://localhost:" + port + "/restful/user")
                .header("Access-Control-Request-Method", "GET")
                .header("Origin", "http://localhost");
        final CorsFilter corsFilter = createIsisCorsFilter();
        final DefaultMockMvcBuilder builder = MockMvcBuilders
                .webAppContextSetup(this.wac)
                .addFilter(corsFilter, "/**");
        final MockMvc mockMvc = builder.build();
        mockMvc
                .perform(requestBuilder)
                .andDo(print())
                //ServletResource not available.  //Set BP in PathResourceResolver.getResource:186
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,PUT,DELETE,POST,OPTIONS"));
    }

    private CorsFilter createIsisCorsFilter() {
        final MetaModelContext metaModelContext = MetaModelContext_forTesting.buildDefault();
        final ConfigurableEnvironment environment = metaModelContext.getConfiguration().getEnvironment();
        final IsisConfiguration configuration = new IsisConfiguration(environment);
        final Map<String, String> map = new HashMap<>();

        configuration.setIsisSettings(map);

        final IsisModuleExtCorsImpl isisExtCors = new IsisModuleExtCorsImpl(configuration);
        isisExtCors.corsFilterRegistration();
        return isisExtCors.corsFilter();
    }

    @Test
    public void testRequestExpectStandardCorsResponse() throws ServletException, IOException {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/invalid");
        request.addHeader("Origin", "*");

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final FilterChain filterChain = new MockFilterChain();
        final Filter corsFilter = createIsisCorsFilter();
        corsFilter.doFilter(request, response, filterChain);

        assertEquals("*", response.getHeaderValue("Access-Control-Allow-Origin"));
    }

    @Test //requestUrl is not seen as invalid. Why?
    // BP -> CorsFilter.doFilterInternal:87
    public void testRequestWithForbiddenUri() throws ServletException, IOException {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/invalid");
        request.addHeader("Origin", "*");

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final Filter corsFilter = createIsisCorsFilter();
        final FilterChain filterChain = new FilterChainImpl(corsFilter);
        corsFilter.doFilter(request, response, filterChain);

        assertEquals(403, response.getStatus());
    }

    class FilterChainImpl implements FilterChain {
        private Iterator<Filter> filters;

        public FilterChainImpl(Filter... filters) {
            this.filters = Arrays.asList(filters).iterator();
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            if (filters.hasNext()) {
                Filter filter = filters.next();
                filter.doFilter(request, response, this);
            }
        }
    }

}
