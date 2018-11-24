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

package org.apache.isis.core.metamodel.facets.collections.collection.modify;

import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.applib.events.domain.CollectionDomainEvent;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.wrapper.events.UsabilityEvent;
import org.apache.isis.applib.services.wrapper.events.ValidityEvent;
import org.apache.isis.applib.services.wrapper.events.VisibilityEvent;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.DomainEventHelper;
import org.apache.isis.core.metamodel.facets.SingleClassValueFacetAbstract;
import org.apache.isis.core.metamodel.interactions.CollectionAddToContext;
import org.apache.isis.core.metamodel.interactions.ProposedHolder;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

public abstract class CollectionDomainEventFacetAbstract extends SingleClassValueFacetAbstract implements CollectionDomainEventFacet {

    private final DomainEventHelper domainEventHelper;
    private final TranslationService translationService;
    private final String translationContext;

    public CollectionDomainEventFacetAbstract(
            final Class<? extends CollectionDomainEvent<?, ?>> eventType,
                    final FacetHolder holder,
                    final ServicesInjector servicesInjector,
                    final SpecificationLoader specificationLoader) {
        super(CollectionDomainEventFacet.class, holder, eventType, specificationLoader);
        this.eventType = eventType;

        this.translationService = servicesInjector.lookupService(TranslationService.class).orElse(null);;
        // sadness: same as in TranslationFactory
        this.translationContext = ((IdentifiedHolder)holder).getIdentifier().toClassAndNameIdentityString();

        domainEventHelper = new DomainEventHelper(servicesInjector);
    }

    private Class<? extends CollectionDomainEvent<?, ?>> eventType;

    @Override
    public Class<?> value() {
        return eventType;
    }

    public <S, T> Class<? extends CollectionDomainEvent<S, T>> getEventType() {
        return _Casts.uncheckedCast(eventType);
    }

    public void setEventType(final Class<? extends CollectionDomainEvent<?, ?>> eventType) {
        this.eventType = eventType;
    }

    @Override
    public String hides(final VisibilityContext<? extends VisibilityEvent> ic) {

        final CollectionDomainEvent<?, ?> event =
                domainEventHelper.postEventForCollection(
                        AbstractDomainEvent.Phase.HIDE,
                        getEventType(), null,
                        getIdentified(), ic.getTarget(), ic.getMixedIn(),
                        CollectionDomainEvent.Of.ACCESS,
                        null);
        if (event != null && event.isHidden()) {
            return "Hidden by subscriber";
        }
        return null;
    }

    @Override
    public String disables(final UsabilityContext<? extends UsabilityEvent> ic) {

        final CollectionDomainEvent<?, ?> event =
                domainEventHelper.postEventForCollection(
                        AbstractDomainEvent.Phase.DISABLE,
                        getEventType(), null,
                        getIdentified(), ic.getTarget(), ic.getMixedIn(),
                        CollectionDomainEvent.Of.ACCESS,
                        null);
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
    public String invalidates(final ValidityContext<? extends ValidityEvent> ic) {

        // if this is a mixin, then this ain't true.
        if(!(ic instanceof ProposedHolder)) {
            return null;
        }
        final ProposedHolder catc = (ProposedHolder) ic;
        final Object proposed = catc.getProposed().getPojo();

        final CollectionDomainEvent.Of of =
                ic instanceof CollectionAddToContext
                ? CollectionDomainEvent.Of.ADD_TO
                        : CollectionDomainEvent.Of.REMOVE_FROM;

        final CollectionDomainEvent<?, ?> event =
                domainEventHelper.postEventForCollection(
                        AbstractDomainEvent.Phase.VALIDATE,
                        getEventType(), null,
                        getIdentified(), ic.getTarget(), ic.getMixedIn(),
                        of,
                        proposed);
        if (event != null && event.isInvalid()) {
            final TranslatableString reasonTranslatable = event.getInvalidityReasonTranslatable();
            if(reasonTranslatable != null) {
                return reasonTranslatable.translate(translationService, translationContext);
            }
            return event.getInvalidityReason();
        }

        return null;
    }


}
