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
package org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.wicket.Session;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.Bulk.InteractionContext;
import org.apache.isis.applib.annotation.Bulk.InteractionContext.InvokedAs;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.util.MementoFunctions;
import org.apache.isis.viewer.wicket.model.util.ObjectAdapterFunctions;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponse;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseType;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuItem;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.ActionLinkFactory;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlBehaviour;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;

final class BulkActionsLinkFactory implements ActionLinkFactory {
    
    private static final long serialVersionUID = 1L;
    private final EntityCollectionModel model;
    
    @SuppressWarnings("unused")
    private final DataTable<ObjectAdapter,String> dataTable;

    BulkActionsLinkFactory(EntityCollectionModel model, DataTable<ObjectAdapter,String> dataTable) {
        this.model = model;
        this.dataTable = dataTable;
    }

    @Override
    public LinkAndLabel newLink(
            final ObjectAdapterMemento serviceAdapterMemento, 
            final ObjectAction objectAction, 
            final String linkId, 
            final ActionPromptProvider actionPromptModalWindowProvider) {
        
        final ActionMemento actionMemento = new ActionMemento(objectAction);
        final AbstractLink link = new Link<Object>(linkId) {
            
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                final ObjectAction objectAction = actionMemento.getAction();
                final ConcurrencyChecking concurrencyChecking = 
                        ConcurrencyChecking.concurrencyCheckingFor(objectAction.getSemantics());

                try {
                    final List<ObjectAdapterMemento> toggleMementosList = model.getToggleMementosList();

                    final Iterable<ObjectAdapter> toggledAdapters = 
                            Iterables.transform(toggleMementosList, ObjectAdapterMemento.Functions.fromMemento(concurrencyChecking));
                    
                    final List<Object> domainObjects = Lists.newArrayList(Iterables.transform(toggledAdapters, ObjectAdapter.Functions.getObject()));
                    
                    
                    final Bulk.InteractionContext bulkInteractionContext = Bulk.InteractionContext.current.get();
                    if (bulkInteractionContext != null) {
                        bulkInteractionContext.setInvokedAs(InvokedAs.BULK);
                        bulkInteractionContext.setDomainObjects(domainObjects);
                    }

                    ObjectAdapter lastReturnedAdapter = null;
                    int i=0;
                    for(final ObjectAdapter adapter : toggledAdapters) {
    
                        int numParameters = objectAction.getParameterCount();
                        if(numParameters != 0) {
                            return;
                        }
                        if (bulkInteractionContext != null) {
                            bulkInteractionContext.setIndex(i++);
                        }
                        lastReturnedAdapter = objectAction.execute(adapter, new ObjectAdapter[]{});
                    }
                    
                    model.clearToggleMementosList();
                    final ActionModel actionModelHint = model.getActionModelHint();
                    if(actionModelHint != null) {
                        ObjectAdapter resultAdapter = actionModelHint.getObject();
                        model.setObjectList(resultAdapter);
                    } else {
                        model.setObject(persistentAdaptersWithin(model.getObject()));
                    }
                    
                    if(lastReturnedAdapter != null) {
                        final ActionResultResponse resultResponse = 
                                ActionResultResponseType.determineAndInterpretResult(actionModelHint, lastReturnedAdapter);
                        if(resultResponse.isToPage()) {
                            setResponsePage(resultResponse.getToPage());
                        }
                    }

                } catch(final ConcurrencyException ex) {
                    
                    // resync with the objectstore
                    final List<ObjectAdapterMemento> toggleMementosList = Lists.newArrayList(model.getToggleMementosList());
                    for (ObjectAdapterMemento oam : toggleMementosList) {
                        // just requesting the adapter will sync the OAM's version with the objectstore
                        oam.getObjectAdapter(ConcurrencyChecking.NO_CHECK);
                    }
                    
                    // discard any adapters that might have been deleted
                    model.setObject(persistentAdaptersWithin(model.getObject()));
                    
                    // attempt to preserve the toggled adapters
                    final List<ObjectAdapter> adapters = model.getObject();
                    model.clearToggleMementosList();
                    for (ObjectAdapterMemento oam : toggleMementosList) {
                        final ObjectAdapter objectAdapter = oam.getObjectAdapter(ConcurrencyChecking.NO_CHECK);
                        if(adapters.contains(objectAdapter)) {
                            // in case it has been deleted...
                            model.toggleSelectionOn(objectAdapter);
                        }
                    }
                    
                    // display a warning to the user so that they know that the action wasn't performed
                    getMessageBroker().addWarning(ex.getMessage());
                    return;
                }
            }

            private List<ObjectAdapter> persistentAdaptersWithin(List<ObjectAdapter> adapters) {
                return Lists.newArrayList(Iterables.filter(adapters, new Predicate<ObjectAdapter>() {
                    @Override
                    public boolean apply(ObjectAdapter input) {
                        return !input.isTransient() && !input.isDestroyed();
                    }
                }));
            }

        };
        link.add(new JGrowlBehaviour());
        final boolean explorationOrPrototype = CssMenuItem.isExplorationOrPrototype(objectAction);
        final String actionIdentifier = CssMenuItem.actionIdentifierFor(objectAction);
        final String cssClass = CssMenuItem.cssClassFor(objectAction);

        return new LinkAndLabel(link, objectAction.getName(), null, false, explorationOrPrototype, actionIdentifier, cssClass);
    }
    
    
    ///////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////

    public AuthenticationSession getAuthenticationSession() {
        final AuthenticationSessionProvider asa = (AuthenticationSessionProvider) Session.get();
        return asa.getAuthenticationSession();
    }

    protected MessageBroker getMessageBroker() {
        return getAuthenticationSession().getMessageBroker();
    }

}