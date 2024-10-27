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

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.value.Password;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.schema.SchemaValueMarshaller;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.schema.cmd.v2.PropertyDto;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.testdomain.model.valuetypes.ValueTypeExample;
import org.apache.causeway.testdomain.value.ValueSemanticsTester.PropertyInteractionProbe;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
        var decomposition = semantics.decompose(example.getValue());

        tester.assertValueEquals(
                example.getValue(),
                semantics.compose(decomposition),
                "decompose/compose roundtrip failed");

        // json roundtrip test
        {
            var json = decomposition.toJson();
            assertNotNull(json);
            assertFalse(json.isBlank());

            var reconstructed = ValueDecomposition
                    .fromJson(semantics.getSchemaValueType(), json);

            assertEquals(json, reconstructed.toJson());

        }

        if(semantics.getSchemaValueType()==ValueType.COMPOSITE) {
            System.err.printf("WARN: ValueSemanticsTest for COMPOSITE %s not implemented.%n",
                    semantics);

            var valueMixin = (Object)null;
            if(valueMixin!=null) {

                var spec = specLoader.specForTypeElseFail(valueMixin.getClass());
                var interaction = ActionInteraction
                        .start(ManagedObject.mixin(spec,  valueMixin), "act", Where.ANYWHERE);

                var pendingParams = interaction
                        .startParameterNegotiation()
                        .get();

                var managedAction = interaction.getManagedActionElseFail();
                var typedTuple = pendingParams.getParamValues();

                var recoveredValue = managedAction
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
            var value = parseExpectation.getValue();

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
                    var parsedValue = parser.parseTextRepresentation(context, in);

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
        for(var value : example.getParserRoundtripExamples()) {

            var stringified = parser.parseableTextRepresentation(context, value);

            if(valueType.equals(Password.class)) {
                var recoveredValue = (Password)parser.parseTextRepresentation(context, stringified);
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

                    var with4digitZone = _Strings.substring(stringified, 0, -3) + "00";
                    var with2digitZone = _Strings.substring(stringified, 0, -3);

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
            var value = renderExpectation.getValue();
            assertEquals(renderExpectation.getTitle(), renderer.titlePresentation(context, value));
            assertEquals(renderExpectation.getHtml(), renderer.htmlPresentation(context, value));
        });
    }

    @Override
    public void testCommand(
            final ValueSemanticsProvider.Context context,
            final Command command) {

        var propertyDto = (PropertyDto)command.getCommandDto().getMember();
        var newValueRecordedDto = propertyDto.getNewValue();

        var newValueRecorded = valueMarshaller.recoverPropertyFrom(propertyDto);
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

        var parsedValue = Try.call(()->
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
