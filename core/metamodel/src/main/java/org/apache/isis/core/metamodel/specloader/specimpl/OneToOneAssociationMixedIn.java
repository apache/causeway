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
import org.apache.isis.applib.annotation.Domain;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacetForStaticMemberName;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetForContributee;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.services.publishing.ExecutionPublisher;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import lombok.val;

public class OneToOneAssociationMixedIn
extends OneToOneAssociationDefault
implements MixedInMember {

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
     * Hold facets rather than delegate to the contributed action (different types might
     * use layout metadata to position the contributee in different ways)
     */
    @Getter(onMethod = @__(@Override))
    private final FacetHolder facetHolder;

    public OneToOneAssociationMixedIn(
            final ObjectActionDefault mixinAction,
            final ObjectSpecification mixeeSpec,
            final Class<?> mixinType,
            final String mixinMethodName) {

        super(Identifier.propertyOrCollectionIdentifier(
                    LogicalType.eager(
                            mixeeSpec.getCorrespondingClass(),
                            mixeeSpec.getLogicalTypeName()),
                    _MixedInMemberNamingStrategy.determineIdFrom(mixinAction)),
                mixinAction.getFacetedMethod(), mixinAction.getReturnType());

        this.facetHolder = FacetHolderAbstract.simple(
                mixeeSpec.getMetaModelContext(),
                super.getFeatureIdentifier());

        this.mixinType = mixinType;
        this.mixinAction = mixinAction;
        this.mixeeSpec = mixeeSpec;

        //
        // ensure the contributed property cannot be modified
        //
        FacetUtil.addFacet(disabledFacet());

        //
        // in addition, copy over facets from contributed to own.
        //
        // These could include everything under @Property(...) because the
        // PropertyAnnotationFacetFactory is also run against actions.
        //

        FacetUtil.copyFacetsTo(mixinAction.getFacetedMethod(), facetHolder);

        // adjust name if necessary
        val isExplicitlyNamed = lookupNonFallbackFacet(MemberNamedFacet.class)
                .isPresent();

        if(!isExplicitlyNamed) {
            String memberName = _MixedInMemberNamingStrategy.determineNameFrom(mixinAction);
            FacetUtil.addFacet(
                    new MemberNamedFacetForStaticMemberName(memberName, facetHolder));
        }

    }

    @Override
    protected InteractionHead headFor(final ManagedObject mixeeAdapter) {
        return InteractionHead.mixin(
                mixeeAdapter,
                mixinAdapterFor(mixinType, mixeeAdapter));
    }

    private DisabledFacet disabledFacet() {
        final DisabledFacet originalFacet = facetHolder.getFacet(DisabledFacet.class);
        if( originalFacet != null &&
                originalFacet.where().isAlways()) {
            return originalFacet;
        }
        // ensure that the contributed association is always disabled
        return new DisabledFacetForContributee("Contributed property", this);
    }

    @Override
    public ManagedObject get(
            final ManagedObject mixedInAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val head = headFor(mixedInAdapter);

        return getPublisherDispatchService().withPublishingSuppressed(
                () -> mixinAction.executeInternal(head, Can.empty(), interactionInitiatedBy)
        );
    }

    @Override
    public ObjectSpecification getDeclaringType() {
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

    @Getter(lazy=true, onMethod_ = {@Override})
    private final boolean explicitlyAnnotated = calculateIsExplicitlyAnnotated();

    // -- HELPER

    private boolean calculateIsExplicitlyAnnotated() {
        val javaMethod = getFacetedMethod().getMethod();
        return super.isExplicitlyAnnotated() // legacy programming style
                || _Annotations.synthesizeInherited(javaMethod, Domain.Include.class).isPresent();
    }

    private ExecutionPublisher getPublisherDispatchService() {
        return getServiceRegistry().lookupServiceElseFail(ExecutionPublisher.class);
    }


}
