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
package org.apache.isis.testdomain.rest;

import java.util.function.UnaryOperator;

import javax.inject.Inject;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.extensions.cors.impl.IsisModuleExtCorsImpl;
import org.apache.isis.extensions.restclient.ResponseDigest;
import org.apache.isis.extensions.restclient.log.ClientConversationFilter;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.rospec.Configuration_usingRoSpec;
import org.apache.isis.testdomain.rospec.RoSpecSampler;
import org.apache.isis.testdomain.util.rest.RestEndpointService;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.IsisModuleViewerRestfulObjectsJaxrsResteasy4;

import lombok.val;

@SpringBootTest(
        classes = {
                RestEndpointService.class
        },
        properties = {
                //preparing the CORS filter with specific settings for testing
                "isis.extensions.cors.allowedMethods=POST",
                "isis.extensions.cors.allowedOrigins=http://www.google.com",
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(IsisPresets.UseLog4j2Test)
@Import({
    Configuration_headless.class,
    Configuration_usingRoSpec.class,
    IsisModuleViewerRestfulObjectsJaxrsResteasy4.class,
    IsisModuleExtCorsImpl.class
})
@TestMethodOrder(OrderAnnotation.class) // run tests in sequence, to ease debugging
class CorsFilterTest {

    @LocalServerPort int port; 
    @Inject RestEndpointService restService;

    private final RoSpecSampler refSampler = new RoSpecSampler();
    private final Can<ClientConversationFilter> conversationFilters = Can.empty();
    
    // -- STRING

    @Test @Order(1) 
    void requestWithValidOriginAndMethod_shouldSucceed() {
        val digest = digestUsingPost("string", String.class, builder->builder
                .header("Origin", validOrigin()));
        assertHttpResponse200(digest);
    }

    @Test @Order(2) 
    void requestWithInvalidOrigin_shouldFail() {
        val digest = digestUsingPost("string", String.class, builder->builder
                .header("Origin", invalidOrigin()));
        assertHttpResponse403(digest);
    }
    
    @Test @Order(3) 
    void requestWithMissingOrigin_shouldSucceed() {
        val digest = digestUsingPost("string", String.class, builder->builder);
        assertHttpResponse200(digest);
    }

    @Test @Order(4)
    void requestWithInvalidMethod_shouldFail() {
        val digest = digestUsingGet("stringSafe", String.class, builder->builder
                .header("Origin", validOrigin()));
        assertHttpResponse403(digest);
    }

    // -- HELPER

    <T> ResponseDigest<T> digestUsingPost(
            String actionName, 
            Class<T> entityType, 
            UnaryOperator<javax.ws.rs.client.Invocation.Builder> onRequestBuilder) {

        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = true;
        val client = restService.newClient(useRequestDebugLogging, conversationFilters);

        val request = onRequestBuilder.apply( 
                restService.newInvocationBuilder(client, 
                        String.format("services/testdomain.RoSpecSampler/actions/%s/invoke", actionName)));
        
        val args = client.arguments() 
                .build();
        
        val response = request.post(args);
        val digest = client.digest(response, entityType);

        return digest;

    }
    
    <T> ResponseDigest<T> digestUsingGet(
            String actionName, 
            Class<T> entityType, 
            UnaryOperator<javax.ws.rs.client.Invocation.Builder> onRequestBuilder) {

        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = true;
        val client = restService.newClient(useRequestDebugLogging, conversationFilters);

        val request = onRequestBuilder.apply( 
                restService.newInvocationBuilder(client, 
                        String.format("services/testdomain.RoSpecSampler/actions/%s/invoke", actionName)));

        val response = request.get();
        val digest = client.digest(response, entityType);

        return digest;
    }
    
    
    private String validOrigin() {
        return "http://www.google.com";
    }
    
    private String invalidOrigin() {
        return "http://localhost";
    }
   
    private void assertHttpResponse200(ResponseDigest<String> digest) {
        if(!digest.isSuccess()) {
            fail(digest.getFailureCause());
        }
        val returnValue = digest.getEntities().getSingletonOrFail();
        assertEquals(refSampler.string(), returnValue);
    }
    
    private void assertHttpResponse403(ResponseDigest<String> digest) {
        assertNotNull(digest.getFailureCause(), "request was expected to fail, but succeeded");
        assertTrue(digest.getFailureCause().getMessage().contains("403"));
    }

}
