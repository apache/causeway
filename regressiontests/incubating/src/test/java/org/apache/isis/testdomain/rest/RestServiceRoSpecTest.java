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

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.extensions.restclient.ResponseDigest;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.rospec.Configuration_usingRoSpec;
import org.apache.isis.testdomain.rospec.Customer;
import org.apache.isis.testdomain.rospec.RoSpecSampler;
import org.apache.isis.testdomain.util.rest.RestEndpointService;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.IsisModuleViewerRestfulObjectsJaxrsResteasy4;

import static org.apache.isis.testdomain.util.CollectionAssertions.assertComponentWiseEquals;

import lombok.val;

@SpringBootTest(
        classes = {
                RestEndpointService.class
        },
        properties = {
                "logging.level.org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationServiceContentNegotiator=DEBUG"
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(IsisPresets.UseLog4j2Test)
@Import({
    Configuration_headless.class,
    Configuration_usingRoSpec.class,
    IsisModuleViewerRestfulObjectsJaxrsResteasy4.class,
})
class RestServiceRoSpecTest {

    @LocalServerPort int port; // just for reference (not used)
    @Inject RestEndpointService restService;
    
    private RoSpecSampler refSampler = new RoSpecSampler(); 

    // -- STRING
    
    @Test
    void string() {
        val digest = digest("string", String.class);
        val returnValue = digest.getEntities().getSingletonOrFail();
        assertEquals(refSampler.string(), returnValue);
    }
    
    @Test
    void stringNull() {
        val digest = digest("stringNull", String.class);
        assertTrue(digest.getEntities().isEmpty());        
    }
    
    // -- STRING ARRAY
    
    @Test 
    void stringArray() {
        val digest = digestList("stringArray", String.class, new GenericType<List<String>>(){});
        val returnValue = digest.getEntities().getSingletonOrFail();
        assertComponentWiseEquals(refSampler.stringArray(), returnValue);
    }
    
    @Test 
    void stringArrayEmpty() {
        val digest = digestList("stringArrayEmpty", String.class, new GenericType<List<String>>(){});
        assertTrue(digest.getEntities().isEmpty());
    }
    
    @Test 
    void stringArrayNull() {
        val digest = digestList("stringArrayNull", String.class, new GenericType<List<String>>(){});
        assertTrue(digest.getEntities().isEmpty());
    }
    
    // -- STRING LIST
    
    @Test 
    void stringList() {
        val digest = digestList("stringList", String.class, new GenericType<List<String>>(){});
        val returnValues = digest.getEntities();
        assertComponentWiseEquals(refSampler.stringList(), returnValues);
    }
    
    @Test 
    void stringListEmpty() {
        val digest = digestList("stringListEmpty", String.class, new GenericType<List<String>>(){});
        assertTrue(digest.getEntities().isEmpty());
    }
    
    @Test 
    void stringListNull() {
        val digest = digestList("stringListNull", String.class, new GenericType<List<String>>(){});
        assertTrue(digest.getEntities().isEmpty());
    }
    
    // -- INT
    
    @Test
    void integer() {
        val digest = digest("integer", Integer.class);
        val returnValue = digest.getEntities().getSingletonOrFail();
        assertEquals(refSampler.integer(), returnValue);
    }
    
    @Test
    void integerNull() {
        val digest = digest("integerNull", Integer.class);
        assertTrue(digest.getEntities().isEmpty());        
    }
    
    @Test
    void integerPrimitive() {
        val digest = digest("integerPrimitive", int.class);
        val returnValue = digest.getEntities().getSingletonOrFail();
        assertEquals(refSampler.integerPrimitive(), returnValue);
    }
    
    // -- CUSTOMER
    
    @Test 
    void customer() {
        val digest = digest("customer", Customer.class);
        val returnValue = digest.getEntities().getSingletonOrFail();
        assertEquals(refSampler.customer(), returnValue);
    }
    
    @Test 
    void customerNull() {
        val digest = digest("customerNull", Customer.class);
        assertTrue(digest.getEntities().isEmpty());
    }
    
    @Test
    void customerList() {
        val digest = digestList("customerList", Customer.class, new GenericType<List<Customer>>(){});
        val returnValues = digest.getEntities();
        assertComponentWiseEquals(refSampler.customerList(), returnValues);
    }
    
    @Test
    void customerListEmpty() {
        val digest = digestList("customerListEmpty", Customer.class, new GenericType<List<Customer>>(){});
        assertTrue(digest.getEntities().isEmpty());
    }
    
    @Test
    void customerListNull() {
        val digest = digestList("customerListNull", Customer.class, new GenericType<List<Customer>>(){});
        assertTrue(digest.getEntities().isEmpty());
    }
    
    // -- HELPER
    
    public <T> ResponseDigest<T> digest(String actionName, Class<T> entityType) {
        
        _Probe.errOut("");
        _Probe.errOut("=== %s", actionName);
        _Probe.errOut("");
        
        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = true;
        val client = restService.newClient(useRequestDebugLogging);
        
        val request = restService.newInvocationBuilder(client, 
                String.format("services/testdomain.RoSpecSampler/actions/%s/invoke", actionName)); 

        val args = client.arguments()
                .build();

        val response = request.post(args);
        val digest = client.digest(response, entityType);
        
        if(!digest.isSuccess()) {
            fail(digest.getFailureCause());
        }
        
        return digest;

    }
    
    public <T> ResponseDigest<T> digestList(
            String actionName, 
            Class<T> entityType, 
            GenericType<List<T>> genericType) {
        
        _Probe.errOut("");
        _Probe.errOut("=== %s", actionName);
        _Probe.errOut("");
        
        assertTrue(restService.getPort()>0);

        val useRequestDebugLogging = true;
        val client = restService.newClient(useRequestDebugLogging);
        
        val request = restService.newInvocationBuilder(client, 
                String.format("services/testdomain.RoSpecSampler/actions/%s/invoke", actionName)); 

        val args = client.arguments()
                .build();

        val response = request.post(args);
        val digest = client.digestList(response, entityType, genericType);
        
        if(!digest.isSuccess()) {
            fail(digest.getFailureCause());
        }
        
        return digest;

    }
    
    
}
