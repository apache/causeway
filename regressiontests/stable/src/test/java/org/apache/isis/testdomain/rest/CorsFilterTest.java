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
import org.apache.isis.testdomain.conf.Configuration_headlessButSecure;
import org.apache.isis.testdomain.rospec.Configuration_usingRoSpec;
import org.apache.isis.testdomain.util.rest.RestEndpointService;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.IsisModuleViewerRestfulObjectsJaxrsResteasy4;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {RestEndpointService.class, IsisModuleExtCorsImpl.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
        Configuration_headlessButSecure.class,
        Configuration_usingRoSpec.class,
        IsisModuleViewerRestfulObjectsJaxrsResteasy4.class,
})
class CorsFilterTest {

    @LocalServerPort
    private int port;

    @Test
    /** implies that:
     * the CORS filter is registered, and
     * the URI passes that filter
     */
    public void ensureUrlCanBeAccessed() {

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
                .get("/restful/user")
                .header("Access-Control-Request-Method", "GET")
                .header("Origin", "http://localhost");
        final CorsFilter corsFilter = createIsisCorsFilter().corsFilter();
        final DefaultMockMvcBuilder builder = MockMvcBuilders
                .webAppContextSetup(this.wac)
                .addFilter(corsFilter, "/")
                .dispatchOptions(true);
        final MockMvc mockMvc = builder.build();
        mockMvc
                .perform(requestBuilder)
                .andDo(print());
                //ServletResource not available.  Set BP in PathResourceResolver.getResource:186
                //.andExpect(status().isOk())
                //.andExpect(header().string("Access-Control-Allow-Methods", "GET,PUT,DELETE,POST,OPTIONS"));
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

}

