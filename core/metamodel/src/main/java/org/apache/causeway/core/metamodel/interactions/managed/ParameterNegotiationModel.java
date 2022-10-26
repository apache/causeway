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

import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.binding.Bindable;
import org.apache.causeway.commons.binding.Observable;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.binding._BindableAbstract;
import org.apache.causeway.commons.internal.binding._Bindables;
import org.apache.causeway.commons.internal.binding._Bindables.BooleanBindable;
import org.apache.causeway.commons.internal.binding._Observables;
import org.apache.causeway.commons.internal.binding._Observables.LazyObservable;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.consent.InteractionResult;
import org.apache.causeway.core.metamodel.interactions.managed._BindingUtil.TargetFormat;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmAssertionUtil;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;


/**
 * Model used to negotiate the parameter values of an action by means of an UI dialog.
 * <p>
 * This supports aspects of UI component binding to pending values and possible choices,
 * as well as validation failures.
 *
 * @since 2.0.0
 */
public class ParameterNegotiationModel {

    public static ParameterNegotiationModel of(
            final @NonNull ManagedAction managedAction,
            final @NonNull Can<ManagedObject> initialParamValues) {
        return new ParameterNegotiationModel(managedAction, initialParamValues);

    }

    private final @NonNull ManagedAction managedAction;
    private final Can<ParameterModel> paramModels;
    private final _BindableAbstract<Boolean> validationFeedbackActive;
    private final LazyObservable<String> observableActionValidation;

    private ParameterNegotiationModel(
            final @NonNull ManagedAction managedAction,
            final @NonNull Can<ManagedObject> initialParamValues) {
        this.managedAction = managedAction;
        this.validationFeedbackActive = _Bindables.forValue(false);

        val paramNrIterator = IntStream.range(0, initialParamValues.size()).iterator();
        this.paramModels = initialParamValues
                .map(initialValue->new ParameterModel(paramNrIterator.nextInt(), this, initialValue));

        this.observableActionValidation = _Observables.lazy(()->
            validationFeedbackActive.getValue()
                ? actionValidationMessage()
                : (String)null);

        // when activated make sure all validation is reassessed
        this.validationFeedbackActive.addListener((e,o,n)->{
            paramModels.forEach(ParameterModel::invalidateChoicesAndValidation);
            observableActionValidation.invalidate();
        });

    }

    // -- ACTION SPECIFIC

    public int getParamCount() {
        return paramModels.size();
    }

    @NonNull public Can<ManagedObject> getParamValues() {
        return paramModels.stream()
                .map(ParameterModel::getValue)
                .map(Bindable::getValue)
                // guard against framework bugs
                .peek(managedObj->Objects.requireNonNull(managedObj, ()->
                        String.format("Internal Error: Parameter value adapter must not be null in %s",
                                managedAction.getAction().getFeatureIdentifier())))
                .collect(Can.toCan());
    }

    @NonNull public ActionInteractionHead getHead() {
        return managedAction.interactionHead();
    }

    @NonNull public ManagedObject getActionTarget() {
        return getHead().getTarget();
    }

    @NonNull public Observable<String> getObservableActionValidation() {
        return observableActionValidation;
    }

    /**
     * Whether validation feedback is activated. Activates once user attempts to 'submit' an action.
     */
    @NonNull public Observable<Boolean> getObservableValidationFeedbackActive() {
        return validationFeedbackActive;
    }

    public void setParamValues(final @NonNull Can<ManagedObject> paramValues) {
        // allow overflow and underflow
        val valueIterator = paramValues.iterator();
        paramModels.forEach(paramModel->{
            if(!valueIterator.hasNext()) return;
            paramModel.getBindableParamValue().setValue(valueIterator.next());
        });
    }

    // -- PARAMETER SPECIFIC

    @NonNull public Can<? extends ManagedParameter> getParamModels() {
        return paramModels;
    }

    @NonNull public ObjectActionParameter getParamMetamodel(final int paramNr) {
        return paramModels.getElseFail(paramNr).getMetaModel();
    }

    @NonNull public Bindable<ManagedObject> getBindableParamValue(final int paramNr) {
        return paramModels.getElseFail(paramNr).getBindableParamValue();
    }

    @NonNull public BooleanBindable getBindableParamValueDirtyFlag(final int paramNr) {
        return paramModels.getElseFail(paramNr).getBindableParamValueDirtyFlag();
    }

    @NonNull public Observable<Can<ManagedObject>> getObservableParamChoices(final int paramNr) {
        return paramModels.getElseFail(paramNr).getObservableParamChoices();
    }

    @NonNull public Observable<String> getObservableParamValidation(final int paramNr) {
        return paramModels.getElseFail(paramNr).getObservableParamValidation();
    }

    @NonNull public Bindable<String> getBindableParamSearchArgument(final int paramNr) {
        return paramModels.getElseFail(paramNr).getBindableParamSearchArgument();
    }

    @NonNull public Consent getVisibilityConsent(final int paramNr) {
        val pendingArgValues = getParamValues();
        val paramMeta = getParamMetamodel(paramNr);
        val isVisible = paramMeta
                .isVisible(getHead(), pendingArgValues, InteractionInitiatedBy.USER);
        return isVisible;
    }
    @NonNull public Consent getUsabilityConsent(final int paramNr) {
        val pendingArgValues = getParamValues();
        val paramMeta = getParamMetamodel(paramNr);
        val isUsable = paramMeta
                .isUsable(getHead(), pendingArgValues, InteractionInitiatedBy.USER);
        return isUsable;
    }

    // -- MULTI SELECT

    public MultiselectChoices getMultiselectChoices() {
        return managedAction.getMultiselectChoices();
    }

    // -- RATHER INTERNAL ...

    /** validates all, the action and each individual parameter */
    public Consent validateParameterSet() {
        val head = this.getHead();
        return head.getMetaModel().isArgumentSetValid(head, this.getParamValues(), InteractionInitiatedBy.USER);
    }

    public Consent validateParameterSetForAction() {
        val head = this.getHead();
        return head.getMetaModel().isArgumentSetValidForAction(head, this.getParamValues(), InteractionInitiatedBy.USER);
    }

    public Can<Consent> validateParameterSetForParameters() {
        val head = this.getHead();
        return head.getMetaModel()
                .isArgumentSetValidForParameters(head, this.getParamValues(), InteractionInitiatedBy.USER)
                .stream()
                .map(InteractionResult::createConsent)
                .collect(Can.toCan());
    }


    @NonNull public ManagedObject getParamValue(final int paramNr) {
        return paramModels.getElseFail(paramNr).getValue().getValue();
    }

    /**
     * If newParamValue is null, unspecified or empty,
     * results in a {@link #clearParamValue(int)} operation;
     * otherwise sets the new value.
     */
    public void setParamValue(final int paramIndex, final @Nullable ManagedObject newParamValue) {
        if (ManagedObjects.isNullOrUnspecifiedOrEmpty(newParamValue)) {
            clearParamValue(paramIndex);
        } else {
            paramModels.getElseFail(paramIndex).getBindableParamValue().setValue(newParamValue);
        }
    }

    public void clearParamValue(final int paramIndex) {
        val emptyValue = adaptParamValuePojo(paramIndex, null);
        paramModels.getElseFail(paramIndex).getBindableParamValue().setValue(emptyValue);
    }

    @NonNull public ManagedObject adaptParamValuePojo(final int paramIndex,
            final @Nullable Object newParamValuePojo) {
        val paramMeta = getParamMetamodel(paramIndex);
        return ManagedObject.adaptParameter(paramMeta, newParamValuePojo);
    }

    /**
     * exposed for testing
     */
    public void activateValidationFeedback() {
        validationFeedbackActive.setValue(true);
    }

    private void onNewParamValue() {
        paramModels.forEach(ParameterModel::invalidateChoicesAndValidation);
        observableActionValidation.invalidate();
    }

    private String actionValidationMessage() {
        val validityConsentForAction = this.validateParameterSetForAction();
        if(validityConsentForAction!=null
                && validityConsentForAction.isVetoed()) {
            return validityConsentForAction.getReason();
        }
        return null;
    }

    // -- INTERNAL HOLDER OF PARAMETER BINDABLES

    private static class ParameterModel extends ManagedParameter {

        @Getter(onMethod_ = {@Override}) private final int paramNr;
        @Getter(onMethod_ = {@Override}) @NonNull private final ObjectActionParameter metaModel;
        @Getter(onMethod_ = {@Override}) @NonNull private final ParameterNegotiationModel negotiationModel;
        @Getter @NonNull private final _BindableAbstract<ManagedObject> bindableParamValue;
        @Getter @NonNull private final BooleanBindable bindableParamValueDirtyFlag;
        @Getter @NonNull private final LazyObservable<String> observableParamValidation;
        @Getter @NonNull private final _BindableAbstract<String> bindableParamSearchArgument;
        @Getter @NonNull private final LazyObservable<Can<ManagedObject>> observableParamChoices;
        private Observable<String> bindableParamAsTitle;
        private Observable<String> bindableParamAsHtml;
        private Bindable<String> bindableParamAsParsableText;

        private ParameterModel(
                final int paramNr,
                final @NonNull ParameterNegotiationModel negotiationModel,
                final @NonNull ManagedObject initialValue) {

            val action = negotiationModel.getHead().getMetaModel();

            this.paramNr = paramNr;
            this.metaModel = action.getParameters().getElseFail(paramNr);
            this.negotiationModel = negotiationModel;

            bindableParamValue = _Bindables.forValue(initialValue);
            bindableParamValueDirtyFlag = _Bindables.forBoolean(false);

            //bindableParamValue.setValueRefiner(MmEntityUtil::refetch); no longer used
            bindableParamValue.setValueGuard(MmAssertionUtil.assertInstanceOf(metaModel.getElementType()));
            bindableParamValue.addListener((event, oldValue, newValue)->{
                if(newValue==null) {
                    // lift null to empty ...
                    bindableParamValue.setValue(metaModel.getEmpty()); // triggers this event again
                    return;
                }
                getNegotiationModel().onNewParamValue();
                bindableParamValueDirtyFlag.setValue(true); // set dirty whenever an update event happens
            });

            // has either autoComplete, choices, or none
            observableParamChoices = metaModel.hasAutoComplete()
            ? _Observables.lazy(()->
                getMetaModel().getAutoComplete(
                        getNegotiationModel(),
                        getBindableParamSearchArgument().getValue(),
                        InteractionInitiatedBy.USER))
            : metaModel.hasChoices()
                ? _Observables.lazy(()->
                    getMetaModel().getChoices(getNegotiationModel(), InteractionInitiatedBy.USER))
                : _Observables.lazy(Can::empty);

            // if has autoComplete, then activate the search argument
            bindableParamSearchArgument = _Bindables.forValue(null);
            if(metaModel.hasAutoComplete()) {
                bindableParamSearchArgument.addListener((e,o,n)->{
                    observableParamChoices.invalidate();
                });
            }

            // validate this parameter, but only when validationFeedback has been activated
            observableParamValidation = _Observables.lazy(()->
                isValidationFeedbackActive()
                ? getMetaModel()
                        .isValid(getNegotiationModel().getHead(), getNegotiationModel().getParamValues(), InteractionInitiatedBy.USER)
                        .getReason()
                : (String)null);

        }

        public void invalidateChoicesAndValidation() {
            observableParamChoices.invalidate();
            observableParamValidation.invalidate();
        }

        private boolean isValidationFeedbackActive() {
            return getNegotiationModel().getObservableValidationFeedbackActive().getValue();
        }

        // -- MANAGED PARAMETER

        @Override
        public Identifier getIdentifier() {
            return getMetaModel().getFeatureIdentifier();
        }

        @Override
        public String getFriendlyName() {
            return getMetaModel().getStaticFriendlyName()
                    .orElseThrow(_Exceptions::unexpectedCodeReach);
        }

        @Override
        public Optional<String> getDescription() {
            return getMetaModel().getStaticDescription();
        }

        @Override
        public ObjectSpecification getElementType() {
            return getMetaModel().getElementType();
        }

        @Override
        public Bindable<ManagedObject> getValue() {
            return bindableParamValue;
        }

        @Override
        public Observable<String> getValueAsTitle() {
            if(bindableParamAsTitle==null) {
                // value types should have associated rederers via value semantics
                bindableParamAsTitle = _BindingUtil
                        .bindAsFormated(TargetFormat.TITLE, metaModel, bindableParamValue);
            }
            return bindableParamAsTitle;
        }

        @Override
        public Observable<String> getValueAsHtml() {
            if(bindableParamAsHtml==null) {
                // value types should have associated rederers via value semantics
                bindableParamAsHtml = _BindingUtil
                        .bindAsFormated(TargetFormat.HTML, metaModel, bindableParamValue);
            }
            return bindableParamAsHtml;
        }

        @Override
        public boolean isValueAsParsableTextSupported() {
            return _BindingUtil.hasParser(metaModel);
        }

        @Override
        public Bindable<String> getValueAsParsableText() {
            if(bindableParamAsParsableText==null) {
                // value types should have associated parsers/formatters via value semantics
                // except for composite value types, which might have not
                bindableParamAsParsableText = (Bindable<String>) _BindingUtil
                        .bindAsFormated(TargetFormat.PARSABLE_TEXT, metaModel, bindableParamValue);
            }
            return bindableParamAsParsableText;
        }

        @Override
        public Observable<String> getValidationMessage() {
            return observableParamValidation;
        }

        @Override
        public Bindable<String> getSearchArgument() {
            return bindableParamSearchArgument;
        }

        @Override
        public Observable<Can<ManagedObject>> getChoices() {
            return observableParamChoices;
        }

    }

}
