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
package org.apache.causeway.testdomain.value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.graph.tree.TreeNode;
import org.apache.causeway.applib.locale.UserLocale;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.value.Password;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.applib.value.semantics.ValueSemanticsResolver;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.schema.SchemaValueMarshaller;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.schema.cmd.v2.PropertyDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.valuetypes.Configuration_usingValueTypes;
import org.apache.causeway.testdomain.model.valuetypes.ValueTypeExample;
import org.apache.causeway.testdomain.model.valuetypes.ValueTypeExampleService;
import org.apache.causeway.testdomain.model.valuetypes.ValueTypeExampleService.Scenario;
import org.apache.causeway.testdomain.value.ValueSemanticsTester.PropertyInteractionProbe;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;
import org.apache.causeway.valuetypes.markdown.applib.value.Markdown;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingValueTypes.class,
        },
        properties = {
                "causeway.core.meta-model.introspector.mode=FULL"
        })
@TestPropertySource({
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
@TestInstance(Lifecycle.PER_CLASS)
class ValueSemanticsTest {

    /* debug
    static class TestEnvironment {

        private Locale systemLocale;
        private TimeZone systemTimeZone;

        TestEnvironment() {
            systemLocale = Locale.getDefault();
            systemTimeZone = TimeZone.getDefault();

            if(!Objects.equals(systemLocale, Locale.US)) {
                System.err.println("DEBUG: setting test Locale to US");
                //log.warn("setting test Locale to US");
                Locale.setDefault(Locale.US);
            }

            if(!Objects.equals(systemTimeZone, TimeZone.getTimeZone("GMT"))) {
                System.err.println("DEBUG: setting test TimeZone to GMT");
                //log.warn("setting test TimeZone to GMT");
                TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            }
        }

        void cleanup() {
            Locale.setDefault(systemLocale);
            TimeZone.setDefault(systemTimeZone);
        }

    }
     */

    @Test
    void fullTypeCoverage() {

        valueSemanticsResolver.streamClassesWithValueSemantics()
        .forEach(valueType->System.err.printf("%s%n", valueType.getName()));

        final Set<Class<?>> valueTypesCovered = valueTypeExampleService.streamExamples()
        .map(ValueTypeExample::getValueType)
        .collect(Collectors.toSet());

        final Set<Class<?>> valueTypesKnown = valueSemanticsResolver.streamClassesWithValueSemantics()
        .collect(Collectors.toSet());

        //TODO[CAUSEWAY-2877] yet excluded from coverage ...
        valueTypesKnown.remove(TreeNode.class);
        valueTypesKnown.remove(Markdown.class);
        valueTypesKnown.remove(AsciiDoc.class);

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

        //val env = new TestEnvironment();

        assertNotNull(example);

        val tester = serviceInjector.injectServicesInto(
                new ValueSemanticsTester<T>(example.getValueType(), example));

        tester.propertyInteraction("value",
                interactionContext(),
                managedProp->example.getUpdateValue(),
                new PropertyInteractionProbe<T>() {

                    @Override
                    public void testComposer(
                            final ValueSemanticsProvider.Context context,
                            final ValueSemanticsProvider<T> semantics) {

                        // composer round-trip test
                        val decomposition = semantics.decompose(example.getValue());

                        tester.assertValueEquals(
                                example.getValue(),
                                semantics.compose(decomposition),
                                "decompose/compose roundtrip failed");

                        // json roundtrip test
                        {
                            val json = decomposition.toJson();
                            assertNotNull(json);
                            assertFalse(json.isBlank());

                            val reconstructed = ValueDecomposition
                                    .fromJson(semantics.getSchemaValueType(), json);

                            assertEquals(json, reconstructed.toJson());

                        }

                        if(semantics.getSchemaValueType()==ValueType.COMPOSITE) {
                            System.err.printf("WARN: ValueSemanticsTest for COMPOSITE %s not implemented.%n",
                                    semantics);

                            val valueMixin = (Object)null;
                            if(valueMixin!=null) {

                                val spec = specLoader.specForTypeElseFail(valueMixin.getClass());
                                val interaction = ActionInteraction
                                        .start(ManagedObject.mixin(spec,  valueMixin), "act", Where.ANYWHERE);

                                val pendingParams = interaction
                                        .startParameterNegotiation()
                                        .get();

                                val managedAction = interaction.getManagedActionElseFail();
                                val typedTuple = pendingParams.getParamValues();

                                val recoveredValue = managedAction
                                        .invoke(typedTuple, InteractionInitiatedBy.PASS_THROUGH)
                                        .getSuccessElseFail()
                                        .getPojo();

                                tester.assertValueEquals(
                                        example.getValue(),
                                        recoveredValue,
                                        "serialization roundtrip failed");
                            }
                        }

                    }

                    @Override
                    public void testParser(
                            final ValueSemanticsProvider.Context context,
                            final Parser<T> parser) {

                        example.getParseExpectations()
                        .forEach(parseExpectation->{
                            val value = parseExpectation.getValue();

                            if(parseExpectation.getExpectedThrows()!=null) {

                                // test parsing that should throw
                                parseExpectation.getInputSamples()
                                .forEach(in->{
                                    Assertions.assertThrows(parseExpectation.getExpectedThrows(), ()->{
                                        parser.parseTextRepresentation(context, in);
                                    });
                                });

                            } else {

                                // test parsing that should not throw
                                parseExpectation.getInputSamples()
                                .forEach(in->{
                                    val parsedValue = parser.parseTextRepresentation(context, in);

                                    if(value instanceof BigDecimal) {
                                        assertNumberEquals((BigDecimal)value, (BigDecimal)parsedValue);
                                    } else {
                                        assertEquals(value, parsedValue);
                                    }

                                });

                                // test formatting
                                assertEquals(
                                        parseExpectation.getExpectedOutput(),
                                        parser.parseableTextRepresentation(context, value));

                            }

                        });

                        if(example.getParseExpectations().isNotEmpty()) {
                            return; // skip round-trip test
                        }

                        //TODO eventually all examples should have their ParseExpectations, so we can remove
                        // Parser round-trip test
                        for(val value : example.getParserRoundtripExamples()) {

                            val stringified = parser.parseableTextRepresentation(context, value);

                            if(valueType.equals(Password.class)) {
                                val recoveredValue = (Password)parser.parseTextRepresentation(context, stringified);
                                assertTrue(recoveredValue.checkPassword("(suppressed)"));

                            } else {

                                assertValueEqualsParsedValue(context, parser, value, stringified, "parser roundtrip failed");

                                if(valueType.equals(OffsetDateTime.class)
                                        || valueType.equals(OffsetTime.class)
                                        //|| valueType.equals(ZonedDateTime.class)
                                        ) {

                                    if(stringified.endsWith("Z")) {
                                        // skip format variations on UTC time-zone
                                        //System.err.printf("DEBUG: skipping stringified: %s%n", stringified);
                                        return;
                                    }

                                    val with4digitZone = _Strings.substring(stringified, 0, -3) + "00";
                                    val with2digitZone = _Strings.substring(stringified, 0, -3);

                                    // test alternative time-zone format with 4 digits +-HHmm
                                    assertValueEqualsParsedValue(context, parser, value, with4digitZone, "parser roundtrip failed "
                                            + "(alternative time-zone format with 4 digits +-HHmm)");

                                    // test alternative time-zone format with 2 digits +-HH
                                    assertValueEqualsParsedValue(context, parser, value, with2digitZone, "parser roundtrip failed "
                                            + "(alternative time-zone format with 2 digits +-HH)");
                                }

                            }
                        }
                    }
                    @Override
                    public void testRenderer(
                            final ValueSemanticsProvider.Context context,
                            final Renderer<T> renderer) {

                        example.getRenderExpectations()
                        .forEach(renderExpectation->{
                            val value = renderExpectation.getValue();
                            assertEquals(renderExpectation.getTitle(), renderer.titlePresentation(context, value));
                            assertEquals(renderExpectation.getHtml(), renderer.htmlPresentation(context, value));
                        });
                    }

                    @Override
                    public void testCommand(
                            final ValueSemanticsProvider.Context context,
                            final Command command) {

                        val propertyDto = (PropertyDto)command.getCommandDto().getMember();
                        val newValueRecordedDto = propertyDto.getNewValue();

                        val newValueRecorded = valueMarshaller.recoverPropertyFrom(propertyDto);
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

                    private void assertValueEqualsParsedValue(
                            final ValueSemanticsProvider.Context context,
                            final Parser<T> parser,
                            final T value,
                            final String textRepresentation,
                            final String failureMessage) {

                        //debug
                        System.err.printf("using %s trying to parse '%s' to value %s%n",
                                valueType.getName(), textRepresentation, ""+value);

                        val parsedValue = Try.call(()->
                            parser.parseTextRepresentation(context, textRepresentation));

                        if(parsedValue.isFailure()) {
                            Assertions.fail(failureMessage, parsedValue.getFailure().get());
                        }

                        tester.assertValueEquals(
                                value,
                                parsedValue.getValue().orElse(null),
                                failureMessage);
                    }

                });

       // env.cleanup();

    }

    // -- HELPER

    private static void assertNumberEquals(final BigDecimal a, final BigDecimal b) {
        val maxScale = Math.max(a.scale(), b.scale());
        assertEquals(
                a.setScale(maxScale),
                b.setScale(maxScale));
    }

    private InteractionContext interactionContext() {
        return InteractionContext.builder().locale(UserLocale.valueOf(Locale.ENGLISH)).build();
    }

    // -- DEPENDENCIES

    @Inject ValueTypeExampleService valueTypeExampleService;
    @Inject SpecificationLoader specLoader;
    @Inject InteractionService interactionService;
    @Inject ServiceInjector serviceInjector;
    @Inject ValueSemanticsResolver valueSemanticsResolver;
    @Inject SchemaValueMarshaller valueMarshaller;

    Stream<Arguments> provideValueTypeExamples() {
        return valueTypeExampleService.streamScenarios()
                .map(Scenario::getArguments);
    }

}
