/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetAbstract;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetForContributee;
import org.apache.isis.core.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.facets.propcoll.notpersisted.NotPersistedFacetAbstract;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

public class OneToManyAssociationContributee extends OneToManyAssociationDefault implements ContributeeMember2 {

    private final Object servicePojo;
    private final ObjectAction serviceAction;
    

    /**
     * Hold facets rather than delegate to the contributed action (different types might
     * use layout metadata to position the contributee in different ways)
     */
    private final FacetHolder facetHolder = new FacetHolderImpl();
    
    private final Identifier identifier;

    private static ObjectSpecification typeOfSpec(
            final ObjectActionDefault objectAction,
            final ServicesInjector objectMemberDependencies) {

        final TypeOfFacet actionTypeOfFacet = objectAction.getFacet(TypeOfFacet.class);
        final SpecificationLoader specificationLookup = objectMemberDependencies.getSpecificationLoader();
        // TODO: a bit of a hack; ought really to set up a fallback TypeOfFacetDefault which ensures that there is always
        // a TypeOfFacet for any contributee associations created from contributed actions.
        Class<? extends Object> cls = actionTypeOfFacet != null? actionTypeOfFacet.value(): Object.class;
        return specificationLookup.loadSpecification(cls);
    }
    
    public OneToManyAssociationContributee(
            final Object servicePojo,
            final ObjectActionDefault serviceAction,
            final ObjectSpecification contributeeType,
            final ServicesInjector servicesInjector) {
        super(serviceAction.getFacetedMethod(),
                typeOfSpec(serviceAction, servicesInjector),
                servicesInjector);
        this.servicePojo = servicePojo;
        this.serviceAction = serviceAction;

        //
        // ensure the contributed collection cannot be modified, and derive its TypeOfFaccet
        //
        final NotPersistedFacet notPersistedFacet = new NotPersistedFacetAbstract(this) {};
        final DisabledFacet disabledFacet = disabledFacet();
        final TypeOfFacet typeOfFacet = new TypeOfFacetAbstract(getSpecification().getCorrespondingClass(), this, servicesInjector.getSpecificationLoader()) {};

        FacetUtil.addFacet(notPersistedFacet);
        FacetUtil.addFacet(disabledFacet);
        FacetUtil.addFacet(typeOfFacet);


        //
        // in addition, copy over facets from contributed to own.
        //
        // These could include everything under @Collection(...) because the
        // CollectionAnnotationFacetFactory is also run against actions.
        //
        FacetUtil.copyFacets(serviceAction.getFacetedMethod(), facetHolder);


        // calculate the identifier
        final Identifier contributorIdentifier = serviceAction.getFacetedMethod().getIdentifier();
        final String memberName = contributorIdentifier.getMemberName();
        List<String> memberParameterNames = contributorIdentifier.getMemberParameterNames();

        identifier = Identifier.actionIdentifier(contributeeType.getCorrespondingClass().getName(), memberName, memberParameterNames);
    }

    private DisabledFacet disabledFacet() {
        final DisabledFacet originalFacet = facetHolder.getFacet(DisabledFacet.class);
        if( originalFacet != null && 
            originalFacet.when() == When.ALWAYS && 
            originalFacet.where() == Where.ANYWHERE) {
            return originalFacet;
        }
        // ensure that the contributed association is always disabled
        return new DisabledFacetForContributee("Contributed collection", this);
    }

    @Override
    public ObjectAdapter get(final ObjectAdapter ownerAdapter, final InteractionInitiatedBy interactionInitiatedBy) {
        return serviceAction.execute(getServiceAdapter(), null, new ObjectAdapter[]{ownerAdapter}, interactionInitiatedBy);
    }

    @Override
    public ObjectSpecification getOnType() {
        return serviceAction.getOnType();
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public boolean isContributedBy(final ObjectAction serviceAction) {
        return serviceAction == this.serviceAction;
    }

    @Override
    public int getContributeeParamPosition() {
        // always 0 for contributed collections
        return 0;
    }

    @Override
    public Consent isVisible(
            final ObjectAdapter contributee,
            final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        final VisibilityContext<?> ic = ((ObjectMemberAbstract)serviceAction).createVisibleInteractionContext(
                getServiceAdapter(), interactionInitiatedBy, where);
        ic.putContributee(0, contributee); // by definition, the contributee will be the first arg of the service action
        return InteractionUtils.isVisibleResult(this, ic).createConsent();
    }

    @Override
    public Consent isUsable(
            final ObjectAdapter contributee,
            final InteractionInitiatedBy interactionInitiatedBy, final Where where) {
        final ObjectMemberAbstract serviceAction = (ObjectMemberAbstract) this.serviceAction;
        final UsabilityContext<?> ic = serviceAction.createUsableInteractionContext(
                getServiceAdapter(), interactionInitiatedBy, where);
        ic.putContributee(0, contributee); // by definition, the contributee will be the first arg of the service action
        return InteractionUtils.isUsableResult(this, ic).createConsent();
    }

    //region > FacetHolder

    @Override
    protected FacetHolder getFacetHolder() {
        return facetHolder;
    }

    //endregion

    private ObjectAdapter getServiceAdapter() {
        return getPersistenceSessionService().adapterFor(servicePojo);
    }

    //region > ContributeeMember2 impl (getServiceContributedBy)

    @Override
    public ObjectSpecification getServiceContributedBy() {
        return getServiceAdapter().getSpecification();
    }

    //endregion

}
