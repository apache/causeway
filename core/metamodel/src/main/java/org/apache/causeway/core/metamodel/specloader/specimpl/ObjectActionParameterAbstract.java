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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.exceptions.unrecoverable.DomainModelException;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.commons.ClassExtensions;
import org.apache.causeway.core.metamodel.consent.Allow;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.all.described.ParamDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.named.ParamNamedFacet;
import org.apache.causeway.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.causeway.core.metamodel.facets.param.autocomplete.MinLengthUtil;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.causeway.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.causeway.core.metamodel.interactions.ActionArgUsabilityContext;
import org.apache.causeway.core.metamodel.interactions.ActionArgValidityContext;
import org.apache.causeway.core.metamodel.interactions.ActionArgVisibilityContext;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.interactions.InteractionUtils;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public abstract class ObjectActionParameterAbstract
implements
    ObjectActionParameter {

    @Getter(onMethod_ = {@Override}) private final FeatureType featureType;
    @Getter(onMethod_ = {@Override}) private final int parameterIndex;
    private final ObjectActionDefault parentAction;
    private final String javaSourceParamName;
    private final ObjectSpecification paramElementType;

    protected ObjectActionParameterAbstract(
            final FeatureType featureType,
            final int parameterIndex,
            final ObjectSpecification paramElementType,
            final ObjectActionDefault objectAction) {

        this.featureType = featureType;
        this.parameterIndex = parameterIndex;
        this.parentAction = objectAction;
        this.paramElementType = paramElementType;

        this.javaSourceParamName =
                objectAction.getFacetedMethod().getMethod().getParameters()[parameterIndex].getName();
    }

    @Override
    public MetaModelContext getMetaModelContext() {
        return parentAction.getMetaModelContext();
    }

    @Override
    public ManagedObject get(final ManagedObject owner, final InteractionInitiatedBy interactionInitiatedBy) {
        throw _Exceptions.unexpectedCodeReach(); // not available for params
    }

    @Override
    public ObjectAction getAction() {
        return parentAction;
    }

    @Override
    public ObjectSpecification getElementType() {
        return paramElementType;
    }

    @Getter(lazy = true, onMethod_ = {@Override})
    private final Identifier featureIdentifier = getAction().getFeatureIdentifier()
        .withParameterIndex(getParameterIndex());

    @Override
    public String getId() {
        return javaSourceParamName;
    }

    @Override
    public final String getFriendlyName(final Supplier<ManagedObject> domainObjectProvider) {
        //as we don't support imperative naming for parameters yet ..
        return staticFriendlyName();
    }

    @Override
    public final Optional<String> getStaticFriendlyName() {
        return Optional.of(staticFriendlyName());
    }

    @Override
    public final String getCanonicalFriendlyName() {
        //as we don't support imperative naming for parameters yet ..
        return staticFriendlyName();
    }

    private String staticFriendlyName() {
        return lookupFacet(ParamNamedFacet.class)
        .map(ParamNamedFacet::translated)
        .orElseThrow(()->_Exceptions
                .unrecoverable("action parameters must have a ParamNamedFacet %s", getFeatureIdentifier()));
    }

    @Override
    public final Optional<String> getDescription(final Supplier<ManagedObject> domainObjectProvider) {
        //as we don't support imperative naming for parameters yet ..
        return staticDescription();
    }

    @Override
    public final Optional<String> getStaticDescription() {
        return staticDescription();
    }

    @Override
    public final Optional<String> getCanonicalDescription() {
        //as we don't support imperative naming for parameters yet ..
        return staticDescription();
    }

    private Optional<String> staticDescription() {
        return lookupFacet(ParamDescribedFacet.class)
        .map(ParamDescribedFacet::translated);
    }

    public Consent isUsable() {
        return Allow.DEFAULT;
    }

    // -- FacetHolder

    @Override
    public FacetHolder getFacetHolder() {
        // that is the faceted method parameter
        return parentAction.getFacetedMethod().getParameters().getElseFail(parameterIndex);
    }

    // -- AutoComplete

    @Override
    public boolean hasAutoComplete() {
        val actionParameterAutoCompleteFacet = getFacet(ActionParameterAutoCompleteFacet.class);
        return actionParameterAutoCompleteFacet != null;
    }

    @Override
    public Can<ManagedObject> getAutoComplete(
            final ParameterNegotiationModel pendingArgs,
            final String searchArg,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val autoCompleteFacet = getFacet(ActionParameterAutoCompleteFacet.class);
        if (autoCompleteFacet == null) {
            return Can.empty();
        }

        val paramSpec = getElementType();

        val visibleChoices = autoCompleteFacet
                .autoComplete(paramSpec,
                        pendingArgs.getActionTarget(),
                        pendingArgs.getParamValues(),
                        searchArg,
                        interactionInitiatedBy);
        checkChoicesOrAutoCompleteType(getSpecificationLoader(), visibleChoices, paramSpec);

        return visibleChoices;
    }

    @Override
    public int getAutoCompleteMinLength() {
        final ActionParameterAutoCompleteFacet facet = getFacet(ActionParameterAutoCompleteFacet.class);
        return facet != null? facet.getMinLength(): MinLengthUtil.MIN_LENGTH_DEFAULT;
    }


    // -- Choices

    @Override
    public boolean hasChoices() {
        val choicesFacet = getFacet(ActionParameterChoicesFacet.class);
        return choicesFacet != null;
    }

    @Override
    public Can<ManagedObject> getChoices(
            final ParameterNegotiationModel pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val paramSpec = getElementType();
        val choicesFacet = getFacet(ActionParameterChoicesFacet.class);
        if (choicesFacet == null) {
            return Can.empty();
        }

        val visibleChoices = choicesFacet.getChoices(paramSpec,
                pendingArgs.getHead(),
                pendingArgs.getParamValues(),
                interactionInitiatedBy);
        checkChoicesOrAutoCompleteType(getSpecificationLoader(), visibleChoices, paramSpec);

        return visibleChoices;
    }

    // -- Defaults

    @Override
    @NonNull
    public ManagedObject getDefault(
            final @NonNull ParameterNegotiationModel pendingArgs) {

        val paramSpec = getElementType();
        val defaults = lookupNonFallbackFacet(ActionParameterDefaultsFacet.class)
                .map(defaultsFacet->defaultsFacet.getDefault(pendingArgs))
                .orElseGet(Can::empty);

        if(this.isPlural()) {
            final Can<ManagedObject> pluralDefaults = defaults
            // post processing each entry
            .map(obj->ManagedObjects.emptyToDefault(paramSpec, !isOptional(), obj));
            // pack up
            val packed = ManagedObject.packed(paramSpec, pluralDefaults);
            return packed;
        }

        val scalarDefault = defaults.getFirst()
              .orElseGet(()->ManagedObject.empty(paramSpec));

        return ManagedObjects
                      .emptyToDefault(paramSpec, !isOptional(), scalarDefault);
    }

    // helpers
    static void checkChoicesOrAutoCompleteType(
            final SpecificationLoader specificationLookup,
            final Can<ManagedObject> choices,
            final ObjectSpecification paramSpec) {

        for (final ManagedObject choice : choices) {

            val choicePojo = choice.getPojo();

            if(choicePojo == null) {
                continue;
            }

            // check type, but wrap first
            // (eg we treat int.class and java.lang.Integer.class as compatible with each other)
            final Class<?> choiceClass = choicePojo.getClass();
            final Class<?> paramClass = paramSpec.getCorrespondingClass();

            final Class<?> choiceWrappedClass = ClassExtensions.asWrappedIfNecessary(choiceClass);
            final Class<?> paramWrappedClass = ClassExtensions.asWrappedIfNecessary(paramClass);

            final ObjectSpecification choiceWrappedSpec = specificationLookup.loadSpecification(choiceWrappedClass);
            final ObjectSpecification paramWrappedSpec = specificationLookup.loadSpecification(paramWrappedClass);

            // type returned by choices must be an instance of the param type
            // in other words <param type> is assignable from <choices type>

            // TODO: should implement this instead as a MetaModelValidator (subject to [CAUSEWAY-3172])
            if (!choiceWrappedSpec.isOfType(paramWrappedSpec)) {
                throw new DomainModelException(String.format(
                        "Type incompatible with parameter type; expected %s, but was %s",
                        paramSpec.getFullIdentifier(), choiceClass.getName()));
            }
        }
    }


    // > Visibility

    private ActionArgVisibilityContext createArgumentVisibilityContext(
            final InteractionHead head,
            final Can<ManagedObject> pendingArgs,
            final int position,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return new ActionArgVisibilityContext(
                head, parentAction, getFeatureIdentifier(), pendingArgs, position, interactionInitiatedBy);
    }

    @Override
    public Consent isVisible(
            final InteractionHead head,
            final Can<ManagedObject> pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val visibilityContext = createArgumentVisibilityContext(
                head, pendingArgs, getParameterIndex(), interactionInitiatedBy);

        return InteractionUtils.isVisibleResult(this, visibilityContext).createConsent();
    }

    // > Usability

    private ActionArgUsabilityContext createArgumentUsabilityContext(
            final InteractionHead head,
            final Can<ManagedObject> pendingArgs,
            final int position,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return new ActionArgUsabilityContext(
                head,
                parentAction,
                getFeatureIdentifier(),
                pendingArgs,
                position,
                interactionInitiatedBy);
    }

    @Override
    public Consent isUsable(
            final InteractionHead head,
            final Can<ManagedObject> pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val usabilityContext = createArgumentUsabilityContext(
                head, pendingArgs, getParameterIndex(), interactionInitiatedBy);

        val usableResult = InteractionUtils.isUsableResult(this, usabilityContext);
        return usableResult.createConsent();
    }


    // -- Validation

    @Override
    public ActionArgValidityContext createProposedArgumentInteractionContext(
            final InteractionHead head,
            final Can<ManagedObject> proposedArguments,
            final int position,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return new ActionArgValidityContext(
                head, parentAction, getFeatureIdentifier(), proposedArguments, position, interactionInitiatedBy);
    }

    @Override
    public Consent isValid(
            final InteractionHead head,
            final Can<ManagedObject> pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val validityContext = createProposedArgumentInteractionContext(
                head, pendingArgs, getParameterIndex(), interactionInitiatedBy);

        val validResult = InteractionUtils.isValidResult(this, validityContext);
        return validResult.createConsent();
    }

}
