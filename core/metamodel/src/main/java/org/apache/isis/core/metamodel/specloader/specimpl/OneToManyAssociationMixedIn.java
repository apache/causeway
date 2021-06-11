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

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetAbstract;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetForMemberName;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetForContributee;
import org.apache.isis.core.metamodel.facets.propcoll.memserexcl.SnapshotExcludeFacet;
import org.apache.isis.core.metamodel.facets.propcoll.memserexcl.SnapshotExcludeFacetAbstract;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.services.publishing.ExecutionPublisher;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import lombok.val;

public class OneToManyAssociationMixedIn extends OneToManyAssociationDefault implements MixedInMember {

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
    private final ObjectSpecification mixeeSpec;


    /**
     * Hold facets rather than delegate to the mixin action (different types might use layout metadata to position
     * the mixin in different ways)
     */
    @Getter(onMethod = @__(@Override))
    private final FacetHolder facetHolder = new FacetHolderAbstract() {};

    private static ObjectSpecification typeOfSpec(
            final ObjectActionDefault objectAction) {

        val actionTypeOfFacet = objectAction.getFacet(TypeOfFacet.class);
        // TODO: a bit of a hack; ought really to set up a fallback TypeOfFacetDefault,
        // which ensures that there is always a TypeOfFacet for any mixedIn associations
        // created from mixin actions.
        Class<?> type = actionTypeOfFacet != null
                ? actionTypeOfFacet.value()
                : (Class<?>)Object.class;

        return objectAction.getSpecificationLoader().loadSpecification(type);
    }

    public OneToManyAssociationMixedIn(
            final ObjectActionDefault mixinAction,
            final ObjectSpecification mixeeSpec,
            final Class<?> mixinType,
            final String mixinMethodName) {

        super(Identifier.actionIdentifier(
                    LogicalType.eager(
                            mixeeSpec.getCorrespondingClass(),
                            mixeeSpec.getLogicalTypeName()),
                    determineIdFrom(mixinAction),
                    mixinAction.getFacetedMethod().getIdentifier().getMemberParameterClassNames()),
                mixinAction.getFacetedMethod(), typeOfSpec(mixinAction));

        this.mixinType = mixinType;
        this.mixinAction = mixinAction;
        this.mixeeSpec = mixeeSpec;

        //
        // ensure the mixedIn collection cannot be modified, and derive its TypeOfFaccet
        //
        final SnapshotExcludeFacet snapshotExcludeFacet = new SnapshotExcludeFacetAbstract(this) {};
        final DisabledFacet disabledFacet = disabledFacet();
        final TypeOfFacet typeOfFacet = new TypeOfFacetAbstract(getSpecification().getCorrespondingClass(), this) {};

        FacetUtil.addFacetIfPresent(snapshotExcludeFacet);
        FacetUtil.addFacetIfPresent(disabledFacet);
        FacetUtil.addFacetIfPresent(typeOfFacet);


        //
        // in addition, copy over facets from contributed to own.
        //
        // These could include everything under @Collection(...) because the
        // CollectionAnnotationFacetFactory is also run against actions.
        //
        FacetUtil.copyFacetsTo(mixinAction.getFacetedMethod(), facetHolder);

        // adjust name if necessary
        val isExplicitlyNamed = lookupNonFallbackFacet(NamedFacet.class)
                .isPresent();

        if(!isExplicitlyNamed) {
            String memberName = determineNameFrom(mixinAction);
            FacetUtil.addFacetIfPresent(new NamedFacetForMemberName(memberName, facetHolder));
        }

    }

    @Override
    protected InteractionHead headFor(final ManagedObject mixedInAdapter) {
        val mixinAdapter = mixinAdapterFor(mixinType, mixedInAdapter);
        return InteractionHead.mixin(mixedInAdapter, mixinAdapter);
    }

    private DisabledFacet disabledFacet() {
        final DisabledFacet originalFacet = facetHolder.getFacet(DisabledFacet.class);
        if( originalFacet != null &&
                originalFacet.where().isAlways()) {
            return originalFacet;
        }
        // ensure that the contributed association is always disabled
        return new DisabledFacetForContributee("Contributed collection", this);
    }

    @Override
    public ManagedObject get(
            final ManagedObject ownerAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return getPublishingServiceInternal().withPublishingSuppressed(
                () -> mixinAction.executeInternal(
                        headFor(ownerAdapter), Can.empty(), interactionInitiatedBy));
    }

    @Override
    public ObjectSpecification getOnType() {
        return mixeeSpec;
    }

    @Override
    public ObjectSpecification getMixinType() {
        return getSpecificationLoader().loadSpecification(mixinType);
    }

    @Override
    public boolean hasMixinAction(final ObjectAction mixinAction) {
        return this.mixinAction == mixinAction;
    }

    private ExecutionPublisher getPublishingServiceInternal() {
        return getServiceRegistry().lookupServiceElseFail(ExecutionPublisher.class);
    }


}
