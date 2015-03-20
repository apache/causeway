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
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.QuerySubmitter;
import org.apache.isis.core.metamodel.adapter.ServicesProvider;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MultiTypedFacet;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.help.HelpFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.interactions.*;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberContext;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;

public abstract class ObjectMemberAbstract implements ObjectMember {

    public static ObjectSpecification getSpecification(final SpecificationLoader specificationLookup, final Class<?> type) {
        return type == null ? null : specificationLookup.loadSpecification(type);
    }

    private final CollectionTypeRegistry collectionTypeRegistry = new CollectionTypeRegistry();

    protected final String defaultName;
    private final String id;
    private final FacetedMethod facetedMethod;
    private final FeatureType featureType;
    private final AuthenticationSessionProvider authenticationSessionProvider;
    private final SpecificationLoader specificationLookup;
    private final AdapterManager adapterManager;
    private final ServicesProvider servicesProvider;
    private final QuerySubmitter querySubmitter;
    private final DeploymentCategory deploymentCategory;

    protected ObjectMemberAbstract(final FacetedMethod facetedMethod, final FeatureType featureType, final ObjectMemberContext objectMemberContext) {
        final String id = facetedMethod.getIdentifier().getMemberName();
        if (id == null) {
            throw new IllegalArgumentException("Name must always be set");
        }
        this.facetedMethod = facetedMethod;
        this.featureType = featureType;
        this.id = id;
        this.defaultName = StringExtensions.asNaturalName2(this.id);

        this.deploymentCategory = objectMemberContext.getDeploymentCategory();
        this.authenticationSessionProvider = objectMemberContext.getAuthenticationSessionProvider();
        this.specificationLookup = objectMemberContext.getSpecificationLookup();
        this.adapterManager = objectMemberContext.getAdapterManager();
        this.servicesProvider = objectMemberContext.getServicesProvider();
        this.querySubmitter = objectMemberContext.getQuerySubmitter();
    }

    // /////////////////////////////////////////////////////////////
    // from context
    // /////////////////////////////////////////////////////////////

    public DeploymentCategory getDeploymentCategory() {
        return deploymentCategory;
    }
    
    // /////////////////////////////////////////////////////////////
    // Identifiers
    // /////////////////////////////////////////////////////////////

    @Override
    public String getId() {
        return id;
    }

    /**
     * @return the facetedMethod
     */
    public FacetedMethod getFacetedMethod() {
        return facetedMethod;
    }

    @Override
    public Identifier getIdentifier() {
        return getFacetedMethod().getIdentifier();
    }

    @Override
    public FeatureType getFeatureType() {
        return featureType;
    }

    // /////////////////////////////////////////////////////////////
    // Facets
    // /////////////////////////////////////////////////////////////

    @Override
    public boolean containsFacet(final Class<? extends Facet> facetType) {
        return getFacetedMethod().containsFacet(facetType);
    }

    @Override
    public boolean containsDoOpFacet(final Class<? extends Facet> facetType) {
        return getFacetedMethod().containsDoOpFacet(facetType);
    }

    @Override
    public <T extends Facet> T getFacet(final Class<T> cls) {
        return getFacetedMethod().getFacet(cls);
    }

    @Override
    public Class<? extends Facet>[] getFacetTypes() {
        return getFacetedMethod().getFacetTypes();
    }

    @Override
    public List<Facet> getFacets(final Filter<Facet> filter) {
        return getFacetedMethod().getFacets(filter);
    }

    @Override
    public void addFacet(final Facet facet) {
        getFacetedMethod().addFacet(facet);
    }

    @Override
    public void addFacet(final MultiTypedFacet facet) {
        getFacetedMethod().addFacet(facet);
    }

    @Override
    public void removeFacet(final Facet facet) {
        getFacetedMethod().removeFacet(facet);
    }

    @Override
    public void removeFacet(final Class<? extends Facet> facetType) {
        getFacetedMethod().removeFacet(facetType);
    }

    // /////////////////////////////////////////////////////////////
    // Name, Description, Help (convenience for facets)
    // /////////////////////////////////////////////////////////////

    /**
     * Return the default label for this member. This is based on the name of
     * this member.
     * 
     * @see #getId()
     */
    @Override
    public String getName() {
        final NamedFacet facet = getFacet(NamedFacet.class);
        final String name = facet.value();
        if (name != null) {
            return name;
        }
        else {
            // this should now be redundant, see NamedFacetDefault
            return defaultName;
        }
    }

    @Override
    public String getDescription() {
        final DescribedAsFacet facet = getFacet(DescribedAsFacet.class);
        return facet.value();
    }

    @Override
    public String getHelp() {
        final HelpFacet facet = getFacet(HelpFacet.class);
        return facet.value();
    }

    // /////////////////////////////////////////////////////////////
    // Hidden (or visible)
    // /////////////////////////////////////////////////////////////

    @Override
    public boolean isAlwaysHidden() {
        final HiddenFacet facet = getFacet(HiddenFacet.class);
        return facet != null &&
                !facet.isNoop() &&
                facet.when() == When.ALWAYS &&
                (facet.where() == Where.EVERYWHERE || facet.where() == Where.ANYWHERE)
                ;

    }

    /**
     * Loops over all {@link HidingInteractionAdvisor} {@link Facet}s and
     * returns <tt>true</tt> only if none hide the member.
     * 
     * <p>
     * TODO: currently this method is hard-coded to assume all interactions are
     * initiated {@link InteractionInvocationMethod#BY_USER by user}.
     */
    @Override
    public Consent isVisible(final AuthenticationSession session, final ObjectAdapter target, Where where) {
        return isVisibleResult(session, target, where).createConsent();
    }

    private InteractionResult isVisibleResult(final AuthenticationSession session, final ObjectAdapter target, Where where) {
        final VisibilityContext<?> ic = createVisibleInteractionContext(session, InteractionInvocationMethod.BY_USER, target, where);
        return InteractionUtils.isVisibleResult(this, ic);
    }

    // /////////////////////////////////////////////////////////////
    // Disabled (or enabled)
    // /////////////////////////////////////////////////////////////

    /**
     * Loops over all {@link DisablingInteractionAdvisor} {@link Facet}s and
     * returns <tt>true</tt> only if none disables the member.
     * 
     * <p>
     * TODO: currently this method is hard-coded to assume all interactions are
     * initiated {@link InteractionInvocationMethod#BY_USER by user}.
     */
    @Override
    public Consent isUsable(final AuthenticationSession session, final ObjectAdapter target, Where where) {
        return isUsableResult(session, target, where).createConsent();
    }

    private InteractionResult isUsableResult(final AuthenticationSession session, final ObjectAdapter target, Where where) {
        final UsabilityContext<?> ic = createUsableInteractionContext(session, InteractionInvocationMethod.BY_USER, target, where);
        return InteractionUtils.isUsableResult(this, ic);
    }

    // //////////////////////////////////////////////////////////////////
    // isAssociation, isAction
    // //////////////////////////////////////////////////////////////////

    @Override
    public boolean isAction() {
        return featureType.isAction();
    }

    @Override
    public boolean isPropertyOrCollection() {
        return featureType.isPropertyOrCollection();
    }

    @Override
    public boolean isOneToManyAssociation() {
        return featureType.isCollection();
    }

    @Override
    public boolean isOneToOneAssociation() {
        return featureType.isProperty();
    }

    // //////////////////////////////////////////////////////////////////
    // Convenience
    // //////////////////////////////////////////////////////////////////

    /**
     * The current {@link AuthenticationSession} (can change over time so do not
     * cache).
     */
    protected AuthenticationSession getAuthenticationSession() {
        return authenticationSessionProvider.getAuthenticationSession();
    }

    // //////////////////////////////////////////////////////////////////
    // toString
    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return String.format("id=%s,name='%s'", getId(), getName());
    }

    // //////////////////////////////////////////////////////////////////
    // Dependencies
    // //////////////////////////////////////////////////////////////////

    public AuthenticationSessionProvider getAuthenticationSessionProvider() {
        return authenticationSessionProvider;
    }

    public SpecificationLoader getSpecificationLookup() {
        return specificationLookup;
    }

    public AdapterManager getAdapterManager() {
        return adapterManager;
    }

    public ServicesProvider getServicesProvider() {
        return servicesProvider;
    }

    public QuerySubmitter getQuerySubmitter() {
        return querySubmitter;
    }

    public CollectionTypeRegistry getCollectionTypeRegistry() {
        return collectionTypeRegistry;
    }
}
