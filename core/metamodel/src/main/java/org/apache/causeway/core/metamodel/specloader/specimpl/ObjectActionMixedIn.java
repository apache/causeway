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
import org.apache.causeway.commons.collections.CanVector;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacetForStaticMemberName;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedInMember;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class ObjectActionMixedIn
extends ObjectActionDefault
implements MixedInMember {

    /**
     * The type of the mixin (providing the action), eg annotated with @{@link DomainObject DomainObject}#{@link DomainObject#nature() nature} of {@link org.apache.causeway.applib.annotation.Nature#MIXIN MIXIN}.
     */
    private final Class<?> mixinType;

    /**
     * The {@link ObjectActionDefault} for the action being mixed in (ie on the {@link #mixinType}.
     */
    final ObjectActionDefault mixinAction;

    /**
     * The domain object type being mixed in to (being supplemented).
     */
    private final ObjectSpecification mixeeSpec;

    /**
     * Hold facets rather than delegate to the mixin action
     */
    @Getter(onMethod = @__(@Override))
    private final FacetHolder facetHolder;

    public ObjectActionMixedIn(
            final Class<?> mixinType,
            final String mixinMethodName,
            final ObjectActionDefault mixinAction,
            final ObjectSpecification mixeeSpec) {

        super(Identifier.actionIdentifier(
                    LogicalType.eager(
                            mixeeSpec.getCorrespondingClass(),
                            mixeeSpec.getLogicalTypeName()),
                    _MixedInMemberNamingStrategy.determineIdFrom(mixinAction),
                    mixinAction.getFacetedMethod().getFeatureIdentifier().getMemberParameterClassNames()),
                mixinAction.getFacetedMethod(), false);

        this.facetHolder = FacetHolder.layered(
                super.getFeatureIdentifier(),
                mixinAction.getFacetedMethod());
        this.mixinType = mixinType;
        this.mixinAction = mixinAction;
        this.mixeeSpec = mixeeSpec;

        // adjust name if necessary

        val isExplicitlyNamed = lookupNonFallbackFacet(MemberNamedFacet.class)
                .isPresent();

        if(!isExplicitlyNamed) {
            val memberName = _MixedInMemberNamingStrategy.determineNameFrom(mixinAction);
            this.addFacet(
                    new MemberNamedFacetForStaticMemberName(memberName, facetHolder));
        }
    }

    @Override
    protected InteractionHead headFor(final ManagedObject owner) {
        return InteractionHead.mixin(
                owner,
                mixinAdapterFor(mixinType, owner));
    }

    @Override
    public boolean hasMixinAction(final ObjectAction mixinAction) {
        return this.mixinAction == mixinAction;
    }

    @Override
    public ObjectSpecification getDeclaringType() {
        return mixeeSpec;
    }

    @Override
    public int getParameterCount() {
        return mixinAction.getParameterCount();
    }

    @Override
    public ManagedObject realTargetAdapter(final ManagedObject targetAdapter) {
        return mixinAdapterFor(targetAdapter);
    }

    @Override
    public ActionInteractionHead interactionHead(final @NonNull ManagedObject actionOwner) {
        return ActionInteractionHead.of(this, actionOwner, mixinAdapterFor(actionOwner));
    }

    @Override
    public CanVector<ManagedObject> getChoices(
            final ManagedObject mixedInAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final ManagedObject mixinAdapter = mixinAdapterFor(mixedInAdapter);
        return mixinAction.getChoices(mixinAdapter, interactionInitiatedBy);
    }

    protected ManagedObject mixinAdapterFor(final ManagedObject mixeeAdapter) {
        return mixinAdapterFor(mixinType, mixeeAdapter);
    }

    @Override
    public ManagedObject execute(
            final InteractionHead head,
            final Can<ManagedObject> arguments,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final ManagedObject owner = head.getOwner();
        final ManagedObject target = mixinAdapterFor(mixinType, owner);
        _Assert.assertEquals(target.getSpecification(), head.getTarget().getSpecification(),
                "head has the wrong target (should be a mixed-in adapter, but is the mixee adapter)");

        setupCommand(head, arguments);
        return mixinAction.executeInternal(
                head, arguments,
                interactionInitiatedBy);
    }

    @Override
    public ObjectSpecification getMixinType() {
        return getSpecificationLoader().loadSpecification(mixinType);
    }

    @Getter(lazy=true, onMethod_ = {@Override})
    private final boolean explicitlyAnnotated = calculateIsExplicitlyAnnotated();

    // -- HELPER

    private boolean calculateIsExplicitlyAnnotated() {
        val javaMethod = getFacetedMethod().getMethod();
        return super.isExplicitlyAnnotated() // legacy programming style
                || _Annotations.synthesize(javaMethod, Domain.Include.class).isPresent();
    }

}
