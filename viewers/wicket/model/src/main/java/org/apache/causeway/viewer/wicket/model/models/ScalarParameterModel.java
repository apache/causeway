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
package org.apache.causeway.viewer.wicket.model.models;

import java.util.ArrayList;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.commons.ViewOrEditMode;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.consent.InteractionResultSet;
import org.apache.causeway.core.metamodel.interactions.ActionArgValidityContext;
import org.apache.causeway.core.metamodel.interactions.InteractionUtils;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedValue;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.viewer.commons.model.scalar.HasUiParameter;
import org.apache.causeway.viewer.wicket.model.models.interaction.act.UiParameterWkt;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class ScalarParameterModel
extends ScalarModel
implements HasUiParameter {

    private static final long serialVersionUID = 1L;

    public static ScalarParameterModel wrap(final UiParameterWkt delegate) {
        return new ScalarParameterModel(delegate);
    }

    @Getter(onMethod_={@Override})
    private final UiParameterWkt uiParameter;

    /**
     * Creates a model representing an action parameter of an action of a parent
     * object, with the {@link #getObject() value of this model} to be default
     * value (if any) of that action parameter.
     */
    private ScalarParameterModel(
            final UiParameterWkt uiParameter) {
        super(
                UiObjectWkt.ofAdapter(uiParameter.getOwner()),
                ViewOrEditMode.EDITING); // always init params in editing mode, decide usability later dynamically
        this.uiParameter = uiParameter;
    }

    @Override
    public String validate(final ManagedObject proposedArg) {

        val value = getParameterNegotiationModel().getObservableParamValidation(getParameterIndex()).getValue();
        if (value != null) {
            return null;
        }


        //
        // validate individual params
        //
        // TODO: this validation could, I think, move to the param negotiation model?
        //

        final var interactionHead = getParameterNegotiationModel().getHead();
        final var previousOrProposedArgs = previousOrProposedArgs(proposedArg);
        final var validityContext = validityContext(interactionHead, previousOrProposedArgs);

        return validate(validityContext);
    }

    /**
     * Returns a Can of the previous args + the proposed arg.
     */
    private Can<ManagedObject> previousOrProposedArgs(
            final ManagedObject proposedArg) {

        final var objectAction = getParameterNegotiationModel().getHead().getMetaModel();
        final var paramList = new ArrayList<ManagedObject>();

        final var previousArgs = getParameterNegotiationModel().getParamValues();
        for (ObjectActionParameter oap : objectAction.getParameters()) {
            paramList.add(previousOrProposedArg(oap, previousArgs, proposedArg));
        }

        return Can.ofCollection(paramList);
    }

    /**
     * Returns either the relevant previous arg (from the {@link #getParameterNegotiationModel() negotiation model}
     * or the proposed arg if the supplied {@link ObjectActionParameter} corresponds
     *
     * @param eachOap - each {@link ObjectActionParameter} of the action
     * @param previousParamArgs - already in the negotiation model, have been validated
     * @param proposedParamArg - current being validated
     */
    private ManagedObject previousOrProposedArg(
            final ObjectActionParameter eachOap,
            final Can<ManagedObject> previousParamArgs,
            final ManagedObject proposedParamArg) {

        int eachParamIndex = eachOap.getParameterIndex();
        if(eachParamIndex == getParameterIndex()) {
            return proposedParamArg;
        }

        return previousParamArgs.get(eachParamIndex).orElseGet(() -> {
            ObjectSpecification eachParamType = eachOap.getElementType();
            return ManagedObject.empty(eachParamType);
        });
    }

    private ActionArgValidityContext validityContext(ActionInteractionHead interactionHead, Can<ManagedObject> proposedArguments) {
        final var objectActionParameter = getUiParameter().getMetaModel();
        return objectActionParameter.createProposedArgumentInteractionContext(
                    interactionHead, proposedArguments, getParameterIndex(), InteractionInitiatedBy.USER);
    }

    private String validate(ActionArgValidityContext validityContext) {
        final var objectActionParameter = getUiParameter().getMetaModel();
        final var resultSet = new InteractionResultSet();
        InteractionUtils.isValidResultSet(objectActionParameter, validityContext, resultSet);
        final var consent = resultSet.createConsent();
        return consent.getReasonAsString().orElse(null);
    }

    @Override
    public String toStringOf() {
        return getFriendlyName() + ": " + getParameterIndex();
    }

    @Override
    protected Can<ObjectAction> calcAssociatedActions() {
        return getElementType().streamActions(ActionScope.ANY, MixedIn.INCLUDED)
                .collect(Can.toCan());
    }

    @Override
    public @NonNull ManagedObject getValue() {
        return getObject();
    }

    @Override
    public void setValue(final ManagedObject paramValue) {
        setObject(paramValue);
    }

    @Override
    public ManagedValue proposedValue() {
        return getParameterNegotiationModel().getParamModels().getElseFail(getParameterIndex());
    }

}
