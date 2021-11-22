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
package org.apache.isis.testdomain.value;

import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.valuetypes.Configuration_usingValueTypes;
import org.apache.isis.testdomain.model.valuetypes.ValueTypeExample;
import org.apache.isis.testdomain.model.valuetypes.ValueTypeExampleService;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingValueTypes.class,
        },
        properties = {
                "isis.core.meta-model.introspector.mode=FULL"
        })
@TestPropertySource({
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
@TestInstance(Lifecycle.PER_CLASS)
class ValueSemanticsTest {

    //@Inject ValueSemanticsRegistry valueSemanticsRegistry;
    @Inject ValueTypeExampleService valueTypeExampleProvider;
    @Inject SpecificationLoader specLoader;

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideValueTypeExamples")
    void valueTypes(
            final String name,
            final ValueTypeExample<?> example) {
        assertNotNull(example);
        System.err.printf("%s%n", example);

        val exampleSpec = specLoader.specForTypeElseFail(example.getClass());

        val act = exampleSpec.getActionElseFail("updateValue");
        val prop = exampleSpec.getPropertyElseFail("value");
        val coll = exampleSpec.getCollectionElseFail("values");

        assertNotNull(act);
        assertNotNull(prop);
        assertNotNull(coll);

    }

    // -- DEPENDENCIES

    Stream<Arguments> provideValueTypeExamples() {
        return valueTypeExampleProvider.streamExamples()
                .map(x->Arguments.of(x.getValueType().getSimpleName(), x));
    }

}
