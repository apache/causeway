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

package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.commons.ClassExtensions;
import org.apache.isis.core.metamodel.commons.StringExtensions;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.consent.InteractionResultSet;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.MinLengthUtil;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.interactions.ActionArgUsabilityContext;
import org.apache.isis.core.metamodel.interactions.ActionArgValidityContext;
import org.apache.isis.core.metamodel.interactions.ActionArgVisibilityContext;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.DomainModelException;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import static org.apache.isis.core.commons.internal.base._With.requires;

import lombok.val;

public abstract class ObjectActionParameterAbstract 
implements ObjectActionParameter, FacetHolder.Delegating {

    private final FeatureType featureType;
    private final int number;
    private final ObjectActionDefault parentAction;
    private final TypedHolder peer;

    protected ObjectActionParameterAbstract(
            final FeatureType featureType,
            final int number,
            final ObjectActionDefault objectAction,
            final TypedHolder peer) {
        
        this.featureType = featureType;
        this.number = number;
        this.parentAction = objectAction;
        this.peer = requires(peer, "peer");
    }
    
    @Override
    public MetaModelContext getMetaModelContext() {
        return parentAction.getMetaModelContext();
    }

    @Override
    public FeatureType getFeatureType() {
        return featureType;
    }


    /**
     * Gets the proposed value of the {@link ManagedObject} (downcast as a
     * <code>MutableProposedHolder</code>, wrapping the proposed value into a
     * {@link ManagedObject}.
     */
    @Override
    public ManagedObject get(final ManagedObject owner, final InteractionInitiatedBy interactionInitiatedBy) {
        throw _Exceptions.unexpectedCodeReach();
        //FIXME[ISIS-1976] marked for removal (must be dead code, since MutableProposedHolder has no implementation)
        //        final MutableProposedHolder proposedHolder = getProposedHolder(owner);
        //        final Object proposed = proposedHolder.getProposed();
        //        return getObjectAdapterProvider().adapterFor(proposed);
    }

    //    protected MutableProposedHolder getProposedHolder(final ObjectAdapter owner) {
    //        if (!(owner instanceof MutableProposedHolder)) {
    //            throw new IllegalArgumentException("Instance should implement MutableProposedHolder");
    //        }
    //        return (MutableProposedHolder) owner;
    //    }

    /**
     * Parameter number, 0-based.
     */
    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public ObjectAction getAction() {
        return parentAction;
    }

    /**
     * NOT API, but exposed for the benefit of {@link ObjectActionParameterContributee}
     * and {@link ObjectActionParameterMixedIn}.
     */
    public TypedHolder getPeer() {
        return peer;
    }

    @Override
    public ObjectSpecification getSpecification() {
        return getSpecificationLoader().loadSpecification(peer.getType());
    }

    @Override
    public Identifier getIdentifier() {
        return parentAction.getIdentifier();
    }

    @Override
    public String getId() {
        return StringExtensions.asCamelLowerFirst(getName());
    }

    @Override
    public String getName() {
        final NamedFacet facet = getFacet(NamedFacet.class);
        if (facet != null && facet.value() != null) {
            return facet.value();
        }
        val singularName = getSpecification().getSingularName();
        val parameters = getAction().getParameters(this::equalsShortIdentifier);
        if (parameters.isCardinalityOne()) {
            return singularName;
        }
        final int indexOf = parameters.indexOf(this);
        return singularName + " " + (indexOf + 1);
    }
    
    private boolean equalsShortIdentifier(final ObjectActionParameter objParam) {
        val spec1 = objParam.getSpecification(); 
        val spec2 = getSpecification();
        return spec1.getShortIdentifier().toLowerCase()
                .equals(spec2.getShortIdentifier().toLowerCase());
    }

    @Override
    public String getDescription() {
        final DescribedAsFacet facet = getFacet(DescribedAsFacet.class);
        final String description = facet.value();
        return description == null ? "" : description;
    }

    @Override
    public boolean isOptional() {
        final MandatoryFacet facet = getFacet(MandatoryFacet.class);
        return facet.isInvertedSemantics();
    }

    public Consent isUsable() {
        return Allow.DEFAULT;
    }

    // -- FacetHolder

    @Override
    public FacetHolder getFacetHolder() {
        return peer;
    }

    // -- AutoComplete

    @Override
    public boolean hasAutoComplete() {
        final ActionParameterAutoCompleteFacet facet = getFacet(ActionParameterAutoCompleteFacet.class);
        return facet != null;
    }

    @Override
    public Can<ManagedObject> getAutoComplete(
            final ManagedObject adapter,
            final List<ManagedObject> pendingArgs,
            final String searchArg,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final List<ManagedObject> adapters = _Lists.newArrayList();
        final ActionParameterAutoCompleteFacet facet = getFacet(ActionParameterAutoCompleteFacet.class);

        if (facet != null) {
            final Object[] choices = facet
                    .autoComplete(adapter, pendingArgs, searchArg, interactionInitiatedBy);
            checkChoicesOrAutoCompleteType(getSpecificationLoader(), choices, getSpecification());
            for (final Object choice : choices) {
                adapters.add(getObjectManager().adapt(choice));
            }
        }
        return Can.ofCollection(adapters);
    }

    @Override
    public int getAutoCompleteMinLength() {
        final ActionParameterAutoCompleteFacet facet = getFacet(ActionParameterAutoCompleteFacet.class);
        return facet != null? facet.getMinLength(): MinLengthUtil.MIN_LENGTH_DEFAULT;
    }



    // -- Choices

    @Override
    public boolean hasChoices() {
        final ActionParameterChoicesFacet choicesFacet = getFacet(ActionParameterChoicesFacet.class);
        return choicesFacet != null;
    }

    @Override
    public Can<ManagedObject> getChoices(
            final ManagedObject adapter,
            final List<ManagedObject> pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        val args = argsForDefaultOrChoices(adapter, pendingArgs);
        val target = targetForDefaultOrChoices(adapter);

        return findChoices(target, args, interactionInitiatedBy);
    }

    private Can<ManagedObject> findChoices(
            final ManagedObject target,
            final List<ManagedObject> pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        final List<ManagedObject> adapters = _Lists.newArrayList();
        final ActionParameterChoicesFacet facet = getFacet(ActionParameterChoicesFacet.class);

        if (facet != null) {
            final Object[] choices = facet.getChoices(target, pendingArgs, interactionInitiatedBy);
            checkChoicesOrAutoCompleteType(getSpecificationLoader(), choices, getSpecification());
            for (final Object choice : choices) {
                ManagedObject adapter = choice != null? getObjectManager().adapt(choice) : null;
                adapters.add(adapter);
            }
        }
        return Can.ofCollection(adapters);
    }

    // -- Defaults

    @Override
    public ManagedObject getDefault(
            final ManagedObject adapter,
            final List<ManagedObject> pendingArgs,
            final Integer paramNumUpdated) {

        final ManagedObject target = targetForDefaultOrChoices(adapter);
        val args = argsForDefaultOrChoices(adapter, pendingArgs);

        return findDefault(target, args, paramNumUpdated);
    }

    private ManagedObject findDefault(
            final ManagedObject target,
            final List<ManagedObject> args,
            final Integer paramNumUpdated) {
        
        final ActionParameterDefaultsFacet defaultsFacet = getFacet(ActionParameterDefaultsFacet.class);
        if (defaultsFacet != null) {
            final Object dflt = defaultsFacet.getDefault(target, args, paramNumUpdated);
            if (dflt == null) {
                // it's possible that even though there is a default facet, when
                // invoked it is unable to return a default.
                return null;
            }
            return getObjectManager().adapt(dflt);
        }
        return null;
    }

    /**
     * Hook method; {@link ObjectActionParameterContributee contributed action parameter}s override.
     */
    protected ManagedObject targetForDefaultOrChoices(final ManagedObject adapter) {
        return adapter;
    }

    /**
     * Hook method; {@link ObjectActionParameterContributee contributed action parameter}s override.
     */
    protected List<ManagedObject> argsForDefaultOrChoices(
            final ManagedObject adapter,
            final List<ManagedObject> argumentsIfAvailable) {
        return argumentsIfAvailable;
    }


    // helpers
    static void checkChoicesOrAutoCompleteType(
            final SpecificationLoader specificationLookup,
            final Object[] objects,
            final ObjectSpecification paramSpec) {
        for (final Object object : objects) {

            if(object == null) {
                continue;
            }

            // check type, but wrap first
            // (eg we treat int.class and java.lang.Integer.class as compatible with each other)
            final Class<?> choiceClass = object.getClass();
            final Class<?> paramClass = paramSpec.getCorrespondingClass();

            final Class<?> choiceWrappedClass = ClassExtensions.asWrappedIfNecessary(choiceClass);
            final Class<?> paramWrappedClass = ClassExtensions.asWrappedIfNecessary(paramClass);

            final ObjectSpecification choiceWrappedSpec = specificationLookup.loadSpecification(choiceWrappedClass);
            final ObjectSpecification paramWrappedSpec = specificationLookup.loadSpecification(paramWrappedClass);

            // type returned by choices must be an instance of the param type
            // in other words <param type> is assignable from <choices type>

            // TODO: should implement this instead as a MetaModelValidator
            if (!choiceWrappedSpec.isOfType(paramWrappedSpec)) {
                throw new DomainModelException(String.format(
                        "Type incompatible with parameter type; expected %s, but was %s",
                        paramSpec.getFullIdentifier(), choiceClass.getName()));
            }
        }
    }

  
    //region > Visibility

    private ActionArgVisibilityContext createArgumentVisibilityContext(
            final ManagedObject objectAdapter,
            final List<ManagedObject> pendingArgs,
            final int position,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        return new ActionArgVisibilityContext(
                objectAdapter, parentAction, getIdentifier(), pendingArgs, position, interactionInitiatedBy);
    }

    @Override
    public Consent isVisible(
            final ManagedObject targetAdapter,
            final List<ManagedObject> pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final VisibilityContext<?> ic = createArgumentVisibilityContext(
                targetAdapter, pendingArgs, getNumber(), interactionInitiatedBy);

        final InteractionResult visibleResult = InteractionUtils.isVisibleResult(this, ic);
        return visibleResult.createConsent();
    }

    //endregion

    //region > Usability

    private ActionArgUsabilityContext createArgumentUsabilityContext(
            final ManagedObject objectAdapter,
            final List<ManagedObject> pendingArgs,
            final int position,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        return new ActionArgUsabilityContext(
                objectAdapter, 
                parentAction, 
                getIdentifier(), 
                pendingArgs, 
                position, 
                interactionInitiatedBy);
    }

    @Override
    public Consent isUsable(
            final ManagedObject targetAdapter,
            final List<ManagedObject> pendingArgs,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final UsabilityContext<?> ic = createArgumentUsabilityContext(
                targetAdapter, pendingArgs, getNumber(), interactionInitiatedBy);

        final InteractionResult usableResult = InteractionUtils.isUsableResult(this, ic);
        return usableResult.createConsent();
    }

    //endregion


    // -- Validation

    @Override
    public ActionArgValidityContext createProposedArgumentInteractionContext(
            final ManagedObject objectAdapter,
            final List<ManagedObject> proposedArguments,
            final int position,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        return new ActionArgValidityContext(
                objectAdapter, parentAction, getIdentifier(), proposedArguments, position, interactionInitiatedBy);
    }

    @Override
    public String isValid(
            final ManagedObject objectAdapter,
            final Object proposedValue,
            final InteractionInitiatedBy interactionInitiatedBy) {

        ManagedObject proposedValueAdapter = null;
        ObjectSpecification proposedValueSpec;
        if(proposedValue != null) {
            proposedValueAdapter = getObjectManager().adapt(proposedValue);
            if(proposedValueAdapter == null) {
                return null;
            }
            proposedValueSpec = proposedValueAdapter.getSpecification();
            if(!proposedValueSpec.isOfType(proposedValueSpec)) {
                return null;
            }
        }

        val argumentAdapters = arguments(proposedValueAdapter);
        final ValidityContext<?> ic = createProposedArgumentInteractionContext(
                objectAdapter, argumentAdapters, getNumber(), interactionInitiatedBy);

        final InteractionResultSet buf = new InteractionResultSet();
        InteractionUtils.isValidResultSet(this, ic, buf);
        if (buf.isVetoed()) {
            return buf.getInteractionResult().getReason();
        }
        return null;

    }

    /**
     * TODO: this is not ideal, because we can only populate the array for
     * single argument, rather than the entire argument set. Instead, we ought
     * to do this in two passes, one to build up the argument set as a single
     * unit, and then validate each in turn.
     */
    private List<ManagedObject> arguments(final ManagedObject proposedValue) {
        final int paramCount = getAction().getParameterCount();
        final int paramIndex = getNumber();
        val arguments = new ArrayList<ManagedObject>(paramCount);
        for(int i=0; i<paramCount; ++i) {
            arguments.add(i==paramIndex ? proposedValue : ManagedObject.empty());
        }
        return Collections.unmodifiableList(arguments);
    }



    // -- Dependencies (from parent)

    protected SpecificationLoader getSpecificationLoader() {
        return parentAction.getSpecificationLoader();
    }

    protected ObjectManager getObjectManager() {
        return parentAction.getObjectManager();
    }

}
