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

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.events.domain.PropertyDomainEvent;
import org.apache.causeway.applib.services.i18n.TranslatableString;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract;
import org.apache.causeway.core.metamodel.facets.DomainEventHelper;
import org.apache.causeway.core.metamodel.facets.object.domainobject.domainevents.PropertyDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.causeway.core.metamodel.interactions.DisablingInteractionAdvisor;
import org.apache.causeway.core.metamodel.interactions.HidingInteractionAdvisor;
import org.apache.causeway.core.metamodel.interactions.ProposedHolder;
import org.apache.causeway.core.metamodel.interactions.UsabilityContext;
import org.apache.causeway.core.metamodel.interactions.ValidatingInteractionAdvisor;
import org.apache.causeway.core.metamodel.interactions.ValidityContext;
import org.apache.causeway.core.metamodel.interactions.VisibilityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.specimpl.OneToOneAssociationMixedIn;

import lombok.NonNull;

public class PropertyDomainEventFacet
extends DomainEventFacetAbstract<PropertyDomainEvent<?, ?>>
implements
    HidingInteractionAdvisor,
    DisablingInteractionAdvisor,
    ValidatingInteractionAdvisor {

    // -- FACET TYPE

    private static Class<? extends Facet> type() {
        return PropertyDomainEventFacet.class;
    }

    // -- FACTORIES

    /**
     * Inspect {@link Property#domainEvent()} if present, else use the default event type.
     * @param getterFacet - will be empty if this is for a mixed-in property {@link OneToOneAssociationMixedIn}.
     */
    public static PropertyDomainEventFacet create(
            final @NonNull Optional<Property> propertyIfAny,
            final @NonNull Class<?> classBeingIntrospected,
            final @NonNull Optional<PropertyOrCollectionAccessorFacet> getterFacet,
            final @NonNull FacetHolder facetHolder) {

        var propertyDomainEventFacet = propertyIfAny
                .map(Property::domainEvent)
                .filter(domainEvent -> domainEvent != PropertyDomainEvent.Default.class)
                .map(domainEvent -> new PropertyDomainEventFacet(domainEvent,
                        EventTypeOrigin.ANNOTATED_MEMBER, getterFacet, facetHolder))
                .orElseGet(()->{

                    /* only used to lookup {@link PropertyDomainEventDefaultFacetForDomainObjectAnnotation} */
                    var typeSpec = facetHolder.getSpecificationLoader().loadSpecification(classBeingIntrospected);
                    var typeFromDomainObject = typeSpec.getFacet(PropertyDomainEventDefaultFacetForDomainObjectAnnotation.class);

                    return typeFromDomainObject != null
                            ? new PropertyDomainEventFacet(
                                    typeFromDomainObject.getEventType(),
                                    EventTypeOrigin.ANNOTATED_OBJECT, getterFacet, facetHolder)
                            : new PropertyDomainEventFacet(
                                    PropertyDomainEvent.Default.class,
                                    EventTypeOrigin.DEFAULT, getterFacet, facetHolder);
                });

        return propertyDomainEventFacet;
    }

    // -- CONSTRUCTION

    private final DomainEventHelper domainEventHelper;

    private final PropertyOrCollectionAccessorFacet getterFacetIfAny;
    private final TranslationService translationService;
    private final TranslationContext translationContext;

    /**
     * @param getterFacet - will be empty if this is for a mixed-in property {@link OneToOneAssociationMixedIn}.
     */
    protected PropertyDomainEventFacet(
            final @NonNull Class<? extends PropertyDomainEvent<?, ?>> eventType,
            final @NonNull EventTypeOrigin eventTypeOrigin,
            final @NonNull Optional<PropertyOrCollectionAccessorFacet> getterFacet,
            final @NonNull FacetHolder holder) {

        super(type(), eventType, eventTypeOrigin, holder);
        this.getterFacetIfAny = getterFacet.orElse(null);

        this.translationService = getTranslationService();
        this.translationContext = holder.getTranslationContext();

        domainEventHelper = DomainEventHelper.ofServiceRegistry(getServiceRegistry());
    }

    @Override
    public void initWithMixee(final ObjectSpecification mixeeSpec) {
        if(!getEventTypeOrigin().isDefault()) return; // skip if already set explicitly
        mixeeSpec
        .lookupFacet(PropertyDomainEventDefaultFacetForDomainObjectAnnotation.class)
        .ifPresent(facetOnMixee->
                super.updateEventType(facetOnMixee.getEventType(), EventTypeOrigin.ANNOTATED_OBJECT));
    }

    @Override
    public String hides(final VisibilityContext ic) {
        if(!isPostable()) return null; // bale out

        final PropertyDomainEvent<?, ?> event =
                domainEventHelper.postEventForProperty(
                        AbstractDomainEvent.Phase.HIDE,
                        _Casts.uncheckedCast(getEventType()), null,
                        getFacetHolder(), ic.getHead(),
                        null, null);
        if (event != null && event.isHidden()) {
            return "Hidden by subscriber";
        }
        return null;
    }

    @Override
    public Optional<VetoReason> disables(final UsabilityContext ic) {
        if(!isPostable()) return Optional.empty(); // bale out

        final PropertyDomainEvent<?, ?> event =
                domainEventHelper.postEventForProperty(
                        AbstractDomainEvent.Phase.DISABLE,
                        _Casts.uncheckedCast(getEventType()), null,
                        getFacetHolder(), ic.getHead(),
                        null, null);
        if (event != null
                && event.isDisabled()) {

            final TranslatableString reasonTranslatable = event.getDisabledReasonTranslatable();
            final String reasonString = reasonTranslatable != null
                    ? reasonTranslatable.translate(translationService, translationContext)
                    : event.getDisabledReason();

            return Optional.ofNullable(reasonString)
                .map(VetoReason::explicit);
        }
        return Optional.empty();
    }

    @Override
    public String invalidates(final ValidityContext ic) {
        if(!isPostable()) return null; // bale out

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
                        _Casts.uncheckedCast(getEventType()), null,
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
