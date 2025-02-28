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
package org.apache.causeway.core.metamodel.interactions.managed;

import java.util.Optional;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Railway;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facets.object.value.ValueFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember.AuthorizationException;

public record ActionInteraction(
        String memberId,
        /**
         * optionally the action's metamodel, based on whether was found by actionId
         */
        @NonNull Optional<ObjectAction> objectAction,
        @NonNull InteractionRailway<ManagedAction> railway)
implements MemberInteraction<ManagedAction, ActionInteraction> {

    public enum SemanticConstraint {
        NONE,
        IDEMPOTENT,
        SAFE
    }

    public record Result(
            ManagedAction managedAction,
            Can<ManagedObject> parameterList,
            ManagedObject actionReturnedObject) {
    }

    public static interface ParameterInvalidCallback {
        void onParameterInvalid(ManagedParameter managedParameter, InteractionVeto veto);
    }

    // -- FACTORIES

    public static ActionInteraction start(
            final @NonNull ManagedObject owner,
            final @NonNull String memberId,
            final @NonNull Where where) {
        return startWithMultiselect(owner, memberId, where, Can::empty);
    }

    public static ActionInteraction startWithMultiselect(
            final @NonNull ManagedObject owner,
            final @NonNull String actionId,
            final @NonNull Where where,
            final @NonNull MultiselectChoices multiselectChoices) {

        var managedAction = ManagedAction.lookupActionWithMultiselect(owner, actionId, where, multiselectChoices);

        final InteractionRailway<ManagedAction> railway = managedAction.isPresent()
                ? InteractionRailway.success(managedAction.get())
                : InteractionRailway.veto(InteractionVeto.notFound(Identifier.Type.ACTION, actionId));

        return new ActionInteraction(
                actionId,
                managedAction.map(x->x.getAction()),
                railway);
    }

    /** Supports composite-value-types via mixin (in case detected). */
    public static ActionInteraction startAsBoundToProperty(
            final ManagedProperty associatedWithProperty,
            final String memberId,
            final Where where) {
        var propertyOwner = associatedWithProperty.getOwner();
        var prop = associatedWithProperty.getMetaModel();
        var elementType = prop.getElementType();

        var valueFacet = elementType.isValue()
                ? (ValueFacet<?>) elementType.valueFacet().orElse(null)
                : null;

        if(valueFacet!=null
                && valueFacet.isCompositeValueType()
                //XXX guard against memberId collision,
                // such that if there is a conflict, the conventional member wins
                // (maybe improve programming model so this cannot happen)
                && propertyOwner.getSpecification().getAction(memberId, MixedIn.INCLUDED).isEmpty()) {

            var compositeValueNullable = prop.get(propertyOwner);
            var compositeValue =
                    ManagedObjects.nullOrEmptyToDefault(elementType, compositeValueNullable, ()->
                        valueFacet.selectDefaultsProviderForAttribute(prop)
                            .orElseThrow(()->onMissingDefaultsProvider(prop))
                            .getDefaultValue());

            var mixinAction = valueFacet.selectCompositeValueMixinForProperty(associatedWithProperty);
            if(mixinAction.isPresent()) {
                var managedAction = ManagedAction.of(compositeValue, mixinAction.get(), where);
                return ActionInteraction.wrap(managedAction);
            }
        }
        // fallback if not a composite value
        return ActionInteraction.start(propertyOwner, memberId, where);
    }

    /** Supports composite-value-types via mixin (in case detected). */
    public static ActionInteraction startAsBoundToParameter(
            final ParameterNegotiationModel parameterNegotiationModel,
            final int paramIndex,
            final String memberId,
            final Where where) {

        var actionOwner = parameterNegotiationModel.getActionTarget();
        var param = parameterNegotiationModel.getParamModels().getElseFail(paramIndex);
        var elementType = param.getMetaModel().getElementType();

        var valueFacet = elementType.isValue()
                ? (ValueFacet<?>) elementType.valueFacet().orElse(null)
                : null;
        if(valueFacet!=null
                && valueFacet.isCompositeValueType()
                //XXX guard against memberId collision,
                // such that if there is a conflict, the conventional member wins
                // (maybe improve programming model so this cannot happen)
                && actionOwner.getSpecification().getAction(memberId, MixedIn.INCLUDED).isEmpty()) {

            var compositeValueNullable = parameterNegotiationModel.getParamValue(paramIndex);
            var compositeValue =
                    ManagedObjects.nullOrEmptyToDefault(elementType, compositeValueNullable, ()->
                        valueFacet.selectDefaultsProviderForAttribute(param.getMetaModel())
                            .orElseThrow(()->onMissingDefaultsProvider(param.getMetaModel()))
                            .getDefaultValue());

            var mixinAction = valueFacet.selectCompositeValueMixinForParameter(parameterNegotiationModel, paramIndex);
            if(mixinAction.isPresent()) {
                var managedAction = ManagedAction.of(compositeValue, mixinAction.get(), where);
                return ActionInteraction.wrap(managedAction);
            }
        }

        // else if not a composite value
        return ActionInteraction.start(actionOwner, memberId, where);
    }

    public static ActionInteraction wrap(final @NonNull ManagedAction managedAction) {
        var action = managedAction.getAction();
        return new ActionInteraction(
                action.getId(),
                Optional.of(action),
                InteractionRailway.success(managedAction));
    }

    public static ActionInteraction empty(final String actionId) {
        return new ActionInteraction(
                actionId,
                Optional.empty(),
                InteractionRailway.veto(InteractionVeto.notFound(Identifier.Type.ACTION, actionId)));
    }

    // -- METHODS

    public ObjectAction getObjectActionElseFail() {
        return objectAction.orElseThrow(()->_Exceptions
                .noSuchElement("could not resolve action by memberId '%s'", memberId));
    }

    public Optional<Identifier> getFeatureIdentifier() {
        return objectAction.map(ObjectAction::getFeatureIdentifier);
    }

    public ActionInteraction checkSemanticConstraint(final @NonNull SemanticConstraint semanticConstraint) {
        railway.update(action -> switch(semanticConstraint) {
                case NONE -> Optional.empty();
                case IDEMPOTENT -> action.getAction().getSemantics().isIdempotentInNature()
                            ? Optional.empty()
                            : Optional.of(InteractionVeto.actionNotIdempotent(action));
                case SAFE -> action.getAction().getSemantics().isSafeInNature()
                            ? Optional.empty()
                            : Optional.of(InteractionVeto.actionNotSafe(action));
            }
        );
        return this;
    }

    public Optional<ParameterNegotiationModel> startParameterNegotiation() {
        return getManagedAction()
            .map(ManagedAction::startParameterNegotiation);
    }

    public Railway<InteractionVeto, ManagedObject> invokeWith(final ParameterNegotiationModel pendingArgs) {
        pendingArgs.activateValidationFeedback();
        var veto = validate(pendingArgs);
        if(veto.isPresent()) return Railway.failure(veto.get());

        var action = railway.getSuccessElseFail();
        var actionResultOrVeto = action.invoke(pendingArgs.getParamValues());
        return actionResultOrVeto;
    }

    public ManagedObject invokeWithRuleChecking(
            final ParameterNegotiationModel pendingArgs) throws AuthorizationException {
        var action = railway.getSuccessElseFail();
        return action.invokeWithRuleChecking(pendingArgs.getParamValues());
    }

    public Optional<InteractionVeto> validate(
            final @NonNull ParameterNegotiationModel pendingArgs) {
        if(railway.isVeto()) return railway.getVeto();

        var validityConsent = pendingArgs.validateParameterSet(); // full validation
        return validityConsent!=null
                && validityConsent.isVetoed()
            ? Optional.of(InteractionVeto.actionParamInvalid(validityConsent))
            : Optional.empty();
    }

    /**
     * @return optionally the ManagedAction based on whether there
     * was no interaction veto within the originating chain
     */
    public Optional<ManagedAction> getManagedAction() {
        return railway.getSuccess();
    }

    /**
     * @return this Interaction's ManagedAction
     * @throws X if there was any interaction veto within the originating chain
     */
    public <X extends Throwable> ManagedAction getManagedActionElseThrow(
            final Function<InteractionVeto, ? extends X> onFailure) throws X {
        return getManagedMemberElseThrow(onFailure);
    }

    public <X extends Throwable> ManagedAction getManagedActionElseFail() {
        return getManagedActionElseThrow(veto->
            _Exceptions.unrecoverable("action vetoed: " + veto.getReason()));
    }

    // -- HELPER

    private static RuntimeException onMissingDefaultsProvider(final ObjectFeature feature) {
        return _Exceptions.unrecoverable("Could not find a DefaultsProvider for ObjectFeature %s",
                feature.getFeatureIdentifier());
    }

}
