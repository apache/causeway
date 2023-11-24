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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.value.Password;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueDecomposition;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.commons.functional.Try;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.services.schema.SchemaValueMarshaller;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.schema.cmd.v2.PropertyDto;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.testdomain.model.valuetypes.ValueTypeExample;
import org.apache.isis.testdomain.value.ValueSemanticsTester.PropertyInteractionProbe;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
class PropertyInteractionProbeImpl<T> implements PropertyInteractionProbe<T> {

    final @NonNull String name;
    final @NonNull Class<T> valueType;
    final @NonNull ValueTypeExample<T> example;
    final @NonNull ValueSemanticsTester<T> tester;

    @Inject private SpecificationLoader specLoader;
    @Inject private SchemaValueMarshaller valueMarshaller;
    @Inject private WrapperFactory wrapperFactory;

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
                        _Assert.assertNumberEquals((BigDecimal)value, (BigDecimal)parsedValue);
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
                    _Utils.valueDtoToXml(newValueRecordedDto)));

        tester.assertValueEquals(example.getUpdateValue(), newValueRecorded.getPojo(), "command failed");

//        //debug
//        System.err.printf("Value %s %s%n", name,
//                valueToXml(newValueRecordedDto));
//
//        //debug
//        System.err.printf("CommandDto %s %s%n", name,
//                CommandDtoUtils.toXml(
//                        command.getCommandDto()));
    }

    private void assertValueEqualsParsedValue(
            final ValueSemanticsProvider.Context context,
            final Parser<T> parser,
            final T value,
            final String textRepresentation,
            final String failureMessage) {

        //debug
//        System.err.printf("using %s trying to parse '%s' to value %s%n",
//                valueType.getName(), textRepresentation, ""+value);

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

}
