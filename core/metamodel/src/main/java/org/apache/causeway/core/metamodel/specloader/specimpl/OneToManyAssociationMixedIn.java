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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Domain;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacetForStaticMemberName;
import org.apache.causeway.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.causeway.core.metamodel.facets.members.disabled.DisabledFacetForContributee;
import org.apache.causeway.core.metamodel.facets.properties.update.SnapshotExcludeFacetFromImmutableMember;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.publishing.ExecutionPublisher;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedInMember;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;

public class OneToManyAssociationMixedIn
extends OneToManyAssociationDefault
implements MixedInMember {

    // -- FACTORIES

    /**
     * JUnit Support
     */
    public static class forTesting {
        public static OneToManyAssociationMixedIn forMixinMain(
                final ObjectSpecification mixeeSpec,
                final ObjectSpecification mixinSpec,
                final String mixinMethodName,
                final FacetedMethod facetedMethod) {
            final ObjectActionDefault mixinAction = (ObjectActionDefault) ObjectActionDefault.forTesting.forMixinMain(facetedMethod);
            return new OneToManyAssociationMixedIn(mixeeSpec, mixinAction, mixinSpec, mixinMethodName);
        }
    }

    // -- CONSTRUCTION

    /**
     * The type of the mixin (providing the action), eg annotated or meta-annotated using
     * {@link org.apache.causeway.applib.annotation.DomainObject} with a {@link DomainObject#nature() nature} of
     * {@link org.apache.causeway.applib.annotation.Nature#MIXIN MIXIN}.
     */
    private final ObjectSpecification mixinSpec;

    /**
     * The {@link ObjectActionDefault} for the action being mixed in (ie on the {@link #mixinType}).
     */
    private final ObjectActionDefault mixinAction;

    /**
     * The domain object type being mixed in to (being supplemented).
     */
    private final ObjectSpecification mixeeSpec;

    /**
     * Hold facets rather than delegate to the mixin action (different types might use layout metadata to position
     * the mixin in different ways)
     */
    @Getter(onMethod = @__(@Override))
    private final FacetHolder facetHolder;

    private static ObjectSpecification typeOfSpec(
            final ObjectActionDefault objectAction) {

        var actionTypeOfFacet = objectAction.getFacet(TypeOfFacet.class);
        // TODO: a bit of a hack; ought really to set up a fallback TypeOfFacetDefault,
        // which ensures that there is always a TypeOfFacet for any mixedIn associations
        // created from mixin actions.
        var type = actionTypeOfFacet != null
                ? actionTypeOfFacet.value().elementType()
                : (Class<?>)Object.class;

        return objectAction.getSpecificationLoader().loadSpecification(type);
    }

    public OneToManyAssociationMixedIn(
            final ObjectSpecification mixeeSpec,
            final ObjectActionDefault mixinAction,
            final ObjectSpecification mixinSpec,
            final String mixinMethodName) {

        super(identifierForMixedInCollection(mixeeSpec, mixinAction),
                mixinAction.getFacetedMethod(), typeOfSpec(mixinAction));

        this.facetHolder = FacetHolder.layered(
                super.getFeatureIdentifier(),
                mixinAction.getFacetedMethod());

        this.mixinSpec = mixinSpec;
        this.mixinAction = mixinAction;
        this.mixeeSpec = mixeeSpec;

        //
        // ensure the mixedIn collection cannot be modified, and infer its TypeOfFaccet
        //
        FacetUtil.addFacet(new SnapshotExcludeFacetFromImmutableMember(this));
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
    protected InteractionHead headFor(final ManagedObject mixedInAdapter) {
        var mixinAdapter = mixinAdapterFor(mixinSpec, mixedInAdapter);
        return InteractionHead.mixin(mixedInAdapter, mixinAdapter);
    }

    private DisabledFacet disabledFacet() {
        final DisabledFacet originalFacet = facetHolder.getFacet(DisabledFacet.class);
        if( originalFacet != null &&
                originalFacet.where().isAlways()) {
            return originalFacet;
        }
        // ensure that the contributed association is always disabled
        return new DisabledFacetForContributee(VetoReason.mixedinCollection(), this);
    }

    @Override
    public ManagedObject get(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return executionPublisher().withPublishingSuppressed(
                () -> mixinAction.executeInternal(
                        headFor(ownerAdapter), Can.empty(), interactionInitiatedBy));
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
        var methodFacade = getFacetedMethod().getMethod();
        return super.isExplicitlyAnnotated() // legacy programming style
                || methodFacade.synthesize(Domain.Include.class).isPresent();
    }

    private ExecutionPublisher executionPublisher() {
        return getServiceRegistry().lookupServiceElseFail(ExecutionPublisher.class);
    }

    private static Identifier identifierForMixedInCollection(
            final ObjectSpecification mixeeSpec,
            final ObjectActionDefault mixinAction) {
        return Identifier.collectionIdentifier(
                LogicalType.eager(
                        mixeeSpec.getCorrespondingClass(),
                        mixeeSpec.getLogicalTypeName()),
                _MixedInMemberNamingStrategy.mixinMemberId(mixinAction));
    }

}
