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


package org.apache.isis.metamodel.specloader.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.filters.Filter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.exceptions.ModelException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationFacets;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.facets.MultiTypedFacet;
import org.apache.isis.metamodel.facets.actions.choices.ActionParameterChoicesFacet;
import org.apache.isis.metamodel.facets.actions.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.metamodel.facets.naming.describedas.DescribedAsFacet;
import org.apache.isis.metamodel.facets.naming.named.NamedFacet;
import org.apache.isis.metamodel.facets.propparam.validate.mandatory.MandatoryFacet;
import org.apache.isis.metamodel.interactions.ActionArgumentContext;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.services.container.query.QueryFindAllInstances;
import org.apache.isis.metamodel.specloader.internal.peer.ObjectActionParamPeer;


public abstract class ObjectActionParameterAbstract implements ObjectActionParameter {

    private final int number;
    private final ObjectActionImpl parentAction;
    private final ObjectActionParamPeer peer;

    protected ObjectActionParameterAbstract(
            final int number,
            final ObjectActionImpl objectAction,
            final ObjectActionParamPeer peer) {
        this.number = number;
        this.parentAction = objectAction;
        this.peer = peer;
    }

    /**
     * Subclasses should override either {@link #isObject()} or {@link #isCollection()}.
     */
    public boolean isObject() {
        return false;
    }

    /**
     * Subclasses should override either {@link #isObject()} or {@link #isCollection()}.
     */
    public boolean isCollection() {
        return false;
    }

    /**
     * Parameter number, 0-based.
     */
    public int getNumber() {
        return number;
    }

    public ObjectAction getAction() {
        return parentAction;
    }

    public ObjectSpecification getSpecification() {
        return peer.getSpecification();
    }

    public Identifier getIdentifier() {
        return parentAction.getIdentifier();
    }

    public String getName() {
        final NamedFacet facet = getFacet(NamedFacet.class);
        String name = facet == null ? null : facet.value();
        name = name == null ? peer.getSpecification().getSingularName() : name;
        return name;
    }

    public String getDescription() {
        final DescribedAsFacet facet = getFacet(DescribedAsFacet.class);
        final String description = facet.value();
        return description == null ? "" : description;
    }

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

    public boolean containsFacet(final Class<? extends Facet> facetType) {
        return peer != null ? peer.containsFacet(facetType) : false;
    }

    public <T extends Facet> T getFacet(final Class<T> cls) {
        return peer != null ? peer.getFacet(cls) : null;
    }

    public Class<? extends Facet>[] getFacetTypes() {
        return peer != null ? peer.getFacetTypes() : new Class[] {};
    }

    public Facet[] getFacets(final Filter<Facet> filter) {
        return peer != null ? peer.getFacets(filter) : new Facet[] {};
    }

    public void addFacet(final Facet facet) {
        if (peer != null) {
            peer.addFacet(facet);
        }
    }

    public void addFacet(final MultiTypedFacet facet) {
        if (peer != null) {
            peer.addFacet(facet);
        }
    }

    public void removeFacet(final Facet facet) {
        if (peer != null) {
            peer.removeFacet(facet);
        }
    }

    public void removeFacet(final Class<? extends Facet> facetType) {
        if (peer != null) {
            peer.removeFacet(facetType);
        }
    }

    // //////////////////////////////////////////////////////////
    // Interaction
    // //////////////////////////////////////////////////////////

    public ActionArgumentContext createProposedArgumentInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter targetObject,
            final ObjectAdapter[] proposedArguments,
            final int position) {
        return new ActionArgumentContext(getAuthenticationSession(), invocationMethod, targetObject, getIdentifier(),
                proposedArguments, position);
    }

    public ObjectAdapter[] getChoices(ObjectAdapter adapter) {
        final List<ObjectAdapter> parameterChoices = new ArrayList<ObjectAdapter>();
        final ActionParameterChoicesFacet choicesFacet = getFacet(ActionParameterChoicesFacet.class);

        if (choicesFacet != null) {
            Object[] choices = choicesFacet.getChoices(parentAction.realTarget(adapter));
            checkChoicesType(getRuntimeContext(), choices, getSpecification());
            for (Object choice : choices) {
                parameterChoices.add(getRuntimeContext().adapterFor(choice));
            }
        }
        if (parameterChoices.size() == 0 && SpecificationFacets.isBoundedSet(getSpecification())) {
            QueryFindAllInstances query = new QueryFindAllInstances(getSpecification());
			final List<ObjectAdapter> allInstancesAdapter = getRuntimeContext().allMatchingQuery(query);
            for (ObjectAdapter choiceAdapter: allInstancesAdapter) {
                parameterChoices.add(choiceAdapter);
            }
        }
        return parameterChoices.toArray(new ObjectAdapter[0]);
    }

    protected static void checkChoicesType(RuntimeContext runtimeContext, Object[] objects, ObjectSpecification paramSpec) {
        for (Object object : objects) {
            ObjectSpecification componentSpec = runtimeContext.getSpecificationLoader().loadSpecification(object.getClass());
            if (!componentSpec.isOfType(paramSpec)) {
                throw new ModelException("Choice type incompatible with parameter type; expected " + paramSpec.getFullName() + ", but was " + componentSpec.getFullName());
            }
        }
    }
    
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
			return getRuntimeContext().adapterFor(dflt);
        }
        return null;
    }
    
    
    // /////////////////////////////////////////////////////////////
    // Dependencies (from context)
    // /////////////////////////////////////////////////////////////

    protected RuntimeContext getRuntimeContext() {
        return parentAction.getRuntimeContext();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return getRuntimeContext().getAuthenticationSession();
    }

}
