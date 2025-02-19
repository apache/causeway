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
package org.apache.causeway.core.metamodel.spec.impl;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Domain;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacetForStaticMemberName;
import org.apache.causeway.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.causeway.core.metamodel.facets.members.disabled.DisabledFacetForContributee;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.publishing.ExecutionPublisher;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedInMember;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;

class OneToOneAssociationMixedIn
extends OneToOneAssociationDefault
implements MixedInMember {
    private static final long serialVersionUID = 1L;

    // -- CONSTRUCTION

    /**
     * The type of the mixin (providing the action), eg annotated with {@link org.apache.causeway.applib.annotation.Mixin}.
     */
    private final ObjectSpecification mixinSpec;

    /**
     * The {@link ObjectActionDefault} for the action being mixed in (ie on the {@link #mixinType}.
     */
    private final ObjectActionDefault mixinAction;

    /**
     * The domain object type being mixed in to (being supplemented).
     */
    private final ObjectSpecification mixeeSpec;

    /**
     * Hold facets rather than delegate to the contributed action.
     */
    @Getter(onMethod = @__(@Override))
    private final FacetHolder facetHolder;

    public OneToOneAssociationMixedIn(
            final ObjectSpecification mixeeSpec,
            final ObjectActionDefault mixinAction,
            final ObjectSpecification mixinSpec,
            final String mixinMethodName) {
        super(
                identifierForMixedInProperty(mixeeSpec, mixinAction),
                mixinAction.getFacetedMethod(),
                mixinAction.getReturnType());

        this.facetHolder = FacetHolder.layered(
                super.getFeatureIdentifier(),
                mixinAction.getFacetedMethod());

        this.mixinSpec = mixinSpec;
        this.mixinAction = mixinAction;
        this.mixeeSpec = mixeeSpec;

        //
        // ensure the contributed property cannot be modified
        //
        FacetUtil.addFacet(disabledFacet());

        // adjust name if necessary
        var isExplicitlyNamed = lookupNonFallbackFacet(MemberNamedFacet.class)
                .isPresent();

        if(!isExplicitlyNamed) {
            String memberName = _MixedInMemberNamingStrategy.mixinFriendlyName(mixinAction);
            FacetUtil.addFacet(
                    new MemberNamedFacetForStaticMemberName(memberName, facetHolder));
        }

    }

    @Override
    protected InteractionHead headFor(final ManagedObject mixeeAdapter) {
        return InteractionHead.mixin(
                mixeeAdapter,
                mixinAdapterFor(mixinSpec, mixeeAdapter));
    }

    private DisabledFacet disabledFacet() {
        final DisabledFacet originalFacet = facetHolder.getFacet(DisabledFacet.class);
        if( originalFacet != null &&
                originalFacet.where().isAlways()) {
            return originalFacet;
        }
        // ensure that the contributed association is always disabled
        return new DisabledFacetForContributee(VetoReason.mixedinProperty(), this);
    }

    @Override
    public ManagedObject get(
            final ManagedObject mixedInAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        var head = headFor(mixedInAdapter);

        return executionPublisher().withPublishingSuppressed(
                () -> mixinAction.executeInternal(head, Can.empty(), interactionInitiatedBy)
        );
    }

    @Override
    public ObjectSpecification getDeclaringType() {
        return mixeeSpec;
    }

    @Override
    public ObjectSpecification getMixinType() {
        return mixinSpec;
    }

    @Override
    public boolean hasMixinAction(final ObjectAction mixinAction) {
        return this.mixinAction == mixinAction;
    }

    @Getter(lazy=true, onMethod_ = {@Override})
    private final boolean explicitlyAnnotated = calculateIsExplicitlyAnnotated();

    // -- HELPER

    private boolean calculateIsExplicitlyAnnotated() {
        var methodFacade = getFacetedMethod().methodFacade();
        return super.isExplicitlyAnnotated() // legacy programming style
                || methodFacade.synthesize(Domain.Include.class).isPresent();
    }

    private ExecutionPublisher executionPublisher() {
        return getServiceRegistry().lookupServiceElseFail(ExecutionPublisher.class);
    }

    private static Identifier identifierForMixedInProperty(
            final ObjectSpecification mixeeSpec,
            final ObjectActionDefault mixinAction) {
        return Identifier.propertyIdentifier(
                    LogicalType.eager(
                            mixeeSpec.getCorrespondingClass(),
                            mixeeSpec.logicalTypeName()),
                    _MixedInMemberNamingStrategy.mixinMemberId(mixinAction));
    }

}
