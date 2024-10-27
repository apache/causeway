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

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.value.semantics.OrderRelation;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facets.object.value.ValueFacet;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.testdomain.model.valuetypes.ValueTypeExample;

import lombok.NonNull;
import lombok.SneakyThrows;

class ValueSemanticsTester<T> {

    @Inject InteractionService interactionService;
    @Inject SpecificationLoader specLoader;
    @Inject WrapperFactory wrapperFactory;

    @SuppressWarnings("unused")
    private final Class<T> valueType;
    private final ValueTypeExample<T> domainObject;
    private Optional<OrderRelation<T, ?>> currentOrderRelation = Optional.empty();

    public ValueSemanticsTester(final Class<T> valueType, final ValueTypeExample<T> domainObject) {
        this.valueType = valueType;
        this.domainObject = domainObject;
    }

    // -- ACTIONS

    public static interface ActionInteractionProbe<T> {
        void testCommandWithNonEmptyArg(ValueSemanticsProvider.Context context, Command command);
        void testCommandWithEmptyArg(ValueSemanticsProvider.Context context, Command command);
    }

    public void actionInteraction(
            final @NonNull String actionId,
            final @NonNull InteractionContext interactionContext,
            final @NonNull Supplier<T> actionArgumentProvider,
            final @NonNull ActionInteractionProbe<T> probe) {

        var objSpec = specLoader.specForTypeElseFail(domainObject.getClass());
        var act = objSpec.getActionElseFail(actionId);
        var context = valueFacet(act.getParameters().getFirstElseFail())
                .createValueSemanticsContext(act);

        {
            var actionCommandWithNonEmptyArg = interactionService.call(interactionContext, ()->{

                var command = interactionService.currentInteractionElseFail().getCommand();
                var actInteraction = ActionInteraction
                        .wrap(ManagedAction.of(ManagedObject.adaptSingular(objSpec, domainObject), act, Where.OBJECT_FORMS));

                var params = actInteraction.startParameterNegotiation().orElseThrow();
                var singleArgPojoToUse = actionArgumentProvider.get();

                params.updateParamValuePojo(0, __->singleArgPojoToUse);

                actInteraction.invokeWith(params);

                return command;
            });

            probe.testCommandWithNonEmptyArg(context, actionCommandWithNonEmptyArg);
        }

        {
            var actionCommandWithEmptyArg = interactionService.call(interactionContext, ()->{

                var command = interactionService.currentInteractionElseFail().getCommand();
                var actInteraction = ActionInteraction
                        .wrap(ManagedAction.of(ManagedObject.adaptSingular(objSpec, domainObject), act, Where.OBJECT_FORMS));

                var params = actInteraction.startParameterNegotiation().orElseThrow();

                params.updateParamValuePojo(0, __->null); // overrides default values from value semantics

                actInteraction.invokeWith(params);

                return command;
            });

            probe.testCommandWithEmptyArg(context, actionCommandWithEmptyArg);
        }

        {
            var actionCommandWithNonEmptyArg = interactionService.call(interactionContext, ()->{

                var command = interactionService.currentInteractionElseFail().getCommand();

                domainObject.invokeSampleActionUsingWrapper(wrapperFactory,
                        actionArgumentProvider.get());

                return command;
            });

            probe.testCommandWithNonEmptyArg(context, actionCommandWithNonEmptyArg);
        }

        {
            var actionCommandWithEmptyArg = interactionService.call(interactionContext, ()->{

                var command = interactionService.currentInteractionElseFail().getCommand();

                domainObject.invokeSampleActionUsingWrapper(wrapperFactory, null); // overrides default values from value semantics

                return command;
            });

            probe.testCommandWithEmptyArg(context, actionCommandWithEmptyArg);
        }

    }

    // -- PROPERTIES

    public static interface PropertyInteractionProbe<T> {
        void testComposer(ValueSemanticsProvider.Context context, ValueSemanticsProvider<T> semantics);
        void testParser(ValueSemanticsProvider.Context context, Parser<T> parser);
        void testRenderer(ValueSemanticsProvider.Context context, Renderer<T> renderer);
        void testCommand(ValueSemanticsProvider.Context context, Command command);
    }

    @SneakyThrows
    public void propertyInteraction(
            final @NonNull String propertyId,
            final @NonNull InteractionContext interactionContext,
            final @NonNull Function<ManagedProperty, Object> newProperyValueProvider,
            final @NonNull PropertyInteractionProbe<T> probe) {

        var objSpec = specLoader.specForTypeElseFail(domainObject.getClass());
        var prop = objSpec.getPropertyElseFail(propertyId);

        var context = valueFacet(prop)
                .createValueSemanticsContext(prop);

        var semanticsIfAny = semantics(prop);

        assertTrue(semanticsIfAny.isPresent(), ()->
            "value semantics must be available for "
                + context.getFeatureIdentifier());

        probe.testComposer(context, semanticsIfAny.get());

        var parserIfAny = parser(prop);
        if(parserIfAny.isPresent()) {
            probe.testParser(context, parserIfAny.get());
        }

        var rendererIfAny = renderer(prop);
        if(rendererIfAny.isPresent()) {
            probe.testRenderer(context, rendererIfAny.get());
        }

        interactionService.run(interactionContext, ()->{

            var command = interactionService.currentInteractionElseFail().getCommand();

            var propInteraction = PropertyInteraction
                    .wrap(ManagedProperty.of(ManagedObject.adaptSingular(objSpec, domainObject), prop, Where.OBJECT_FORMS));

            propInteraction.modifyProperty(managedProp->
                ManagedObject.adaptSingular(managedProp.getElementType(), newProperyValueProvider.apply(managedProp)));

            probe.testCommand(context, command);
        });
    }

    // -- COLLECTIONS

    public void collectionInteraction(
            final @NonNull String collectionId,
            final @NonNull InteractionContext interactionContext) {
        var objSpec = specLoader.specForTypeElseFail(domainObject.getClass());
        var coll = objSpec.getCollectionElseFail(collectionId);
        assertNotNull(coll);
        // collections have no interactions (removed in v2)
    }

    // -- UTILITY

    public void assertValueEquals(final T a, final Object _b, final String message) {

        var b = _Casts.<T>uncheckedCast(_b);

        if(currentOrderRelation.isPresent()) {
            assertTrue(currentOrderRelation.get().equals(a, b), ()->
                String.format("%s ==> expected: %s but was: %s",
                        message, ""+a, ""+b));
        } else {
            assertEquals(a, b, message);
        }
    }

    // -- HELPER

    private ValueFacet<T> valueFacet(
            final ObjectFeature feature) {

        var valueFacet = feature.getElementType()
                .lookupFacet(ValueFacet.class)
                .orElseThrow(()->_Exceptions.noSuchElement(
                        "Value type Property or Parameter %s is missing a ValueFacet",
                        feature.getFeatureIdentifier()));

        currentOrderRelation = _Casts.uncheckedCast(valueFacet.selectDefaultOrderRelation());

        return _Casts.uncheckedCast(valueFacet);
    }

    private Optional<ValueSemanticsProvider<T>> semantics(
            final ObjectFeature feature) {
        var valueFacet = valueFacet(feature);
        return valueFacet.selectDefaultSemantics();
    }

    private Optional<Parser<T>> parser(
            final ObjectFeature feature) {
        var valueFacet = valueFacet(feature);
        return valueFacet.selectParserForFeature(feature);
    }

    private Optional<Renderer<T>> renderer(
            final ObjectFeature feature) {
        var valueFacet = valueFacet(feature);
        return valueFacet.selectDefaultRenderer();
    }

}
