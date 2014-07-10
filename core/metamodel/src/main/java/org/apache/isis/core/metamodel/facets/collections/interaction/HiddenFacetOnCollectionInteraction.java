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

package org.apache.isis.core.metamodel.facets.collections.interaction;

import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.events.VisibilityEvent;
import org.apache.isis.applib.services.eventbus.AbstractInteractionEvent;
import org.apache.isis.applib.services.eventbus.CollectionInteractionEvent;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.InteractionHelper;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.facets.members.hidden.HiddenFacetAbstract;

public class HiddenFacetOnCollectionInteraction extends FacetAbstract implements HiddenFacet {

    private final InteractionHelper interactionHelper;

    public HiddenFacetOnCollectionInteraction(
            final FacetHolder holder,
            final ServicesInjector servicesInjector
    ) {
        super(HiddenFacetAbstract.type(), holder, Derivation.NOT_DERIVED);
        interactionHelper = new InteractionHelper(servicesInjector, AbstractInteractionEvent.Mode.HIDE);
    }

    @Override
    public String hides(VisibilityContext<? extends VisibilityEvent> ic) {
        if(!interactionHelper.hasEventBusService()) {
            return null;
        }
        final CollectionInteractionEvent<?, ?> event = postEvent(ic);

        return event != null && event.isHidden()? "Hidden by subscriber": null;
    }

    private CollectionInteractionEvent<?, ?> postEvent(VisibilityContext<? extends VisibilityEvent> ic) {

        // could use either the InteractionWithCollectionAddFacet or the InteractionWithCollectionRemoveFacet;
        // both will identify the same eventType
        final InteractionWithCollectionAddFacet collectionAddFacet = getFacetHolder().getFacet(InteractionWithCollectionAddFacet.class);

        // could be null if this is a read-only or derived collection.
        if(collectionAddFacet != null) {
            final Class eventType = collectionAddFacet.value();
            return interactionHelper.postEventForCollectionAdd(eventType, ic.getTarget(), getIdentified(), null);
        }

        // if add facet was null, then the remove facet almost certainly will be too; but check for "symmetry"
        final InteractionWithCollectionRemoveFacet collectionRemoveFacet = getFacetHolder().getFacet(InteractionWithCollectionRemoveFacet.class);
        if(collectionRemoveFacet != null) {
            final Class eventType = collectionRemoveFacet.value();
            return interactionHelper.postEventForCollectionRemove(eventType, ic.getTarget(), getIdentified(), null);
        }

        return null;
    }

    @Override
    public Where where() {
        return Where.EVERYWHERE;
    }

    @Override
    public When when() {
        return When.ALWAYS;
    }



}
