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
package org.apache.causeway.core.metamodel.facets.properties.property.modify;

import java.util.function.BiConsumer;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.events.domain.PropertyDomainEvent;
import org.apache.causeway.applib.services.i18n.TranslatableString;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventHelper;
import org.apache.causeway.core.metamodel.facets.SingleClassValueFacetAbstract;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.causeway.core.metamodel.interactions.ProposedHolder;
import org.apache.causeway.core.metamodel.interactions.UsabilityContext;
import org.apache.causeway.core.metamodel.interactions.ValidityContext;
import org.apache.causeway.core.metamodel.interactions.VisibilityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.specloader.specimpl.OneToOneAssociationMixedIn;

public abstract class PropertyDomainEventFacetAbstract
extends SingleClassValueFacetAbstract implements PropertyDomainEventFacet {

    private final DomainEventHelper domainEventHelper;

    private final PropertyOrCollectionAccessorFacet getterFacetIfAny;
    private final TranslationService translationService;
    private final TranslationContext translationContext;

    /**
     * @param getterFacetIfAny - will be null if this is for a mixin {@link OneToOneAssociationMixedIn}.
     */
    public PropertyDomainEventFacetAbstract(
            final Class<? extends PropertyDomainEvent<?, ?>> eventType,
                    final PropertyOrCollectionAccessorFacet getterFacetIfAny,
                    final FacetHolder holder ) {

        super(PropertyDomainEventFacet.class, holder, eventType);
        this.eventType = eventType;
        this.getterFacetIfAny = getterFacetIfAny;

        this.translationService = getTranslationService();
        this.translationContext = holder.getTranslationContext();

        domainEventHelper = DomainEventHelper.ofServiceRegistry(getServiceRegistry());
    }

    private Class<? extends PropertyDomainEvent<?, ?>> eventType;

    @Override
    public Class<?> value() {
        return eventType;
    }

    public <S, T> Class<? extends PropertyDomainEvent<S, T>> getEventType() {
        return _Casts.uncheckedCast(eventType);
    }

    /**
     * Can be overwritten if this facet is on a mixin where the subject (mixedInType) is annotated with
     * {@link DomainObject#propertyDomainEvent()}.
     */
    public void setEventType(final Class<? extends PropertyDomainEvent<?, ?>> eventType) {
        this.eventType = eventType;
    }

    @Override
    public String hides(final VisibilityContext ic) {

        final PropertyDomainEvent<?, ?> event =
                domainEventHelper.postEventForProperty(
                        AbstractDomainEvent.Phase.HIDE,
                        getEventType(), null,
                        getFacetHolder(), ic.getHead(),
                        null, null);
        if (event != null && event.isHidden()) {
            return "Hidden by subscriber";
        }
        return null;
    }

    @Override
    public String disables(final UsabilityContext ic) {

        final PropertyDomainEvent<?, ?> event =
                domainEventHelper.postEventForProperty(
                        AbstractDomainEvent.Phase.DISABLE,
                        getEventType(), null,
                        getFacetHolder(), ic.getHead(),
                        null, null);
        if (event != null && event.isDisabled()) {
            final TranslatableString reasonTranslatable = event.getDisabledReasonTranslatable();
            if(reasonTranslatable != null) {
                return reasonTranslatable.translate(translationService, translationContext);
            }
            return event.getDisabledReason();
        }
        return null;
    }

    @Override
    public String invalidates(final ValidityContext ic) {

        if(getterFacetIfAny == null) {
            return null;
        }

        // if this is a mixin, then this ain't true.
        if(!(ic instanceof ProposedHolder)) {
            return null;
        }
        final ProposedHolder ph = (ProposedHolder) ic;

        final Object oldValue = getterFacetIfAny.getProperty(ic.getTarget(), ic.getInitiatedBy());
        final ManagedObject proposedAdapter = ph.getProposed();
        final Object proposedValue = proposedAdapter != null ? proposedAdapter.getPojo() : null;

        final PropertyDomainEvent<?, ?> event =
                domainEventHelper.postEventForProperty(
                        AbstractDomainEvent.Phase.VALIDATE,
                        getEventType(), null,
                        getFacetHolder(), ic.getHead(),
                        oldValue, proposedValue);
        if (event != null && event.isInvalid()) {
            final TranslatableString reasonTranslatable = event.getInvalidityReasonTranslatable();
            if(reasonTranslatable != null) {
                return reasonTranslatable.translate(translationService, translationContext);
            }
            return event.getInvalidityReason();
        }

        return null;
    }


    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("getterFacet", getterFacetIfAny);
    }

}
