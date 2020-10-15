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

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.extensions.restclient.ResponseDigest;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.rospec.Configuration_usingRoSpec;
import org.apache.isis.testdomain.util.rest.RestEndpointService;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.IsisModuleViewerRestfulObjectsJaxrsResteasy4;

import lombok.val;

@SpringBootTest(
        classes = {
                RestEndpointService.class
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

    @Test
    void string() {
        val digest = getDigest(String.class, "string");
        val returnValue = digest.getEntities().getSingletonOrFail();
        assertEquals("aString", returnValue);
    }
    
    // -- HELPER
    
    public <T> ResponseDigest<T> getDigest(Class<T> entityType, String actionName) {
        
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
    
    
}
