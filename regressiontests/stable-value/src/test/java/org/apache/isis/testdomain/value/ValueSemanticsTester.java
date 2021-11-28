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

import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.OrderRelation;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.resources._Xml;
import org.apache.isis.commons.internal.resources._Xml.WriteOptions;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

public class ValueSemanticsTester<T> {

    @Inject InteractionService interactionService;
    @Inject SpecificationLoader specLoader;

    private final Class<T> valueType;
    private final Object domainObject;
    private Optional<OrderRelation<T, ?>> currentOrderRelation = Optional.empty();

    public ValueSemanticsTester(final Class<T> valueType, final Object domainObject) {
        this.valueType = valueType;
        this.domainObject = domainObject;
    }

    public void actionInteraction(
            final @NonNull String actionId) {
        val objSpec = specLoader.specForTypeElseFail(domainObject.getClass());
        val act = objSpec.getActionElseFail(actionId);
    }

    public static interface PropertyInteractionProbe<T> {
        void testEncoderDecoder(ValueSemanticsProvider.Context context, EncoderDecoder<T> codec);
        void testParser(ValueSemanticsProvider.Context context, Parser<T> parser);
        void testRenderer(ValueSemanticsProvider.Context context, Renderer<T> renderer);
        void testCommand(ValueSemanticsProvider.Context context, Command command, EncoderDecoder<T> codec);
    }

    @SneakyThrows
    public void propertyInteraction(
            final @NonNull String propertyId,
            final @NonNull InteractionContext interactionContext,
            final @NonNull Function<ManagedProperty, Object> newProperyValueProvider,
            final @NonNull PropertyInteractionProbe<T> probe) {

        val objSpec = specLoader.specForTypeElseFail(domainObject.getClass());
        val prop = objSpec.getPropertyElseFail(propertyId);

        val context = valueFacet(prop)
                .createValueSemanticsContext(prop);

        val codec = codec(prop);

        probe.testEncoderDecoder(context, codec);

        val parserIfAny = parser(prop);
        if(parserIfAny.isPresent()) {
            probe.testParser(context, parserIfAny.get());
        }

        val rendererIfAny = renderer(prop);
        if(rendererIfAny.isPresent()) {
            probe.testRenderer(context, rendererIfAny.get());
        }

        interactionService.run(interactionContext, ()->{

            val command = interactionService.currentInteractionElseFail().getCommand();

            val propInteraction = PropertyInteraction
                    .wrap(ManagedProperty.of(ManagedObject.of(objSpec, domainObject), prop, Where.OBJECT_FORMS));

            propInteraction.modifyProperty(managedProp->
                ManagedObject.of(managedProp.getElementType(), newProperyValueProvider.apply(managedProp)));

            probe.testCommand(context, command, codec);
        });
    }

    public void collectionInteraction(
            final @NonNull String collectionId,
            final @NonNull InteractionContext interactionContext) {
        val objSpec = specLoader.specForTypeElseFail(domainObject.getClass());
        val coll = objSpec.getCollectionElseFail(collectionId);
    }

    // -- UTILITY

    public void assertValueEquals(final T a, final Object _b, final String message) {

        val b = _Casts.<T>uncheckedCast(_b);

        if(currentOrderRelation.isPresent()) {
            assertTrue(currentOrderRelation.get().equals(a, b), ()->
                String.format("%s ==> expected: %s but was: %s",
                        message, ""+a, ""+b));
        } else {
            assertEquals(a, b, message);
        }
    }

    // eg.. <ValueWithTypeDto type="string"><com:string>anotherString</com:string></ValueWithTypeDto>
    public static String valueDtoToXml(final ValueWithTypeDto valueWithTypeDto) {
        val xmlResult = _Xml.writeXml(valueWithTypeDto,
                WriteOptions.builder().allowMissingRootElement(true).useContextCache(true).build());
        val rawXml = xmlResult.presentElseFail();
        val xmlRef = _Refs.stringRef(rawXml);
        xmlRef.cutAtIndexOf("<ValueWithTypeDto");
        return xmlRef.cutAtLastIndexOf("</ValueWithTypeDto>")
                .replace(" null=\"false\" xmlns:com=\"http://isis.apache.org/schema/common\" xmlns:cmd=\"http://isis.apache.org/schema/cmd\"", "")
                + "</ValueWithTypeDto>";

    }

    // -- HELPER

    private ValueFacet<T> valueFacet(
            final ObjectFeature feature) {

        val valueFacet = feature.getElementType()
                .lookupFacet(ValueFacet.class)
                .orElseThrow(()->_Exceptions.noSuchElement(
                        "Value type Property or Parameter %s is missing a ValueFacet",
                        feature.getFeatureIdentifier()));

        currentOrderRelation = valueFacet.selectDefaultOrderRelation();

        return _Casts.uncheckedCast(valueFacet);
    }

    private EncoderDecoder<T> codec(
            final ObjectFeature feature) {
        val valueFacet = valueFacet(feature);
        return valueFacet.selectDefaultEncoderDecoder()
                .orElseThrow(()->_Exceptions.noSuchElement());
    }

    private Optional<Parser<T>> parser(
            final ObjectFeature feature) {
        val valueFacet = valueFacet(feature);
        return valueFacet.selectParserForFeature(feature);
    }

    private Optional<Renderer<T>> renderer(
            final ObjectFeature feature) {
        val valueFacet = valueFacet(feature);
        return valueFacet.selectDefaultRenderer();
    }



}
