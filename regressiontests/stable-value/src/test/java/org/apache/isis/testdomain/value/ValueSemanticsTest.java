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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.locale.UserLocale;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.value.Password;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.applib.value.semantics.ValueSemanticsResolver;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.services.schema.SchemaValueMarshaller;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.schema.cmd.v2.PropertyDto;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.valuetypes.Configuration_usingValueTypes;
import org.apache.isis.testdomain.model.valuetypes.ValueTypeExample;
import org.apache.isis.testdomain.model.valuetypes.ValueTypeExampleService;
import org.apache.isis.testdomain.model.valuetypes.ValueTypeExampleService.Scenario;
import org.apache.isis.testdomain.value.ValueSemanticsTester.PropertyInteractionProbe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test @Disabled
    void fullTypeCoverage() {

        valueSemanticsResolver.streamClassesWithValueSemantics()
        .forEach(valueType->System.err.printf("%s%n", valueType.getName()));

        final Set<Class<?>> valueTypesCovered = valueTypeExampleProvider.streamExamples()
        .map(ValueTypeExample::getValueType)
        .collect(Collectors.toSet());

        final Set<Class<?>> valueTypesKnown = valueSemanticsResolver.streamClassesWithValueSemantics()
        .collect(Collectors.toSet());

        val valueTypesNotCovered = _Sets.minus(valueTypesKnown, valueTypesCovered);

        assertTrue(valueTypesNotCovered.isEmpty(), ()->
            String.format("value-types not covered by tests:\n\t%s",
                    valueTypesNotCovered.stream()
                    .map(Class::getName)
                    .collect(Collectors.joining("\n\t"))));
    }

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
                new PropertyInteractionProbe<T>() {

                    @Override
                    public void testEncoderDecoder(
                            final ValueSemanticsProvider.Context context,
                            final EncoderDecoder<T> composer) {
                        val valueMixin = composer.getValueMixin(example.getValue());
                        if(valueMixin!=null) {

                            val spec = specLoader.specForTypeElseFail(valueMixin.getClass());
                            val interaction = ActionInteraction
                                    .start(ManagedObject.of(spec,  valueMixin), "act", Where.ANYWHERE);

                            val pendingParams = interaction
                                    .startParameterNegotiation()
                                    .get();

                            val managedAction = interaction.getManagedActionElseFail();
                            val typedTuple = pendingParams.getParamValues();

                            val recoveredValue = managedAction
                                    .invoke(typedTuple, InteractionInitiatedBy.PASS_THROUGH)
                                    .leftIfAny()
                                    .getPojo();

                            tester.assertValueEquals(
                                    example.getValue(),
                                    recoveredValue,
                                    "serialization roundtrip failed");

                            return;
                        }

                        // CoderDecoder round-trip test
                        val serialized = composer.toEncodedString(example.getValue());

                        tester.assertValueEquals(
                                example.getValue(),
                                composer.fromEncodedString(serialized),
                                "serialization roundtrip failed");
                    }

                    @Override
                    public void testParser(
                            final ValueSemanticsProvider.Context context,
                            final Parser<T> parser) {
                        // Parser round-trip test
                        val stringified = parser.parseableTextRepresentation(context, example.getValue());

                        if(valueType.equals(Password.class)) {
                            val recoveredValue = (Password)parser.parseTextRepresentation(context, stringified);
                            assertTrue(recoveredValue.checkPassword("*"));

                        } else {

                            System.err.printf("using %s trying to parse '%s'%n", valueType.getName(), stringified);

                            tester.assertValueEquals(
                                    example.getValue(),
                                    parser.parseTextRepresentation(context, stringified),
                                    "parser roundtrip failed");
                        }
                    }
                    @Override
                    public void testRenderer(
                            final ValueSemanticsProvider.Context context,
                            final Renderer<T> renderer) {

                    }
                    @Override
                    public void testCommand(
                            final ValueSemanticsProvider.Context context,
                            final Command command,
                            final EncoderDecoder<T> codec) {

                        val propertyDto = (PropertyDto)command.getCommandDto().getMember();
                        val newValueRecordedDto = propertyDto.getNewValue();

                        val newValueRecorded = valueMarshaller.recoverValueFrom(propertyDto);
                        assertNotNull(newValueRecorded);

                        assertEquals(valueType, newValueRecorded.getSpecification().getCorrespondingClass(), ()->
                            String.format("command value parsing type mismatch '%s'",
                                    ValueSemanticsTester.valueDtoToXml(newValueRecordedDto)));

                        tester.assertValueEquals(example.getUpdateValue(), newValueRecorded.getPojo(), "command failed");

//                        //debug
//                        System.err.printf("Value %s %s%n", name,
//                                valueToXml(newValueRecordedDto));
    //
//                        //debug
//                        System.err.printf("CommandDto %s %s%n", name,
//                                CommandDtoUtils.toXml(
//                                        command.getCommandDto()));
                    }
                });

    }

    // -- HELPER

    private InteractionContext interactionContext() {
        return InteractionContext.builder().locale(UserLocale.valueOf(Locale.ENGLISH)).build();
    }

    // -- DEPENDENCIES

    @Inject ValueTypeExampleService valueTypeExampleProvider;
    @Inject SpecificationLoader specLoader;
    @Inject InteractionService interactionService;
    @Inject ServiceInjector serviceInjector;
    @Inject ValueSemanticsResolver valueSemanticsResolver;
    @Inject SchemaValueMarshaller valueMarshaller;

    Stream<Arguments> provideValueTypeExamples() {
        return valueTypeExampleProvider.streamScenarios()
                .map(Scenario::getArguments);
    }

}
