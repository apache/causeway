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

package org.apache.isis.metamodel.facets.actions.action.invocation;

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.events.domain.AbstractDomainEvent;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.applib.services.wrapper.events.UsabilityEvent;
import org.apache.isis.applib.services.wrapper.events.ValidityEvent;
import org.apache.isis.applib.services.wrapper.events.VisibilityEvent;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.metamodel.facets.DomainEventHelper;
import org.apache.isis.metamodel.facets.SingleClassValueFacetAbstract;
import org.apache.isis.metamodel.interactions.ActionInteractionContext;
import org.apache.isis.metamodel.interactions.ActionValidityContext;
import org.apache.isis.metamodel.interactions.InteractionContext;
import org.apache.isis.metamodel.interactions.UsabilityContext;
import org.apache.isis.metamodel.interactions.ValidityContext;
import org.apache.isis.metamodel.interactions.VisibilityContext;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

public abstract class ActionDomainEventFacetAbstract
extends SingleClassValueFacetAbstract 
implements ActionDomainEventFacet {

    @Getter @Setter private Class<? extends ActionDomainEvent<?>> eventType;
    private final TranslationService translationService;
    private final String translationContext;
    private final DomainEventHelper domainEventHelper;

    public ActionDomainEventFacetAbstract(
            final Class<? extends ActionDomainEvent<?>> eventType,
            final FacetHolder holder) {
        
        super(ActionDomainEventFacet.class, holder, eventType);
        setEventType(eventType);

        this.translationService = getTranslationService();
        // sadness: same as in TranslationFactory
        this.translationContext = ((IdentifiedHolder)holder).getIdentifier().toClassAndNameIdentityString();

        domainEventHelper = DomainEventHelper.ofServiceRegistry(getServiceRegistry());
    }

    @Override
    public Class<?> value() {
        return eventType;
    }
    
    @Override
    public String hides(final VisibilityContext<? extends VisibilityEvent> ic) {

        final ActionDomainEvent<?> event =
                domainEventHelper.postEventForAction(
                        AbstractDomainEvent.Phase.HIDE,
                        getEventType(),
                        actionFrom(ic), getIdentified(),
                        ic.getTarget(), ic.getMixedIn(), argumentAdaptersFrom(ic),
                        null);
        if (event != null && event.isHidden()) {
            return "Hidden by subscriber";
        }
        return null;
    }

    @Override
    public String disables(UsabilityContext<? extends UsabilityEvent> ic) {

        final ActionDomainEvent<?> event =
                domainEventHelper.postEventForAction(
                        AbstractDomainEvent.Phase.DISABLE,
                        getEventType(),
                        actionFrom(ic), getIdentified(),
                        ic.getTarget(), ic.getMixedIn(), argumentAdaptersFrom(ic),
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

        final ActionValidityContext aic = (ActionValidityContext) ic;
        final ActionDomainEvent<?> event =
                domainEventHelper.postEventForAction(
                        AbstractDomainEvent.Phase.VALIDATE,
                        getEventType(),
                        actionFrom(ic), getIdentified(),
                        ic.getTarget(), ic.getMixedIn(), aic.getArgs(),
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
    
    private static ObjectAction actionFrom(final InteractionContext<?> ic) {
        if(!(ic instanceof ActionInteractionContext)) {
            throw new IllegalStateException(
                    "Expecting ic to be of type ActionInteractionContext, instead was: " + ic);
        }
        return ((ActionInteractionContext) ic).getObjectAction();
    }

    private static List<ManagedObject> argumentAdaptersFrom(
            final InteractionContext<? extends InteractionEvent> ic) {
        
        val contributee = ic.getContributeeWithParamIndex();

        if(contributee!=null) {

            val adapter = contributee.getIndex() == 0
                    ? contributee.getValue()
                            : ManagedObject.empty();
            
            return Collections.singletonList(adapter);
                
        }

        return Collections.emptyList();
    }

}
