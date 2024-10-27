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
package org.apache.causeway.core.metamodel.facets.actions.action.invocation;

import java.util.Optional;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.events.domain.AbstractDomainEvent;
import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.applib.services.i18n.TranslatableString;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract;
import org.apache.causeway.core.metamodel.facets.DomainEventHelper;
import org.apache.causeway.core.metamodel.facets.object.domainobject.domainevents.ActionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.interactions.ActionInteractionContext;
import org.apache.causeway.core.metamodel.interactions.ActionValidityContext;
import org.apache.causeway.core.metamodel.interactions.DisablingInteractionAdvisor;
import org.apache.causeway.core.metamodel.interactions.HidingInteractionAdvisor;
import org.apache.causeway.core.metamodel.interactions.InteractionContext;
import org.apache.causeway.core.metamodel.interactions.UsabilityContext;
import org.apache.causeway.core.metamodel.interactions.ValidatingInteractionAdvisor;
import org.apache.causeway.core.metamodel.interactions.ValidityContext;
import org.apache.causeway.core.metamodel.interactions.VisibilityContext;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import lombok.NonNull;

public class ActionDomainEventFacet
extends DomainEventFacetAbstract<ActionDomainEvent<?>>
implements
    HidingInteractionAdvisor,
    DisablingInteractionAdvisor,
    ValidatingInteractionAdvisor {

    // -- FACET TYPE

    private static Class<? extends Facet> type() {
        return ActionDomainEventFacet.class;
    }

    // -- FACTORIES

    /**
     * Inspect {@link Action#domainEvent()} if present, else use the default event type.
     */
    public static ActionDomainEventFacet create(
            final @NonNull Optional<Action> actionIfAny,
            /** only used to lookup {@link ActionDomainEventDefaultFacetForDomainObjectAnnotation} */
            final @NonNull ObjectSpecification typeSpec,
            final @NonNull FacetHolder facetHolder){

        var actionDomainEventFacet =
                actionIfAny
                .map(Action::domainEvent)
                .filter(domainEvent -> domainEvent != ActionDomainEvent.Default.class)
                .map(domainEvent ->
                        new ActionDomainEventFacet(domainEvent, EventTypeOrigin.ANNOTATED_MEMBER, facetHolder))
                .orElseGet(()->{

                    var typeFromDomainObject = typeSpec
                            .getFacet(ActionDomainEventDefaultFacetForDomainObjectAnnotation.class);

                    return typeFromDomainObject != null
                            ? new ActionDomainEventFacet(
                                    typeFromDomainObject.getEventType(),
                                    EventTypeOrigin.ANNOTATED_OBJECT, facetHolder)
                            : new ActionDomainEventFacet(
                                    ActionDomainEvent.Default.class,
                                    EventTypeOrigin.DEFAULT, facetHolder);
                });
        return actionDomainEventFacet;
    }

    // -- CONSTRUCTION

    private final TranslationService translationService;
    private final TranslationContext translationContext;
    private final DomainEventHelper domainEventHelper;

    protected ActionDomainEventFacet(
            final Class<? extends ActionDomainEvent<?>> eventType,
            final EventTypeOrigin eventTypeOrigin,
            final FacetHolder holder) {

        super(type(), eventType, eventTypeOrigin, holder);

        this.translationService = getTranslationService();
        this.translationContext = holder.getTranslationContext();

        domainEventHelper = DomainEventHelper.ofServiceRegistry(getServiceRegistry());
    }

    @Override
    public void initWithMixee(final ObjectSpecification mixeeSpec) {
        if(!getEventTypeOrigin().isDefault()) return; // skip if already set explicitly
        mixeeSpec
        .lookupFacet(ActionDomainEventDefaultFacetForDomainObjectAnnotation.class)
        .ifPresent(facetOnMixee->
                super.updateEventType(facetOnMixee.getEventType(), EventTypeOrigin.ANNOTATED_OBJECT));
    }

    @Override
    public String hides(final VisibilityContext ic) {
        if(!isPostable()) return null; // bale out

        final ActionDomainEvent<?> event =
                domainEventHelper.postEventForAction(
                        AbstractDomainEvent.Phase.HIDE,
                        getEventType(),
                        actionFrom(ic), getFacetHolder(),
                        ic.getHead(),
                        // corresponds to programming model 'hidePlaceOrder()',
                        // which does no longer consider args
                        Can.empty(),
                        // result pojo n/a
                        null);
        if (event != null && event.isHidden()) {
            return "Hidden by subscriber";
        }
        return null;
    }

    @Override
    public Optional<VetoReason> disables(final UsabilityContext ic) {
        if(!isPostable()) return Optional.empty(); // bale out

        final ActionDomainEvent<?> event =
                domainEventHelper.postEventForAction(
                        AbstractDomainEvent.Phase.DISABLE,
                        getEventType(),
                        actionFrom(ic), getFacetHolder(),
                        ic.getHead(),
                        // corresponds to programming model 'disablePlaceOrder()',
                        // which does no longer consider args
                        Can.empty(),
                        // result pojo n/a
                        null);
        if (event != null && event.isDisabled()) {
            final TranslatableString reasonTranslatable = event.getDisabledReasonTranslatable();
            final String reasonString = reasonTranslatable != null
                    ? reasonTranslatable.translate(translationService, translationContext)
                    : event.getDisabledReason();

            if(reasonString!=null) {
                return VetoReason.explicit(reasonString).toOptional();
            }
        }
        return Optional.empty();
    }

    @Override
    public String invalidates(final ValidityContext ic) {
        if(!isPostable()) return null; // bale out

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

}
