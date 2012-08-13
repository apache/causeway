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
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.QuerySubmitter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.MultiTypedFacet;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.object.bounded.BoundedFacetUtils;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.interactions.ActionArgumentContext;
import org.apache.isis.core.metamodel.spec.DomainModelException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

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
            return StringUtils.camelLowerFirst(facet.value());
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
            return StringUtils.camelLowerFirst(name);
        }
        final int indexOf = parameters.indexOf(this);
        return StringUtils.camelLowerFirst(name + (indexOf + 1));
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

    // //////////////////////////////////////////////////////////
    // Interaction
    // //////////////////////////////////////////////////////////

    @Override
    public ActionArgumentContext createProposedArgumentInteractionContext(final AuthenticationSession session, final InteractionInvocationMethod invocationMethod, final ObjectAdapter targetObject, final ObjectAdapter[] proposedArguments, final int position) {
        return new ActionArgumentContext(getAuthenticationSession(), invocationMethod, targetObject, getIdentifier(), proposedArguments, position);
    }

    @Override
    public ObjectAdapter[] getChoices(final ObjectAdapter adapter) {
        final List<ObjectAdapter> parameterChoices = Lists.newArrayList();
        final ActionParameterChoicesFacet choicesFacet = getFacet(ActionParameterChoicesFacet.class);

        if (choicesFacet != null) {
            final Object[] choices = choicesFacet.getChoices(parentAction.realTarget(adapter));
            checkChoicesType(getSpecificationLookup(), choices, getSpecification());
            for (final Object choice : choices) {
                parameterChoices.add(getAdapterMap().adapterFor(choice));
            }
        }
        if (parameterChoices.size() == 0 && BoundedFacetUtils.isBoundedSet(getSpecification())) {
            addParameterChoicesForBounded(parameterChoices);
        }
        return parameterChoices.toArray(new ObjectAdapter[0]);
    }

    private <T> void addParameterChoicesForBounded(final List<ObjectAdapter> parameterChoices) {
        final Query<T> query = new QueryFindAllInstances<T>(getSpecification().getFullIdentifier());
        final List<ObjectAdapter> allInstancesAdapter = getQuerySubmitter().allMatchingQuery(query);
        for (final ObjectAdapter choiceAdapter : allInstancesAdapter) {
            parameterChoices.add(choiceAdapter);
        }
    }

    protected static void checkChoicesType(final SpecificationLoader specificationLookup, final Object[] objects, final ObjectSpecification paramSpec) {
        for (final Object object : objects) {
            final ObjectSpecification componentSpec = specificationLookup.loadSpecification(object.getClass());
            if (!componentSpec.isOfType(paramSpec)) {
                throw new DomainModelException("Choice type incompatible with parameter type; expected " + paramSpec.getFullIdentifier() + ", but was " + componentSpec.getFullIdentifier());
            }
        }
    }

    @Override
    public ObjectAdapter getDefault(final ObjectAdapter adapter) {
        if (parentAction.isContributed() && adapter != null) {
            if (adapter.getSpecification().isOfType(getSpecification())) {
                return adapter;
            }
        }
        final ActionParameterDefaultsFacet defaultsFacet = getFacet(ActionParameterDefaultsFacet.class);
        if (defaultsFacet != null) {
            final Object dflt = defaultsFacet.getDefault(parentAction.realTarget(adapter));
            if (dflt == null) {
                // it's possible that even though there is a default facet, when
                // invoked it
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

}
