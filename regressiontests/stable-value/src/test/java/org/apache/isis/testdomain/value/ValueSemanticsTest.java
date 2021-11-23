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

import java.util.Locale;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.util.schema.CommonDtoUtils;
import org.apache.isis.applib.value.Password;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.config.valuetypes.ValueSemanticsRegistry;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.schema.cmd.v2.PropertyDto;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.valuetypes.Configuration_usingValueTypes;
import org.apache.isis.testdomain.model.valuetypes.ValueTypeExample;
import org.apache.isis.testdomain.model.valuetypes.ValueTypeExampleService;
import org.apache.isis.testdomain.model.valuetypes.ValueTypeExampleService.Scenario;

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

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideValueTypeExamples")
    <T> void valueTypes(
            final String name,
            final Class<T> valueType,
            final ValueTypeExample<T> example) {
        assertNotNull(example);

        val tester = serviceInjector.injectServicesInto(
                new ValueSemanticsTester<T>(example.getValueType(), example));

        tester.propertyInteraction("value",
                interactionContext(),
                managedProp->example.getUpdateValue(),
                (context, codec)->{

                    // CoderDecoder round-trip test
                    val serialized = codec.toEncodedString(example.getValue());

                    tester.assertValueEquals(
                            example.getValue(),
                            codec.fromEncodedString(serialized),
                            "serialization roundtrip failed");

                },
                (context, parser)->{

                    // Parser round-trip test
                    val stringified = parser.parseableTextRepresentation(context, example.getValue());

                    if(valueType.equals(Password.class)) {
                        val recoveredValue = (Password)parser.parseTextRepresentation(context, stringified);
                        assertTrue(recoveredValue.checkPassword("*"));

                    } else {

                        tester.assertValueEquals(
                                example.getValue(),
                                parser.parseTextRepresentation(context, stringified),
                                "parser roundtrip failed");
                    }

                },
                (context, renderer)->{

                },
                (command, codec)->{

                    val propertyDto = (PropertyDto)command.getCommandDto().getMember();
                    val newValueRecordedDto = propertyDto.getNewValue();

                    //TODO needs a codec to recover values that are not directly represented by the schema
                    val newValueRecorded = CommonDtoUtils.getValue(newValueRecordedDto);

                    // TODO skip tests, because some value-types are not represented by the schema yet
                    if(newValueRecorded==null
                            || valueType.equals(Bookmark.class)) {
                        System.err.printf("skipping command test on %s%n", valueType.getName());
                        return;
                    }

                    assertEquals(valueType, newValueRecorded.getClass(), ()->
                        String.format("command value parsing type mismatch '%s'",
                                ValueSemanticsTester.valueDtoToXml(newValueRecordedDto)));

                    tester.assertValueEquals(example.getUpdateValue(), newValueRecorded, "command failed");

//                    //debug
//                    System.err.printf("Value %s %s%n", name,
//                            valueToXml(newValueRecordedDto));
//
//                    //debug
//                    System.err.printf("CommandDto %s %s%n", name,
//                            CommandDtoUtils.toXml(
//                                    command.getCommandDto()));
                });

    }

//    @Test
//    void list() {
//        valueSemanticsRegistry.streamClassesWithValueSemantics()
//        .forEach(valueType->System.err.printf("%s%n", valueType.getSimpleName()));
//    }

    // -- HELPER

    private InteractionContext interactionContext() {
        return InteractionContext.builder().locale(Locale.ENGLISH).build();
    }

    // -- DEPENDENCIES

    @Inject ValueTypeExampleService valueTypeExampleProvider;
    @Inject SpecificationLoader specLoader;
    @Inject InteractionService interactionService;
    @Inject ServiceInjector serviceInjector;
    @Inject ValueSemanticsRegistry valueSemanticsRegistry;

    Stream<Arguments> provideValueTypeExamples() {
        return valueTypeExampleProvider.streamScenarios()
                .map(Scenario::getArguments);
    }

}
