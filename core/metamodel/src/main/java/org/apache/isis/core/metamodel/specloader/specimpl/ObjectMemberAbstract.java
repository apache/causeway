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
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.runtimecontext.PersistenceSessionService;
import org.apache.isis.core.metamodel.runtimecontext.MessageBrokerService;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MultiTypedFacet;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.help.HelpFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.interactions.AccessContext;
import org.apache.isis.core.metamodel.interactions.DisablingInteractionAdvisor;
import org.apache.isis.core.metamodel.interactions.HidingInteractionAdvisor;
import org.apache.isis.core.metamodel.interactions.InteractionContext;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberDependencies;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;

public abstract class ObjectMemberAbstract implements ObjectMember {

    private final PersistenceSessionService persistenceSessionService;

    public static ObjectSpecification getSpecification(final SpecificationLoader specificationLookup, final Class<?> type) {
        return type == null ? null : specificationLookup.loadSpecification(type);
    }

    private final CollectionTypeRegistry collectionTypeRegistry = new CollectionTypeRegistry();

    protected final String defaultName;
    private final String id;
    private final FacetedMethod facetedMethod;
    private final FeatureType featureType;
    private final SpecificationLoader specificationLookup;
    private final AdapterManager adapterManager;
    private final ServicesInjector servicesInjector;
    private final MessageBrokerService messageBrokerService;

    protected ObjectMemberAbstract(
            final FacetedMethod facetedMethod,
            final FeatureType featureType,
            final ObjectMemberDependencies objectMemberDependencies) {
        final String id = facetedMethod.getIdentifier().getMemberName();
        if (id == null) {
            throw new IllegalArgumentException("Name must always be set");
        }
        this.facetedMethod = facetedMethod;
        this.featureType = featureType;
        this.id = id;
        this.defaultName = StringExtensions.asNaturalName2(this.id);

        this.specificationLookup = objectMemberDependencies.getSpecificationLoader();
        this.adapterManager = objectMemberDependencies.getAdapterManager();
        this.servicesInjector = objectMemberDependencies.getServicesInjector();
        this.messageBrokerService = objectMemberDependencies.getMessageBrokerService();
        this.persistenceSessionService = objectMemberDependencies.getPersistenceSessionService();
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

    /**
     * Create an {@link InteractionContext} to represent an attempt to view this
     * member (that is, to check if it is visible or not).
     *
     * <p>
     * Typically it is easier to just call
     * {@link ObjectMember#isVisible(ObjectAdapter, InteractionInitiatedBy, Where)}; this is
     * provided as API for symmetry with interactions (such as
     * {@link AccessContext} accesses) have no corresponding vetoing methods.
     */
     protected abstract VisibilityContext<?> createVisibleInteractionContext(
             final ObjectAdapter targetObjectAdapter,
             final InteractionInitiatedBy interactionInitiatedBy,
             final Where where);



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
     */
    @Override
    public Consent isVisible(
            final ObjectAdapter target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        return isVisibleResult(target, interactionInitiatedBy, where).createConsent();
    }

    private InteractionResult isVisibleResult(
            final ObjectAdapter target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        final VisibilityContext<?> ic = createVisibleInteractionContext(target, interactionInitiatedBy, where);
        return InteractionUtils.isVisibleResult(this, ic);
    }

    // /////////////////////////////////////////////////////////////
    // Disabled (or enabled)
    // /////////////////////////////////////////////////////////////

    /**
     * Create an {@link InteractionContext} to represent an attempt to
     * use this member (that is, to check if it is usable or not).
     *
     * <p>
     * Typically it is easier to just call
     * {@link ObjectMember#isUsable(ObjectAdapter, InteractionInitiatedBy, Where)}; this is
     * provided as API for symmetry with interactions (such as
     * {@link AccessContext} accesses) have no corresponding vetoing methods.
     */
    protected abstract UsabilityContext<?> createUsableInteractionContext(
            final ObjectAdapter target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where);

    /**
     * Loops over all {@link DisablingInteractionAdvisor} {@link Facet}s and
     * returns <tt>true</tt> only if none disables the member.
     */
    @Override
    public Consent isUsable(
            final ObjectAdapter target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        return isUsableResult(target, interactionInitiatedBy, where).createConsent();
    }

    private InteractionResult isUsableResult(
            final ObjectAdapter target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        final UsabilityContext<?> ic = createUsableInteractionContext(target, interactionInitiatedBy, where);
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
    // toString
    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return String.format("id=%s,name='%s'", getId(), getName());
    }

    // //////////////////////////////////////////////////////////////////
    // Dependencies
    // //////////////////////////////////////////////////////////////////


    public SpecificationLoader getSpecificationLoader() {
        return specificationLookup;
    }

    public AdapterManager getAdapterManager() {
        return adapterManager;
    }

    public ServicesInjector getServicesInjector() {
        return servicesInjector;
    }

    public MessageBrokerService getMessageBrokerService() {
        return messageBrokerService;
    }

    public PersistenceSessionService getPersistenceSessionService() {
        return persistenceSessionService;
    }

    public CollectionTypeRegistry getCollectionTypeRegistry() {
        return collectionTypeRegistry;
    }
}
