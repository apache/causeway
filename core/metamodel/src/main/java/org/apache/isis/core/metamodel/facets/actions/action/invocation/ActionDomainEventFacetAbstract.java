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

package org.apache.isis.core.metamodel.facets.actions.action.invocation;

import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.DomainEventHelper;
import org.apache.isis.core.metamodel.facets.SingleClassValueFacetAbstract;
import org.apache.isis.core.metamodel.interactions.ActionInteractionContext;
import org.apache.isis.core.metamodel.interactions.ActionValidityContext;
import org.apache.isis.core.metamodel.interactions.InteractionContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import lombok.Setter;

public abstract class ActionDomainEventFacetAbstract
extends SingleClassValueFacetAbstract
implements ActionDomainEventFacet {

    @Getter @Setter private Class<? extends ActionDomainEvent<?>> eventType;
    private final TranslationService translationService;
    private final TranslationContext translationContext;
    private final DomainEventHelper domainEventHelper;

    public ActionDomainEventFacetAbstract(
            final Class<? extends ActionDomainEvent<?>> eventType,
            final FacetHolder holder) {

        super(ActionDomainEventFacet.class, holder, eventType);
        setEventType(eventType);

        this.translationService = getTranslationService();
        // sadness: same as in TranslationFactory
        this.translationContext = TranslationContext.forTranslationContextHolder(
                holder.getIdentifier());

        domainEventHelper = DomainEventHelper.ofServiceRegistry(getServiceRegistry());
    }

    @Override
    public Class<?> value() {
        return eventType;
    }

    @Override
    public String hides(final VisibilityContext ic) {

        final ActionDomainEvent<?> event =
                domainEventHelper.postEventForAction(
                        AbstractDomainEvent.Phase.HIDE,
                        getEventType(),
                        actionFrom(ic), getFacetHolder(),
                        ic.getHead(), argumentAdaptersFrom(ic),
                        null);
        if (event != null && event.isHidden()) {
            return "Hidden by subscriber";
        }
        return null;
    }

    @Override
    public String disables(UsabilityContext ic) {

        final ActionDomainEvent<?> event =
                domainEventHelper.postEventForAction(
                        AbstractDomainEvent.Phase.DISABLE,
                        getEventType(),
                        actionFrom(ic), getFacetHolder(),
                        ic.getHead(), argumentAdaptersFrom(ic),
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
    public String invalidates(final ValidityContext ic) {

        _Assert.assertTrue(ic instanceof ActionValidityContext, ()->
            String.format("expecting an action context but got %s", ic.getIdentifier()));

        final ActionValidityContext aic = (ActionValidityContext) ic;
        final ActionDomainEvent<?> event =
                domainEventHelper.postEventForAction(
                        AbstractDomainEvent.Phase.VALIDATE,
                        getEventType(),
                        actionFrom(ic), getFacetHolder(),
                        ic.getHead(), aic.getArgs(),
                        null);
        if (event != null && event.isInvalid()) {
            final TranslatableString reasonTranslatable = event.getInvalidityReasonTranslatable();
            if(reasonTranslatable != null) {
                return reasonTranslatable.translate(translationService, translationContext);
            }
            return event.getInvalidityReason();
        }

        return null;
    }

    // -- HELPER

    private static ObjectAction actionFrom(final InteractionContext ic) {
        if(!(ic instanceof ActionInteractionContext)) {
            throw new IllegalStateException(
                    "Expecting ic to be of type ActionInteractionContext, instead was: " + ic);
        }
        return ((ActionInteractionContext) ic).getObjectAction();
    }

    @Deprecated
    private static Can<ManagedObject> argumentAdaptersFrom(
            final InteractionContext ic) {

        return Can.empty();
    }

}
