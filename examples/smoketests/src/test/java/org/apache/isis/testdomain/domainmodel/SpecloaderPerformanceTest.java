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
package org.apache.isis.testdomain.domainmodel;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.commons.internal.base._Timing;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.IsisPresets;
import org.apache.isis.config.registry.IsisBeanTypeRegistry;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.testdomain.Incubating;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.good.Configuration_usingValidDomain;

import static org.junit.jupiter.api.Assertions.fail;

import lombok.val;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_headless.class,
                Configuration_usingValidDomain.class
        }, 
        properties = {
                "isis.reflector.introspector.mode=FULL",
                "isis.reflector.validator.explicitObjectType=FALSE", // does not override any of the imports
        })
@TestPropertySource({
    IsisPresets.SilenceMetaModel,
    //IsisPresets.DebugProgrammingModel,
})
@Incubating("not a real test, just for performance tuning")
class SpecloaderPerformanceTest {
    
    @Inject private IsisConfiguration config;
    @Inject private SpecificationLoader specificationLoader;
    
    @BeforeAll
    static void setup() {
        IsisBeanTypeRegistry.repeatedTesting = true;
    }
    
    static long ITERATIONS = 100; /* should typically run in ~10s */
    static long EXPECTED_MILLIS_PER_ITERATION = 100;
    
    @Test 
    void repeatedConcurrentSpecloading_shouldNotDeadlock() {
        
        config.getReflector().getIntrospector().setParallelize(true);
        
        val timeOutMillis = ITERATIONS * EXPECTED_MILLIS_PER_ITERATION;
        val goodUntilMillis = System.currentTimeMillis() + timeOutMillis;
        
        val repeatedRun = (Runnable)()->{
        
            for(int i=0; i<ITERATIONS; ++i) {
                _Annotations.clearCache();
                specificationLoader.disposeMetaModel();
                specificationLoader.createMetaModel();
           
                if(System.currentTimeMillis() > goodUntilMillis) {
                    fail("timed out");
                }
            }
            
        };
        
        _Timing.runVerbose("Repeated Concurrent Specloading", repeatedRun);
    }


}
