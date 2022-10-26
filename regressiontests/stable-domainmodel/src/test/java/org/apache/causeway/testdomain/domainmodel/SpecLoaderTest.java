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
package org.apache.causeway.testdomain.domainmodel;

import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.good.Configuration_usingValidDomain;

import lombok.val;

@SpringBootTest(
        classes = { 
                Configuration_headless.class,
                Configuration_usingValidDomain.class,
                
        }, 
        properties = {
                "causeway.core.meta-model.introspector.mode=FULL",
                "causeway.applib.annotation.domain-object.editing=TRUE",
                "causeway.core.meta-model.validator.explicit-object-type=FALSE", // does not override any of the imports
                "logging.level.DependentArgUtils=DEBUG"
        })
@TestPropertySource({
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
class SpecLoaderTest {
    
    @Inject private SpecificationLoader specificationLoader;
    
    @ParameterizedTest
    @MethodSource("providePrimitiveTypes")
    void primitiveRoundtrip_shouldSucceed(Class<?> type) {
        
        val spec1 = specificationLoader.loadSpecification(type);
        assertNotNull(spec1);
        
        val logicalType = spec1.getLogicalType();
        
        val spec2 = specificationLoader.specForLogicalType(logicalType).orElse(null);
        assertNotNull(spec2);
        
        assertEquals(spec1.getLogicalType(), spec2.getLogicalType());
    }
    
    private static Stream<Class<?>> providePrimitiveTypes() {
        return Stream.of(boolean.class,
                byte.class,
                short.class,
                int.class,
                long.class,
                float.class,
                double.class,
                char.class);
    }

}
