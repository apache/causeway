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

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

public final class ActionInteraction
extends MemberInteraction<ManagedAction, ActionInteraction> {

    public static enum SemanticConstraint {
        NONE,
        IDEMPOTENT,
        SAFE
    }

    @Value(staticConstructor = "of")
    public static class Result {
        private final ManagedAction managedAction;
        private final Can<ManagedObject> parameterList;
        private final ManagedObject actionReturnedObject;
    }

    public static final ActionInteraction start(
            final @NonNull ManagedObject owner,
            final @NonNull String memberId,
            final @NonNull Where where) {
        return startWithMultiselect(owner, memberId, where, Can::empty);
    }

    public static final ActionInteraction startWithMultiselect(
            final @NonNull ManagedObject owner,
            final @NonNull String memberId,
            final @NonNull Where where,
            final @NonNull MultiselectChoices multiselectChoices) {

        val managedAction = ManagedAction.lookupActionWithMultiselect(owner, memberId, where, multiselectChoices);

        final InteractionRailway<ManagedAction> railway = managedAction.isPresent()
                ? InteractionRailway.success(managedAction.get())
                : InteractionRailway.veto(InteractionVeto.notFound(Identifier.Type.ACTION, memberId));

        return new ActionInteraction(
                managedAction.map(ManagedAction::getAction),
                railway);
    }

    public static ActionInteraction wrap(final @NonNull ManagedAction managedAction) {
        return new ActionInteraction(
                Optional.of(managedAction.getAction()),
                InteractionRailway.success(managedAction));
    }

    public static ActionInteraction empty(final String actionId) {
        return new ActionInteraction(
                Optional.empty(),
                InteractionRailway.veto(InteractionVeto.notFound(Identifier.Type.ACTION, actionId)));
    }

    ActionInteraction(
            final @NonNull Optional<ObjectAction> metamodel,
            final @NonNull InteractionRailway<ManagedAction> railway) {
        super(railway);
        this.metamodel = metamodel;
    }

    /**
     * optionally the action's metamodel, based on whether even exists (eg. was found by memberId)
     */
    @Getter
    private final Optional<ObjectAction> metamodel;

    public ActionInteraction checkSemanticConstraint(@NonNull final SemanticConstraint semanticConstraint) {

        railway = railway.chain(action->{

            val actionSemantics = action.getAction().getSemantics();

            switch(semanticConstraint) {
            case NONE:
                return railway;

            case IDEMPOTENT:
                return actionSemantics.isIdempotentInNature()
                        ? railway
                        : vetoRailway(InteractionVeto.actionNotIdempotent(action)) ;
            case SAFE:
                return actionSemantics.isSafeInNature()
                        ? railway
                        : vetoRailway(InteractionVeto.actionNotSafe(action));
            default:
                throw _Exceptions.unmatchedCase(semanticConstraint); // unexpected code reach
            }

        });

        return this;
    }

    public Optional<ParameterNegotiationModel> startParameterNegotiation() {
        return getManagedAction()
            .map(ManagedAction::startParameterNegotiation);
    }

    public static interface ParameterInvalidCallback {
        void onParameterInvalid(ManagedParameter managedParameter, InteractionVeto veto);
    }

    public Railway<InteractionVeto, ManagedObject> invokeWith(final ParameterNegotiationModel pendingArgs) {
        pendingArgs.activateValidationFeedback();
        val veto = validate(pendingArgs);
        if(veto.isPresent()) {
            return Railway.failure(veto.get());
        }
        val action = railway.getSuccessElseFail();
        val actionResultOrVeto = action.invoke(pendingArgs.getParamValues());
        return actionResultOrVeto;
    }

    public ManagedObject invokeWithRuleChecking(
            final ParameterNegotiationModel pendingArgs) throws AuthorizationException {
        val action = railway.getSuccessElseFail();
        return action.invokeWithRuleChecking(pendingArgs.getParamValues());
    }

    public Optional<InteractionVeto> validate(
            final @NonNull ParameterNegotiationModel pendingArgs) {

        if(railway.isFailure()) {
            return railway.getFailure();
        }
        val validityConsent = pendingArgs.validateParameterSet(); // full validation
        if(validityConsent!=null && validityConsent.isVetoed()) {
            return Optional.of(InteractionVeto.actionParamInvalid(validityConsent));
        }
        return Optional.empty();
    }

    /**
     * @return optionally the ManagedAction based on whether there
     * was no interaction veto within the originating chain
     */
    public Optional<ManagedAction> getManagedAction() {
        return super.getManagedMember().getSuccess();
    }

    /**
     * @return this Interaction's ManagedAction
     * @throws X if there was any interaction veto within the originating chain
     */
    public <X extends Throwable>
    ManagedAction getManagedActionElseThrow(final Function<InteractionVeto, ? extends X> onFailure) throws X {
        return super.getManagedMemberElseThrow(onFailure);
    }

    public <X extends Throwable>
    ManagedAction getManagedActionElseFail() {
        return getManagedActionElseThrow(veto->
            _Exceptions.unrecoverable("action vetoed: " + veto.getReason()));
    }

    /** Supports composite-value-types via mixin (in case detected). */
    public static ActionInteraction startAsBoundToProperty(
            final ManagedProperty associatedWithProperty,
            final String memberId,
            final Where where) {
        val propertyOwner = associatedWithProperty.getOwner();
        val prop = associatedWithProperty.getMetaModel();
        val elementType = prop.getElementType();

        val valueFacet = elementType.isValue()
                ? (ValueFacet<?>) elementType.valueFacet().orElse(null)
                : null;

        if(valueFacet!=null
                && valueFacet.isCompositeValueType()
                //XXX guard against memberId collision,
                // such that if there is a conflict, the conventional member wins
                // (maybe improve programming model so this cannot happen)
                && propertyOwner.getSpecification().getAction(memberId, MixedIn.INCLUDED).isEmpty()) {

            val compositeValueNullable = prop.get(propertyOwner);
            val compositeValue =
                    ManagedObjects.nullOrEmptyToDefault(elementType, compositeValueNullable, ()->
                        valueFacet.selectDefaultsProviderForProperty(prop)
                            .orElseThrow(()->onMissingDefaultsProvider(prop))
                            .getDefaultValue());

            val mixinAction = valueFacet.selectCompositeValueMixinForProperty(associatedWithProperty);
            if(mixinAction.isPresent()) {
                val managedAction = ManagedAction.of(compositeValue, mixinAction.get(), where);
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

        val actionOwner = parameterNegotiationModel.getActionTarget();
        val param = parameterNegotiationModel.getParamModels().getElseFail(paramIndex);
        val elementType = param.getMetaModel().getElementType();

        val valueFacet = elementType.isValue()
                ? (ValueFacet<?>) elementType.valueFacet().orElse(null)
                : null;
        if(valueFacet!=null
                && valueFacet.isCompositeValueType()
                //XXX guard against memberId collision,
                // such that if there is a conflict, the conventional member wins
                // (maybe improve programming model so this cannot happen)
                && actionOwner.getSpecification().getAction(memberId, MixedIn.INCLUDED).isEmpty()) {

            val compositeValueNullable = parameterNegotiationModel.getParamValue(paramIndex);
            val compositeValue =
                    ManagedObjects.nullOrEmptyToDefault(elementType, compositeValueNullable, ()->
                        valueFacet.selectDefaultsProviderForParameter(param.getMetaModel())
                            .orElseThrow(()->onMissingDefaultsProvider(param.getMetaModel()))
                            .getDefaultValue());

            val mixinAction = valueFacet.selectCompositeValueMixinForParameter(parameterNegotiationModel, paramIndex);
            if(mixinAction.isPresent()) {
                val managedAction = ManagedAction.of(compositeValue, mixinAction.get(), where);
                return ActionInteraction.wrap(managedAction);
            }
        }

        //XXX[CAUSEWAY-3080] prior to this fix we returned... (which I'm not sure why - makes no sense to me)
        //val paramValue = parameterNegotiationModel.getParamValue(paramIndex);
        //return ActionInteraction.start(paramValue, memberId, where);

        // else if not a composite value
        return ActionInteraction.start(actionOwner, memberId, where);
    }

    // -- HELPER

    private static RuntimeException onMissingDefaultsProvider(final ObjectFeature feature) {
        return _Exceptions.unrecoverable("Could not find a DefaultsProvider for ObjectFeature %s",
                feature.getFeatureIdentifier());
    }


}
