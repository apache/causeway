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
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetInferred;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetForContributee;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.services.publishing.ExecutionPublisher;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
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
    private final ObjectSpecification mixeeSpec;


    /**
     * Hold facets rather than delegate to the contributed action (different types might
     * use layout metadata to position the contributee in different ways)
     */
    @Getter(onMethod = @__(@Override))
    private final FacetHolder facetHolder = new FacetHolderImpl();

    public OneToOneAssociationMixedIn(
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
                mixinAction.getFacetedMethod(), mixinAction.getReturnType());

        this.mixinType = mixinType;
        this.mixinAction = mixinAction;
        this.mixeeSpec = mixeeSpec;

        //
        // ensure the contributed property cannot be modified
        //
        final DisabledFacet disabledFacet = disabledFacet();

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
            String memberName = determineNameFrom(mixinAction);
            FacetUtil.addFacet(new NamedFacetInferred(memberName, facetHolder));
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

    private ExecutionPublisher getPublisherDispatchService() {
        return getServiceRegistry().lookupServiceElseFail(ExecutionPublisher.class);
    }


}
