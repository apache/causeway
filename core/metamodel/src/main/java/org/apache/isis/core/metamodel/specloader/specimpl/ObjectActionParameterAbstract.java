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

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.lang.ClassExtensions;
import org.apache.isis.core.commons.lang.ListExtensions;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.QuerySubmitter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResultSet;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.MultiTypedFacet;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.interactions.ActionArgumentContext;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.spec.DomainModelException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.facets.object.choices.ChoicesFacetFromBoundedAbstract;
import org.apache.isis.core.metamodel.facets.param.autocomplete.MinLengthUtil;

public abstract class ObjectActionParameterAbstract implements ObjectActionParameter {

    private final int number;
    private final ObjectActionImpl parentAction;
    private final TypedHolder peer;

    protected ObjectActionParameterAbstract(final int number, final ObjectActionImpl objectAction, final TypedHolder peer) {
        this.number = number;
        this.parentAction = objectAction;
        this.peer = peer;
    }

    /**
     * Subclasses should override either {@link #isObject()} or
     * {@link #isCollection()}.
     */
    @Override
    public boolean isObject() {
        return false;
    }

    /**
     * Subclasses should override either {@link #isObject()} or
     * {@link #isCollection()}.
     */
    @Override
    public boolean isCollection() {
        return false;
    }

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
     * NOT API, but exposed for the benefit of {@link ObjectActionParameterContributee}.
     */
    public TypedHolder getPeer() {
        return peer;
    }

    @Override
    public ObjectSpecification getSpecification() {
        return ObjectMemberAbstract.getSpecification(getSpecificationLookup(), peer.getType());
    }

    @Override
    public Identifier getIdentifier() {
        return parentAction.getIdentifier();
    }

    @Override
    public String getId() {
        final NamedFacet facet = getFacet(NamedFacet.class);
        if (facet != null && facet.value() != null) {
            return StringExtensions.asCamelLowerFirst(facet.value());
        }
        final String name = getSpecification().getSingularName();
        final List<ObjectActionParameter> parameters = this.getAction().getParameters(new Filter<ObjectActionParameter>() {

            @Override
            public boolean accept(final ObjectActionParameter t) {
                return equalsShortIdentifier(t.getSpecification(), getSpecification());
            }

            protected boolean equalsShortIdentifier(final ObjectSpecification spec1, final ObjectSpecification spec2) {
                return spec1.getShortIdentifier().toLowerCase().equals(spec2.getShortIdentifier().toLowerCase());
            }
        });
        if (parameters.size() == 1) {
            return StringExtensions.asCamelLowerFirst(name);
        }
        final int indexOf = parameters.indexOf(this);
        return StringExtensions.asCamelLowerFirst(name + (indexOf + 1));
    }

    @Override
    public String getName() {
        final NamedFacet facet = getFacet(NamedFacet.class);
        if (facet != null && facet.value() != null) {
            return facet.value();
        }
        final String name = getSpecification().getSingularName();
        final List<ObjectActionParameter> parameters = getAction().getParameters(new Filter<ObjectActionParameter>() {

            @Override
            public boolean accept(final ObjectActionParameter t) {
                return equalsShortIdentifier(t.getSpecification(), getSpecification());
            }

            protected boolean equalsShortIdentifier(final ObjectSpecification spec1, final ObjectSpecification spec2) {
                return spec1.getShortIdentifier().toLowerCase().equals(spec2.getShortIdentifier().toLowerCase());
            }
        });
        if (parameters.size() == 1) {
            return name;
        }
        final int indexOf = parameters.indexOf(this);
        return name + " " + (indexOf + 1);
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

    // //////////////////////////////////////////////////////////
    // FacetHolder
    // //////////////////////////////////////////////////////////

    @Override
    public boolean containsFacet(final Class<? extends Facet> facetType) {
        return peer != null ? peer.containsFacet(facetType) : false;
    }

    @Override
    public boolean containsDoOpFacet(final Class<? extends Facet> facetType) {
        return peer == null ? false : peer.containsDoOpFacet(facetType);
    }

    @Override
    public <T extends Facet> T getFacet(final Class<T> cls) {
        return peer != null ? peer.getFacet(cls) : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Facet>[] getFacetTypes() {
        return peer != null ? peer.getFacetTypes() : new Class[] {};
    }

    @Override
    public List<Facet> getFacets(final Filter<Facet> filter) {
        return peer != null ? peer.getFacets(filter) : Lists.<Facet> newArrayList();
    }

    @Override
    public void addFacet(final Facet facet) {
        if (peer != null) {
            peer.addFacet(facet);
        }
    }

    @Override
    public void addFacet(final MultiTypedFacet facet) {
        if (peer != null) {
            peer.addFacet(facet);
        }
    }

    @Override
    public void removeFacet(final Facet facet) {
        if (peer != null) {
            peer.removeFacet(facet);
        }
    }

    @Override
    public void removeFacet(final Class<? extends Facet> facetType) {
        if (peer != null) {
            peer.removeFacet(facetType);
        }
    }


    // /////////////////////////////////////////////////////////////
    // AutoComplete
    // /////////////////////////////////////////////////////////////

    @Override
    public boolean hasAutoComplete() {
        final ActionParameterAutoCompleteFacet facet = getFacet(ActionParameterAutoCompleteFacet.class);
        return facet != null;
    }

    @Override
    public ObjectAdapter[] getAutoComplete(ObjectAdapter adapter, String searchArg) {
        final List<ObjectAdapter> adapters = Lists.newArrayList();
        final ActionParameterAutoCompleteFacet facet = getFacet(ActionParameterAutoCompleteFacet.class);

        if (facet != null) {
            final Object[] choices = facet.autoComplete(adapter, searchArg);
            checkChoicesOrAutoCompleteType(getSpecificationLookup(), choices, getSpecification());
            for (final Object choice : choices) {
                adapters.add(getAdapterMap().adapterFor(choice));
            }
        }
        /* // now incorporated into above choices processing (BoundedFacet is no more)
        if (adapters.size() == 0 && ChoicesFacetUtils.hasChoices(getSpecification())) {
            addAllInstancesForType(adapters);
        }
        */
        return adapters.toArray(new ObjectAdapter[0]);
    }

    @Override
    public int getAutoCompleteMinLength() {
        final ActionParameterAutoCompleteFacet facet = getFacet(ActionParameterAutoCompleteFacet.class);
        return facet != null? facet.getMinLength(): MinLengthUtil.MIN_LENGTH_DEFAULT;
    }


    // /////////////////////////////////////////////////////////////
    // Choices
    // /////////////////////////////////////////////////////////////

    @Override
    public boolean hasChoices() {
        final ActionParameterChoicesFacet choicesFacet = getFacet(ActionParameterChoicesFacet.class);
        return choicesFacet != null;
    }

    @Override
    public ObjectAdapter[] getChoices(final ObjectAdapter adapter, final ObjectAdapter[] argumentsIfAvailable) {
        final List<ObjectAdapter> argListIfAvailable = ListExtensions.mutableCopy(argumentsIfAvailable);
        
        final ObjectAdapter target = targetForDefaultOrChoices(adapter, argListIfAvailable);
        final List<ObjectAdapter> args = argsForDefaultOrChoices(adapter, argListIfAvailable);
        
        return findChoices(target, args);
    }

    private ObjectAdapter[] findChoices(final ObjectAdapter target, final List<ObjectAdapter> args) {
        final List<ObjectAdapter> adapters = Lists.newArrayList();
        final ActionParameterChoicesFacet facet = getFacet(ActionParameterChoicesFacet.class);

        if (facet != null) {
            final Object[] choices = facet.getChoices(target, args);
            checkChoicesOrAutoCompleteType(getSpecificationLookup(), choices, getSpecification());
            for (final Object choice : choices) {
                ObjectAdapter adapter = choice != null? getAdapterMap().adapterFor(choice) : null;
                adapters.add(adapter);
            }
        }
        // now incorporated into above choices processing (BoundedFacet is no more)
        /* 
           if (adapters.size() == 0 && BoundedFacetUtils.isBoundedSet(getSpecification())) {
            addAllInstancesForType(adapters);
        }
        */
        return adapters.toArray(new ObjectAdapter[0]);
    }
    
    // /////////////////////////////////////////////////////////////
    // Defaults
    // /////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter getDefault(final ObjectAdapter adapter) {
        
        final ObjectAdapter target = targetForDefaultOrChoices(adapter, null);
        final List<ObjectAdapter> args = argsForDefaultOrChoices(adapter, null);
        
        return findDefault(target, args);
    }

    private ObjectAdapter findDefault(
            final ObjectAdapter target, 
            final List<ObjectAdapter> args) {
        final ActionParameterDefaultsFacet defaultsFacet = getFacet(ActionParameterDefaultsFacet.class);
        if (defaultsFacet != null) {
            final Object dflt = defaultsFacet.getDefault(target, args);
            if (dflt == null) {
                // it's possible that even though there is a default facet, when
                // invoked it is unable to return a default.
                return null;
            }
            return getAdapterMap().adapterFor(dflt);
        }
        return null;
    }

    /**
     * Hook method; {@link ObjectActionParameterContributee contributed action parameter}s override.
     */
    protected ObjectAdapter targetForDefaultOrChoices(ObjectAdapter adapter, final List<ObjectAdapter> argumentsIfAvailable) {
        return adapter;
    }

    /**
     * Hook method; {@link ObjectActionParameterContributee contributed action parameter}s override.
     */
    protected List<ObjectAdapter> argsForDefaultOrChoices(final ObjectAdapter adapter, final List<ObjectAdapter> argumentsIfAvailable) {
        return argumentsIfAvailable;
    }

    
    // helpers

    static void checkChoicesOrAutoCompleteType(final SpecificationLoader specificationLookup, final Object[] objects, final ObjectSpecification paramSpec) {
        for (final Object object : objects) {

            if(object == null) {
                continue;
            }
            
            // check type, but wrap first 
            // (eg we treat int.class and java.lang.Integer.class as compatible with each other)
            final Class<? extends Object> choiceClass = object.getClass();
            final Class<?> paramClass = paramSpec.getCorrespondingClass();
            
            final Class<? extends Object> choiceWrappedClass = ClassExtensions.asWrappedIfNecessary(choiceClass);
            final Class<? extends Object> paramWrappedClass = ClassExtensions.asWrappedIfNecessary(paramClass);
            
            final ObjectSpecification choiceWrappedSpec = specificationLookup.loadSpecification(choiceWrappedClass);
            final ObjectSpecification paramWrappedSpec = specificationLookup.loadSpecification(paramWrappedClass);
            
            if (!choiceWrappedSpec.isOfType(paramWrappedSpec)) {
                throw new DomainModelException("Type incompatible with parameter type; expected " + paramSpec.getFullIdentifier() + ", but was " + choiceClass.getName());
            }
        }
    }

    /**
     * unused - incorporated into the {@link ChoicesFacetFromBoundedAbstract}
     */
    @SuppressWarnings("unused")
    private <T> void addAllInstancesForType(final List<ObjectAdapter> adapters) {
        final Query<T> query = new QueryFindAllInstances<T>(getSpecification().getFullIdentifier());
        final List<ObjectAdapter> allInstancesAdapter = getQuerySubmitter().allMatchingQuery(query);
        for (final ObjectAdapter choiceAdapter : allInstancesAdapter) {
            adapters.add(choiceAdapter);
        }
    }

    
    // /////////////////////////////////////////////////////////////
    // Validation
    // /////////////////////////////////////////////////////////////

    @Override
    public ActionArgumentContext createProposedArgumentInteractionContext(final AuthenticationSession session, final InteractionInvocationMethod invocationMethod, final ObjectAdapter targetObject, final ObjectAdapter[] proposedArguments, final int position) {
        return new ActionArgumentContext(getDeploymentCategory(), getAuthenticationSession(), invocationMethod, targetObject, getIdentifier(), proposedArguments, position);
    }

    @Override
    public String isValid(final ObjectAdapter adapter, final Object proposedValue, final Localization localization) {
        
        ObjectAdapter proposedValueAdapter = null;
        ObjectSpecification proposedValueSpec;
        if(proposedValue != null) {
            proposedValueAdapter = getAdapterMap().adapterFor(proposedValue);
            proposedValueSpec = proposedValueAdapter.getSpecification();
            if(!proposedValueSpec.isOfType(proposedValueSpec)) {
                proposedValueAdapter = doCoerceProposedValue(adapter, proposedValue, localization);
            }
            
            // check has been coerced into correct type; otherwise give up
            if(proposedValueAdapter == null) {
                return null;
            }
            proposedValueSpec = proposedValueAdapter.getSpecification();
            if(!proposedValueSpec.isOfType(proposedValueSpec)) {
                return null;
            }
        }
        

        final ValidityContext<?> ic = createProposedArgumentInteractionContext(getAuthenticationSession(), InteractionInvocationMethod.BY_USER, adapter, arguments(proposedValueAdapter), getNumber());

        final InteractionResultSet buf = new InteractionResultSet();
        InteractionUtils.isValidResultSet(this, ic, buf);
        if (buf.isVetoed()) {
            return buf.getInteractionResult().getReason();
        }
        return null;

    }

    /**
     * Optional hook for parsing.
     */
    protected ObjectAdapter doCoerceProposedValue(ObjectAdapter adapter, Object proposedValue, final Localization localization) {
        return null;
    }

    /**
     * TODO: this is not ideal, because we can only populate the array for
     * single argument, rather than the entire argument set. Instead, we ought
     * to do this in two passes, one to build up the argument set as a single
     * unit, and then validate each in turn.
     */
    private ObjectAdapter[] arguments(final ObjectAdapter proposedValue) {
        final int parameterCount = getAction().getParameterCount();
        final ObjectAdapter[] arguments = new ObjectAdapter[parameterCount];
        arguments[getNumber()] = proposedValue;
        return arguments;
    }

    // /////////////////////////////////////////////////////////////
    // Dependencies (from parent)
    // /////////////////////////////////////////////////////////////

    private DeploymentCategory getDeploymentCategory() {
        return parentAction.getDeploymentCategory();
    }

    protected SpecificationLoader getSpecificationLookup() {
        return parentAction.getSpecificationLookup();
    }

    protected AuthenticationSessionProvider getAuthenticationSessionProvider() {
        return parentAction.getAuthenticationSessionProvider();
    }

    protected AdapterManager getAdapterMap() {
        return parentAction.getAdapterManager();
    }

    protected QuerySubmitter getQuerySubmitter() {
        return parentAction.getQuerySubmitter();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return getAuthenticationSessionProvider().getAuthenticationSession();
    }

}
