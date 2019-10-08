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
package org.apache.isis.metamodel.specloader.specimpl;

import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facets.all.named.NamedFacetInferred;
import org.apache.isis.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.metamodel.facets.members.disabled.DisabledFacetForContributee;
import org.apache.isis.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;
import org.apache.isis.metamodel.facets.propcoll.notpersisted.NotPersistedFacetAbstract;
import org.apache.isis.metamodel.interactions.InteractionUtils;
import org.apache.isis.metamodel.interactions.UsabilityContext;
import org.apache.isis.metamodel.interactions.VisibilityContext;
import org.apache.isis.metamodel.services.publishing.PublishingServiceInternal;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;

import lombok.val;

public class OneToOneAssociationMixedIn extends OneToOneAssociationDefault implements MixedInMember {

    /**
     * The type of the mixin (providing the action), eg annotated with {@link org.apache.isis.applib.annotation.Mixin}.
     */
    private final Class<?> mixinType;

    /**
     * The {@link ObjectActionDefault} for the action being mixed in (ie on the {@link #mixinType}.
     */
    private final ObjectActionDefault mixinAction;

    /**
     * The domain object type being mixed in to (being supplemented).
     */
    private final ObjectSpecification mixedInType;


    /**
     * Hold facets rather than delegate to the contributed action (different types might
     * use layout metadata to position the contributee in different ways)
     */
    private final FacetHolder facetHolder = new FacetHolderImpl();

    private final Identifier identifier;

    public OneToOneAssociationMixedIn(
            final ObjectActionDefault mixinAction,
            final ObjectSpecification mixedInType,
            final Class<?> mixinType,
            final String mixinMethodName) {

        super(mixinAction.getFacetedMethod(), mixinAction.getReturnType());

        this.mixinType = mixinType;
        this.mixinAction = mixinAction;
        this.mixedInType = mixedInType;

        //
        // ensure the contributed property cannot be modified
        //
        final NotPersistedFacet notPersistedFacet = new NotPersistedFacetAbstract(this) {};
        final DisabledFacet disabledFacet = disabledFacet();

        FacetUtil.addFacet(notPersistedFacet);
        FacetUtil.addFacet(disabledFacet);

        //
        // in addition, copy over facets from contributed to own.
        //
        // These could include everything under @Property(...) because the
        // PropertyAnnotationFacetFactory is also run against actions.
        //
        
        FacetUtil.copyFacets(mixinAction.getFacetedMethod(), facetHolder);
        

        // adjust name if necessary
        final String name = getName();

        if(_Strings.isNullOrEmpty(name) || name.equalsIgnoreCase(mixinMethodName)) {
            String memberName = ObjectActionMixedIn.determineNameFrom(mixinAction);
            FacetUtil.addFacet(new NamedFacetInferred(memberName, facetHolder));
        }


        // calculate the identifier
        final Identifier mixinIdentifier = mixinAction.getFacetedMethod().getIdentifier();
        List<String> memberParameterNames = mixinIdentifier.getMemberParameterNames();

        identifier = Identifier.actionIdentifier(mixedInType.getCorrespondingClass().getName(), getId(), memberParameterNames);
    }

    private DisabledFacet disabledFacet() {
        final DisabledFacet originalFacet = facetHolder.getFacet(DisabledFacet.class);
        if( originalFacet != null &&
                originalFacet.where() == Where.ANYWHERE) {
            return originalFacet;
        }
        // ensure that the contributed association is always disabled
        return new DisabledFacetForContributee("Contributed property", this);
    }

    @Override
    public ManagedObject get(
            final ManagedObject mixedInAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val mixinAdapter = mixinAdapterFor(mixinType, mixedInAdapter);

        return getPublishingServiceInternal().withPublishingSuppressed(
                new PublishingServiceInternal.Block<ManagedObject>(){
                    @Override
                    public ManagedObject exec() {
                        return mixinAction.executeInternal(
                                mixinAdapter, mixedInAdapter, new ManagedObject[0], interactionInitiatedBy);
                    }
                }
                );
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public String getId() {
        return determineIdFrom(this.mixinAction);
    }

    @Override
    public String getOriginalId() {
        return super.getId();
    }

    @Override
    public Consent isVisible(
            final ManagedObject mixedInAdapter,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {

        val mixinAdapter = mixinAdapterFor(mixinType, mixedInAdapter);
        final VisibilityContext<?> ic =
                mixinAction.createVisibleInteractionContext(mixinAdapter, interactionInitiatedBy, where);
        ic.setMixedIn(mixedInAdapter);
        return InteractionUtils.isVisibleResult(this, ic).createConsent();
    }

    @Override
    public Consent isUsable(
            final ManagedObject mixedInAdapter,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {

        val mixinAdapter = mixinAdapterFor(mixinType, mixedInAdapter);
        final UsabilityContext<?> ic =
                mixinAction.createUsableInteractionContext(mixinAdapter, interactionInitiatedBy, where);
        ic.setMixedIn(mixedInAdapter);
        return InteractionUtils.isUsableResult(this, ic).createConsent();
    }

    @Override
    protected FacetHolder getFacetHolder() {
        return facetHolder;
    }

    @Override
    public ObjectSpecification getOnType() {
        return mixedInType;
    }

    @Override
    public ObjectSpecification getMixinType() {
        return getSpecificationLoader().loadSpecification(mixinType);
    }

    private PublishingServiceInternal getPublishingServiceInternal() {
        return getServiceRegistry().lookupServiceElseFail(PublishingServiceInternal.class);
    }


}
