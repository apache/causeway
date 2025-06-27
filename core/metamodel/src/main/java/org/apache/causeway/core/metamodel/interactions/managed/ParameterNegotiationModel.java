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
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.binding.Bindable;
import org.apache.causeway.commons.binding.Observable;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.binding._BindableAbstract;
import org.apache.causeway.commons.internal.binding._Bindables;
import org.apache.causeway.commons.internal.binding._Bindables.BooleanBindable;
import org.apache.causeway.commons.internal.binding._Observables;
import org.apache.causeway.commons.internal.binding._Observables.LazyObservable;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.consent.InteractionResult;
import org.apache.causeway.core.metamodel.consent.Veto;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.interactions.managed._BindingUtil.TargetFormat;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmAssertionUtils;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;

import lombok.extern.log4j.Log4j2;

/**
 * Model used to negotiate the parameter values of an action by means of an UI dialog.
 * <p>
 * This supports aspects of UI component binding to pending values and possible choices,
 * as well as validation failures.
 *
 * @since 2.0.0
 */
public final class ParameterNegotiationModel {

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

        var paramNrIterator = IntStream.range(0, initialParamValues.size()).iterator();
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

    public ObjectAction act() {
        return managedAction.getAction();
    }

    public int getParamCount() {
        return paramModels.size();
    }

    public Can<ManagedObject> getParamValues() {
        return paramModels.stream()
                .map(ParameterModel::getValue)
                .map(Bindable::getValue)
                // guard against framework bugs
                .peek(managedObj->Objects.requireNonNull(managedObj, ()->
                        String.format("Internal Error: Parameter value adapter must not be null in %s",
                                act().getFeatureIdentifier())))
                .collect(Can.toCan());
    }

    InteractionHead interactionHead() {
        return managedAction.interactionHead();
    }

    public ActionInteractionHead actionInteractionHead() {
        return managedAction.actionInteractionHead();
    }

    public ManagedObject getActionTarget() {
        return interactionHead().target();
    }

    public Observable<String> getObservableActionValidation() {
        return observableActionValidation;
    }

    /**
     * Whether validation feedback is activated. Activates once user attempts to 'submit' an action.
     */
    public Observable<Boolean> getObservableValidationFeedbackActive() {
        return validationFeedbackActive;
    }

    public void setParamValues(final @NonNull Can<ManagedObject> paramValues) {
        // allow overflow and underflow
        var valueIterator = paramValues.iterator();
        paramModels.forEach(paramModel->{
            if(!valueIterator.hasNext()) return;
            paramModel.bindableParamValue().setValue(valueIterator.next());
        });
    }

    // -- PARAMETER SPECIFIC

    public Can<? extends ManagedParameter> getParamModels() {
        return paramModels;
    }

    public ObjectActionParameter getParamMetamodel(final int paramNr) {
        return paramModels.getElseFail(paramNr).getMetaModel();
    }

    public Bindable<ManagedObject> getBindableParamValue(final int paramNr) {
        return paramModels.getElseFail(paramNr).bindableParamValue();
    }

    public BooleanBindable getBindableParamValueDirtyFlag(final int paramNr) {
        return paramModels.getElseFail(paramNr).bindableParamValueDirtyFlag();
    }

    public Observable<Can<ManagedObject>> getObservableParamChoices(final int paramNr) {
        return paramModels.getElseFail(paramNr).observableParamChoices();
    }

    public Observable<String> getObservableParamValidation(final int paramNr) {
        return paramModels.getElseFail(paramNr).observableParamValidation();
    }

    /**
     * Calls the underlying action parameter validation logic, for pending arguments.
     * (Ignoring the {@link ParameterModel#isValidationFeedbackActive()} flag.)
     * @apiNote introduced for [CAUSEWAY-3753] - not sure why required.
     */
    public String validateImmediately(final int paramIndex) {
        var actionInteractionHead = actionInteractionHead();
        return actionInteractionHead.getMetaModel().getParameterByIndex(paramIndex)
                .isValid(
                        actionInteractionHead.interactionHead(),
                        getParamValues(),
                        InteractionInitiatedBy.USER)
                .getReasonAsString()
                .orElse(null);
    }

    public Bindable<String> getBindableParamSearchArgument(final int paramNr) {
        return paramModels.getElseFail(paramNr).bindableParamSearchArgument();
    }

    public Observable<Consent> getObservableVisibilityConsent(final int paramNr) {
        return paramModels.getElseFail(paramNr).observableVisibilityConsent();
    }

    public Observable<Consent> getObservableUsabilityConsent(final int paramNr) {
        return paramModels.getElseFail(paramNr).observableUsabilityConsent();
    }

    public Consent getVisibilityConsent(final int paramNr) {
        return getObservableVisibilityConsent(paramNr).getValue();
    }
    public Consent getUsabilityConsent(final int paramNr) {
        return getObservableUsabilityConsent(paramNr).getValue();
    }

    // -- MULTI SELECT

    public MultiselectChoices getMultiselectChoices() {
        return managedAction.getMultiselectChoices();
    }

    // -- RATHER INTERNAL ...

    /** validates all, the action and each individual parameter */
    public Consent validateParameterSet() {
        return act().isArgumentSetValid(interactionHead(), this.getParamValues(), InteractionInitiatedBy.USER);
    }

    public Consent validateParameterSetForAction() {
        return act().isArgumentSetValidForAction(interactionHead(), this.getParamValues(), InteractionInitiatedBy.USER);
    }

    public Can<Consent> validateParameterSetForParameters() {
        return act()
                .isArgumentSetValidForParameters(interactionHead(), this.getParamValues(), InteractionInitiatedBy.USER)
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
            paramModels.getElseFail(paramIndex).bindableParamValue().setValue(newParamValue);
        }
    }

    public void clearParamValue(final int paramIndex) {
        var emptyValue = adaptParamValuePojo(paramIndex, null);
        paramModels.getElseFail(paramIndex).bindableParamValue().setValue(emptyValue);
    }

    /**
     * @param paramIndex - zero based parameter index
     * @param updater - takes the current parameter argument value wrapped as {@link ManagedObject} as input
     *      and returns the new parameter argument value also wrapped as {@link ManagedObject}
     */
    public void updateParamValue(final int paramIndex, final @NonNull UnaryOperator<ManagedObject> updater) {
        var bindableParamValue = paramModels.getElseFail(paramIndex).bindableParamValue();
        var newParamValue = updater.apply(bindableParamValue.getValue());
        if (ManagedObjects.isNullOrUnspecifiedOrEmpty(newParamValue)) {
            clearParamValue(paramIndex);
        } else {
            bindableParamValue.setValue(newParamValue);
        }
    }

    /**
     * @param paramIndex - zero based parameter index
     * @param pojoUpdater - takes the current parameter argument value wrapped as {@link ManagedObject} as input
     *      and returns the new parameter argument value as pojo
     */
    public void updateParamValuePojo(
            final int paramIndex, final @NonNull Function<ManagedObject, Object> pojoUpdater) {
        updateParamValue(paramIndex, current->adaptParamValuePojo(paramIndex, pojoUpdater.apply(current)));
    }

    @NonNull public ManagedObject adaptParamValuePojo(final int paramIndex,
            final @Nullable Object newParamValuePojo) {
        var paramMeta = getParamMetamodel(paramIndex);
        return ManagedObject.adaptParameter(paramMeta, newParamValuePojo);
    }

    /**
     * Returns whether the pending parameter changed during reassessment.
     * @see ObjectActionParameter#reassessDefault(ParameterNegotiationModel)
     */
    public boolean reassessDefaults(final int paramIndexForReassessment) {
        return getParamMetamodel(paramIndexForReassessment).reassessDefault(this);
    }

    /**
     * Invalidates any consent cached previously.
     * Next query for visibility or usability will be reassessed.
     */
    public void invalidateVisibilityAndUsability(final int paramIndexForReassessment) {
        paramModels.get(paramIndexForReassessment)
            .ifPresent(ParameterModel::invalidateVisibilityAndUsability);
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
        var validityConsentForAction = this.validateParameterSetForAction();
        return validityConsentForAction!=null
                ? validityConsentForAction.getReasonAsString().orElse(null)
                : null;
    }

    // -- UTILITY

    /**
     * Creates a serializable {@link PendingParamsSnapshot}, that is bound to this
     * {@link ParameterNegotiationModel}.
     * @see PendingParamsSnapshot
     */
    public PendingParamsSnapshot createSnapshotModel() {
        return PendingParamsSnapshot.create(this);
    }

    /**
     * Returns a copy, but with a single param value replaced.
     */
    public ParameterNegotiationModel withParamValue(final int parameterIndex, @NonNull final ManagedObject paramValue) {
        return ParameterNegotiationModel.of(managedAction, getParamValues().replace(parameterIndex, paramValue));
    }

    // -- INTERNAL HOLDER OF PARAMETER BINDABLES

    @Log4j2 record ParameterModel(
            int paramIndex,
            @NonNull ObjectActionParameter metaModel,
            @NonNull ParameterNegotiationModel negotiationModel,
            @NonNull _BindableAbstract<ManagedObject> bindableParamValue,
            @NonNull BooleanBindable bindableParamValueDirtyFlag,
            @NonNull _BindableAbstract<String> bindableParamSearchArgument,
            @NonNull LazyObservable<String> observableParamValidation,
            @NonNull LazyObservable<Can<ManagedObject>> observableParamChoices,
            @NonNull LazyObservable<Consent> observableVisibilityConsent,
            @NonNull LazyObservable<Consent> observableUsabilityConsent,
            @NonNull Observable<String> observableParamAsTitle,
            @NonNull Observable<String> observableParamAsHtml,
            @NonNull _Lazy<Bindable<String>> bindableParamAsParsableTextLazy
        ) implements ManagedParameter {

        private ParameterModel(
            final int paramIndex,
            final @NonNull ParameterNegotiationModel negotiationModel,
            final @NonNull ManagedObject initialValue) {
            this(paramIndex, negotiationModel.act().getParameterByIndex(paramIndex), negotiationModel,
                // bindableParamValue
                _Bindables.forValue(initialValue),
                // bindableParamValueDirtyFlag
                _Bindables.forBoolean(false),
                // bindableParamSearchArgument
                _Bindables.forValue(null),
                // unused in canonical constructor  ..
                null, null, null, null, null, null, null);
        }

        // canonical constructor
        ParameterModel(
            final int paramIndex,
            @NonNull final ObjectActionParameter metaModel,
            @NonNull final ParameterNegotiationModel negotiationModel,
            @NonNull final _BindableAbstract<ManagedObject> bindableParamValue,
            @NonNull final BooleanBindable bindableParamValueDirtyFlag,
            @NonNull final _BindableAbstract<String> bindableParamSearchArgument,
            // unused ..
            final LazyObservable<String> observableParamValidation,
            final LazyObservable<Can<ManagedObject>> observableParamChoices,
            final LazyObservable<Consent> observableVisibilityConsent,
            final LazyObservable<Consent> observableUsabilityConsent,
            final Observable<String> observableParamAsTitle,
            final Observable<String> observableParamAsHtml,
            final _Lazy<Bindable<String>> bindableParamAsParsableTextLazy
        ) {
            this.paramIndex = paramIndex;
            this.metaModel = metaModel;
            this.negotiationModel = negotiationModel;

            this.bindableParamValue = bindableParamValue;
            this.bindableParamValueDirtyFlag = bindableParamValueDirtyFlag;
            this.bindableParamSearchArgument = bindableParamSearchArgument;

            bindableParamValue().setValueGuard(MmAssertionUtils.assertInstanceOf(metaModel().getElementType()));
            bindableParamValue().addListener((event, oldValue, newValue)->{
                if(newValue==null) {
                    // lift null to empty ...
                    bindableParamValue().setValue(metaModel().getEmpty()); // triggers this event again
                    return;
                }
                negotiationModel().onNewParamValue();
                bindableParamValueDirtyFlag().setValue(true); // set dirty whenever an update event happens
            });

            // has either autoComplete, choices, or none
            this.observableParamChoices = metaModel().hasAutoComplete()
                ? _Observables.lazy(()->
                    metaModel().getAutoComplete(
                            negotiationModel(),
                            bindableParamSearchArgument().getValue(),
                            InteractionInitiatedBy.USER))
                : metaModel().hasChoices()
                    ? _Observables.lazy(()->
                        getMetaModel().getChoices(negotiationModel(), InteractionInitiatedBy.USER))
                    : _Observables.lazy(Can::empty);

            // if has autoComplete, then activate the search argument
            if(metaModel().hasAutoComplete()) {
                this.bindableParamSearchArgument.addListener((e,o,n)->{
                    observableParamChoices().invalidate();
                });
            }

            // validate this parameter, but only when validationFeedback has been activated
            this.observableParamValidation = _Observables.lazy(()->
                isValidationFeedbackActive()
                    ? negotiationModel().validateImmediately(paramIndex)
                    : (String)null);

            this.observableVisibilityConsent = _Observables.lazy(()->
                metaModel().isVisible(
                        negotiationModel().interactionHead(),
                        negotiationModel().getParamValues(),
                        InteractionInitiatedBy.USER));
            this.observableUsabilityConsent = _Observables.lazy(()->
                metaModel().isUsable(
                        negotiationModel().interactionHead(),
                        negotiationModel().getParamValues(),
                        InteractionInitiatedBy.USER));

            // value types should have associated rederers via value semantics
            this.observableParamAsTitle = _BindingUtil
                    .bindAsFormated(TargetFormat.TITLE, metaModel(), bindableParamValue());
            this.observableParamAsHtml = _BindingUtil
                    .bindAsFormated(TargetFormat.HTML, metaModel(), bindableParamValue());
            // value types should have associated parsers/formatters via value semantics
            // except for composite value types, which might have not
            this.bindableParamAsParsableTextLazy = _Lazy.threadSafe(()->(Bindable<String>) _BindingUtil
                    .bindAsFormated(TargetFormat.PARSABLE_TEXT, metaModel(), bindableParamValue()));
        }

        public void invalidateChoicesAndValidation() {
            observableParamChoices.invalidate();
            observableParamValidation.invalidate();
        }

        public void invalidateVisibilityAndUsability() {
            observableVisibilityConsent.invalidate();
            observableUsabilityConsent.invalidate();
        }

        private boolean isValidationFeedbackActive() {
            return negotiationModel().getObservableValidationFeedbackActive().getValue();
        }

        // -- MANAGED PARAMETER

        @Override
        public Identifier getIdentifier() {
            return getMetaModel().getFeatureIdentifier();
        }

        @Override
        public String getFriendlyName() {
            ObjectActionParameter objectActionParameter = getMetaModel();
            return objectActionParameter.getCanonicalFriendlyName();
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
            return observableParamAsTitle;
        }

        @Override
        public Observable<String> getValueAsHtml() {
            return observableParamAsHtml;
        }

        @Override
        public boolean isValueAsParsableTextSupported() {
            return _BindingUtil.hasParser(metaModel);
        }

        @Override
        public Bindable<String> getValueAsParsableText() {
            return bindableParamAsParsableTextLazy.get();
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

        @Override
        public Optional<InteractionVeto> checkUsability(@NonNull final Can<ManagedObject> params) {

            try {
                var usabilityConsent =
                    getMetaModel()
                    .isUsable(negotiationModel().interactionHead(), params, InteractionInitiatedBy.USER);

                return usabilityConsent.isVetoed()
                    ? Optional.of(InteractionVeto.readonly(usabilityConsent))
                    : Optional.empty();

            } catch (final Exception ex) {
                log.warn(ex.getLocalizedMessage(), ex);
                return Optional.of(InteractionVeto.readonly(new Veto("failure during usability evaluation")));
            }

        }

        // -- OBJECT CONTRACT

        @Override
        public final boolean equals(final Object obj) {
            return obj instanceof ParameterModel other
                ? Objects.equals(this.getIdentifier(), other.getIdentifier())
                : false;
        }

        @Override
        public final int hashCode() {
            return Objects.hashCode(getIdentifier());
        }

        @Override
        public final String toString() {
            return "ParameterModel[id=%s]".formatted(getIdentifier());
        }

    }

}
