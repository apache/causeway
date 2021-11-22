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

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.functions._Functions.CheckedBiConsumer;
import org.apache.isis.commons.internal.functions._Functions.CheckedConsumer;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

public class ValueSemanticsTester<T extends Serializable> {

    @Inject InteractionService interactionService;
    @Inject SpecificationLoader specLoader;

    private final Class<T> valueType;
    private final Object domainObject;

    public ValueSemanticsTester(final Class<T> valueType, final Object domainObject) {
        this.valueType = valueType;
        this.domainObject = domainObject;
    }

    public void actionInteraction(
            final @NonNull String actionId) {
        val objSpec = specLoader.specForTypeElseFail(domainObject.getClass());
        val act = objSpec.getActionElseFail(actionId);
    }

    @SneakyThrows
    public void propertyInteraction(
            final @NonNull String propertyId,
            final @NonNull InteractionContext interactionContext,
            final @NonNull Function<ManagedProperty, Object> newProperyValueProvider,
            final @NonNull CheckedBiConsumer<ValueSemanticsProvider.Context, EncoderDecoder<T>> codecCallback,
            final @NonNull CheckedBiConsumer<ValueSemanticsProvider.Context, Parser<T>> parserCallback,
            final @NonNull CheckedBiConsumer<ValueSemanticsProvider.Context, Renderer<T>> renderCallback,
            final @NonNull CheckedConsumer<Command> commandCallback) {

        val objSpec = specLoader.specForTypeElseFail(domainObject.getClass());
        val prop = objSpec.getPropertyElseFail(propertyId);

        val context = valueFacet(prop)
                .createValueSemanticsContext(prop);

        codecCallback.accept(context, codec(prop));

        val parserIfAny = parser(prop);
        if(parserIfAny.isPresent()) {
            parserCallback.accept(context, parserIfAny.get());
        }

        val rendererIfAny = renderer(prop);
        if(rendererIfAny.isPresent()) {
            renderCallback.accept(context, rendererIfAny.get());
        }

        interactionService.run(interactionContext, ()->{

            val command = interactionService.currentInteractionElseFail().getCommand();

            val propInteraction = PropertyInteraction
                    .wrap(ManagedProperty.of(ManagedObject.of(objSpec, domainObject), prop, Where.OBJECT_FORMS));

            propInteraction.modifyProperty(managedProp->
                ManagedObject.of(managedProp.getElementType(), newProperyValueProvider.apply(managedProp)));

            commandCallback.accept(command);
        });
    }

    public void collectionInteraction(
            final @NonNull String collectionId,
            final @NonNull InteractionContext interactionContext) {
        val objSpec = specLoader.specForTypeElseFail(domainObject.getClass());
        val coll = objSpec.getCollectionElseFail(collectionId);
    }

    // -- HELPER

    private ValueFacet<T> valueFacet(
            final ObjectFeature feature) {

        val valueFacet = feature.getElementType()
                .lookupFacet(ValueFacet.class)
                .orElseThrow(()->_Exceptions.noSuchElement(
                        "Value type Property or Parameter %s is missing a ValueFacet",
                        feature.getFeatureIdentifier()));

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
