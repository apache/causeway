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


package org.apache.isis.core.metamodel.specloader.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.core.commons.filters.Filter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.exceptions.ModelException;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.MultiTypedFacet;
import org.apache.isis.core.metamodel.facets.actions.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.facets.naming.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.naming.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.propparam.validate.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.interactions.ActionArgumentContext;
import org.apache.isis.core.metamodel.runtimecontext.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.runtimecontext.AdapterMap;
import org.apache.isis.core.metamodel.runtimecontext.QuerySubmitter;
import org.apache.isis.core.metamodel.runtimecontext.SpecificationLookup;
import org.apache.isis.core.metamodel.runtimecontext.spec.feature.ObjectMemberAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationFacets;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.specloader.internal.peer.TypedHolder;


public abstract class ObjectActionParameterAbstract implements ObjectActionParameter {

    private final int number;
    private final ObjectActionImpl parentAction;
    private final TypedHolder peer;

    protected ObjectActionParameterAbstract(
            final int number,
            final ObjectActionImpl objectAction,
            final TypedHolder peer) {
        this.number = number;
        this.parentAction = objectAction;
        this.peer = peer;
    }

    /**
     * Subclasses should override either {@link #isObject()} or {@link #isCollection()}.
     */
    @Override
    public boolean isObject() {
        return false;
    }

    /**
     * Subclasses should override either {@link #isObject()} or {@link #isCollection()}.
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

    @Override
    public ObjectSpecification getSpecification() {
        return ObjectMemberAbstract.getSpecification(getSpecificationLookup(), peer.getType());
    }

    @Override
    public Identifier getIdentifier() {
        return parentAction.getIdentifier();
    }

    @Override
    public String getName() {
        final NamedFacet facet = getFacet(NamedFacet.class);
        String name = facet == null ? null : facet.value();
        name = name == null ? getSpecification().getSingularName() : name;
        return name;
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
    public <T extends Facet> T getFacet(final Class<T> cls) {
        return peer != null ? peer.getFacet(cls) : null;
    }

    @Override
    public Class<? extends Facet>[] getFacetTypes() {
        return peer != null ? peer.getFacetTypes() : new Class[] {};
    }

    @Override
    public Facet[] getFacets(final Filter<Facet> filter) {
        return peer != null ? peer.getFacets(filter) : new Facet[] {};
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

    // //////////////////////////////////////////////////////////
    // Interaction
    // //////////////////////////////////////////////////////////

    @Override
    public ActionArgumentContext createProposedArgumentInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter targetObject,
            final ObjectAdapter[] proposedArguments,
            final int position) {
        return new ActionArgumentContext(getAuthenticationSession(), invocationMethod, targetObject, getIdentifier(),
                proposedArguments, position);
    }

    @Override
    public ObjectAdapter[] getChoices(ObjectAdapter adapter) {
        final List<ObjectAdapter> parameterChoices = new ArrayList<ObjectAdapter>();
        final ActionParameterChoicesFacet choicesFacet = getFacet(ActionParameterChoicesFacet.class);

        if (choicesFacet != null) {
            Object[] choices = choicesFacet.getChoices(parentAction.realTarget(adapter));
            checkChoicesType(getSpecificationLookup(), choices, getSpecification());
            for (Object choice : choices) {
                parameterChoices.add(getAdapterMap().adapterFor(choice));
            }
        }
        if (parameterChoices.size() == 0 && SpecificationFacets.isBoundedSet(getSpecification())) {
            Query query = new QueryFindAllInstances(getSpecification().getFullName());
			final List<ObjectAdapter> allInstancesAdapter = getQuerySubmitter().allMatchingQuery(query);
            for (ObjectAdapter choiceAdapter: allInstancesAdapter) {
                parameterChoices.add(choiceAdapter);
            }
        }
        return parameterChoices.toArray(new ObjectAdapter[0]);
    }

    protected static void checkChoicesType(SpecificationLookup specificationLookup, Object[] objects, ObjectSpecification paramSpec) {
        for (Object object : objects) {
            ObjectSpecification componentSpec = specificationLookup.loadSpecification(object.getClass());
            if (!componentSpec.isOfType(paramSpec)) {
                throw new ModelException("Choice type incompatible with parameter type; expected " + paramSpec.getFullName() + ", but was " + componentSpec.getFullName());
            }
        }
    }
    
    @Override
    public ObjectAdapter getDefault(ObjectAdapter adapter) {
        if (parentAction.isContributed() && adapter != null) {
            if (adapter.getSpecification().isOfType(getSpecification())) {
                return adapter;
            }
        }
        final ActionParameterDefaultsFacet defaultsFacet = getFacet(ActionParameterDefaultsFacet.class);
        if (defaultsFacet != null) {
            Object dflt = defaultsFacet.getDefault(parentAction.realTarget(adapter));
            if (dflt == null) {
            	// it's possible that even though there is a default facet, when invoked it
            	// is unable to return a default.
            	return null;
            }
			return getAdapterMap().adapterFor(dflt);
        }
        return null;
    }
    

    protected AuthenticationSession getAuthenticationSession() {
        return getAuthenticationSessionProvider().getAuthenticationSession();
    }

    // /////////////////////////////////////////////////////////////
    // Dependencies (from parent)
    // /////////////////////////////////////////////////////////////

    protected SpecificationLookup getSpecificationLookup() {
        return parentAction.getSpecificationLookup();
    }

    protected AuthenticationSessionProvider getAuthenticationSessionProvider() {
        return parentAction.getAuthenticationSessionProvider();
    }

    protected AdapterMap getAdapterMap() {
        return parentAction.getAdapterMap();
    }

    protected QuerySubmitter getQuerySubmitter() {
        return parentAction.getQuerySubmitter();
    }

    

}
