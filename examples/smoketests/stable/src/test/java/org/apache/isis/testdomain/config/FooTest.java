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
package org.apache.isis.testdomain.config;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.Smoketest;

@Smoketest
@SpringBootTest(
        classes = { 
                FooTest.Setup.class
        }, 
        properties = {

                "foo.flag=true",
                "foo.uuid=${random.uuid}",
                "foo.random-schema=test_${random.uuid}",
                "foo.ConnectionURL=jdbc:h2:mem:test"
        })
@TestPropertySource(IsisPresets.UseLog4j2Test)
@EnableConfigurationProperties(FooProperties.class)
class FooTest {
    
    @Configuration
    static class Setup {
        
        @ConfigurationProperties(prefix = "foo")
        @Bean @Named("foo-as-map")
        public Map<String, String> getAsMap() {
            return new HashMap<>();
        }
        
    }

    @Inject 
    private FooProperties foo;
    
    @Inject @Named("foo-as-map") 
    private Map<String, String> fooAsMap;

    @Test
    void foo() {
        assertNotNull(foo);
        assertTrue(foo.isFlag());
        
        assertNotNull(foo.getUuid());
        assertNotNull(foo.getRandomSchema());
        assertNotNull(foo.getConnectionURL());
        
        System.out.println(foo);
        
        assertNotNull(fooAsMap);
        assertFalse(fooAsMap.isEmpty());
        System.out.println(fooAsMap);
        
    }

}
