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
package org.apache.causeway.core.metamodel.postprocessors.members;

import javax.inject.Inject;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForAction;
import org.apache.causeway.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacet;
import org.apache.causeway.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacet;
import org.apache.causeway.core.metamodel.postprocessors.MetaModelPostProcessorAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

import lombok.val;

/**
 * Mixed-in members use the domain-event type as specified with the mixee type,
 * <pre>@DomainObject()</pre>
 * unless overwritten by the mixin type.
 */
public class SynthesizeDomainEventsForMixinPostProcessor
        extends MetaModelPostProcessorAbstract {

    @Inject
    public SynthesizeDomainEventsForMixinPostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    public void postProcessAction(final ObjectSpecification objectSpecification, final ObjectAction objectAction) {
        if (objectAction.isMixedIn()) {
            objectAction
                    .lookupFacet(ActionDomainEventFacet.class)
                    .ifPresentOrElse(
                            facet -> initActionWithMixee(objectSpecification, objectAction, facet),
                            () -> reportMissing(objectAction));
        }
    }

    @Override
    public void postProcessProperty(final ObjectSpecification objectSpecification, final OneToOneAssociation property) {
        if (property.isMixedIn()) {
            property
                    .lookupFacet(PropertyDomainEventFacet.class)
                    .ifPresentOrElse(
                            facet -> initPropertyWithMixee(objectSpecification, property, facet),
                            () -> reportMissing(property));
        }
    }

    @Override
    public void postProcessCollection(final ObjectSpecification objectSpecification, final OneToManyAssociation collection) {
        if (collection.isMixedIn()) {
            collection
                    .lookupFacet(CollectionDomainEventFacet.class)
                    .ifPresentOrElse(
                            facet -> initCollectionWithMixee(objectSpecification, collection, facet),
                            () -> reportMissing(collection));
        }
    }

    // -- HELPER

    private void initActionWithMixee(
            final ObjectSpecification objectSpecification,
            final ObjectAction objectAction,
            final ActionDomainEventFacet actionDomainEventFacet) {
        if (!actionDomainEventFacet.getEventTypeOrigin().isDefault()) {
            return; // skip if already set explicitly on the action or mixin type
        }

        ActionDomainEventFacet
                .createObjectTypeSpecificForMixin(objectSpecification, objectAction.getFacetHolder())
                .ifPresent(mixeeSpecificActionDomainEventFacet -> {
                    objectAction.addFacet(mixeeSpecificActionDomainEventFacet);
                    installMixeeSpecificActionInvocationFacet(objectAction, mixeeSpecificActionDomainEventFacet);
                });
    }

    private void installMixeeSpecificActionInvocationFacet(
            final ObjectAction objectAction,
            final ActionDomainEventFacet mixeeSpecificActionDomainEventFacet) {
        val actionInvocationFacet = objectAction.getFacet(ActionInvocationFacet.class);
        if (!(actionInvocationFacet instanceof ActionInvocationFacetForAction)) {
            return;
        }
        val actionInvocationFacetForAction = (ActionInvocationFacetForAction) actionInvocationFacet;
        objectAction.addFacet(ActionInvocationFacetForAction.createObjectTypeSpecific(
                mixeeSpecificActionDomainEventFacet,
                actionInvocationFacetForAction.getMethods().getFirstElseFail(),
                actionInvocationFacetForAction.getDeclaringType(),
                actionInvocationFacetForAction.getReturnType(),
                objectAction.getFacetHolder()));
    }

    private void initPropertyWithMixee(
            final ObjectSpecification objectSpecification,
            final OneToOneAssociation property,
            final PropertyDomainEventFacet propertyDomainEventFacet) {
        if (!propertyDomainEventFacet.getEventTypeOrigin().isDefault()) {
            return; // skip if already set explicitly on the property or mixin type
        }

        PropertyDomainEventFacet
                .createObjectTypeSpecificForMixin(objectSpecification, property.getFacetHolder())
                .ifPresent(property::addFacet);
    }

    private void initCollectionWithMixee(
            final ObjectSpecification objectSpecification,
            final OneToManyAssociation collection,
            final CollectionDomainEventFacet collectionDomainEventFacet) {
        if (!collectionDomainEventFacet.getEventTypeOrigin().isDefault()) {
            return; // skip if already set explicitly on the collection or mixin type
        }

        CollectionDomainEventFacet
                .createObjectTypeSpecificForMixin(objectSpecification, collection.getFacetHolder())
                .ifPresent(collection::addFacet);
    }

    private void reportMissing(final ObjectAction act) {
        ValidationFailure.raiseFormatted(act,
                "ActionDomainEventFacet for %s should have already been created via "
                        + "ActionAnnotationFacetFactory, yet was not. (possible causes: mixin declartion is invalid)",
                act.getFeatureIdentifier());
    }

    private void reportMissing(final OneToOneAssociation prop) {
        ValidationFailure.raiseFormatted(prop,
                "PropertyDomainEventFacet for %s should have already been created via "
                        + "PropertyAnnotationFacetFactory, yet was not. (possible causes: mixin declartion is invalid)",
                prop.getFeatureIdentifier());
    }

    private void reportMissing(final OneToManyAssociation coll) {
        ValidationFailure.raiseFormatted(coll,
                "CollectionDomainEventFacet for %s should have already been created via "
                        + "CollectionAnnotationFacetFactory, yet was not. (possible causes: mixin declartion is invalid)",
                coll.getFeatureIdentifier());
    }

}
