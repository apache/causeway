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
package org.apache.isis.viewer.wicket.ui.components.collection.bulk;

import java.util.List;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.services.actinvoc.ActionInvocationContext;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.InvokedOn;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.Command.Executor;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponse;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseType;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterToggleboxColumn;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.ActionLinkFactory;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlBehaviour;

public final class BulkActionsLinkFactory implements ActionLinkFactory {
    
    private static final long serialVersionUID = 1L;
    private final EntityCollectionModel model;
    
    private final ObjectAdapterToggleboxColumn toggleboxColumn;

    public BulkActionsLinkFactory(
            final EntityCollectionModel model,
            final ObjectAdapterToggleboxColumn toggleboxColumn) {
        this.model = model;
        this.toggleboxColumn = toggleboxColumn;
    }


    @Override
    public LinkAndLabel newLink(
            final ObjectAdapterMemento objectAdapterMemento,
            final ObjectAction objectAction,
            final String linkId) {
        
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


                    final ActionInvocationContext actionInvocationContext = getServicesInjector().lookupService(ActionInvocationContext.class);
                    if (actionInvocationContext != null) {
                        actionInvocationContext.setInvokedOn(InvokedOn.COLLECTION);
                        actionInvocationContext.setDomainObjects(domainObjects);
                    }

                    final Bulk.InteractionContext bulkInteractionContext = getServicesInjector().lookupService(Bulk.InteractionContext.class);
                    if (bulkInteractionContext != null) {
                        bulkInteractionContext.setInvokedAs(Bulk.InteractionContext.InvokedAs.BULK);
                        bulkInteractionContext.setDomainObjects(domainObjects);
                    }

                    final CommandContext commandContext = getServicesInjector().lookupService(CommandContext.class);
                    final Command command;
                    if (commandContext != null) {
                        command = commandContext.getCommand();
                        command.setExecutor(Executor.USER);
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

                        lastReturnedAdapter = objectAction.executeWithRuleChecking(adapter, new ObjectAdapter[]{}, getAuthenticationSession(), ActionModel.WHERE_FOR_ACTION_INVOCATION);
                    }


                    
                    model.clearToggleMementosList();
                    toggleboxColumn.clearToggles();
                    final ActionModel actionModelHint = model.getActionModelHint();
                    if(actionModelHint != null && actionModelHint.getActionMemento().getAction().getSemantics().isIdempotentInNature()) {
                        ObjectAdapter resultAdapter = actionModelHint.getObject();
                        model.setObjectList(resultAdapter);
                    } else {
                        model.setObject(persistentAdaptersWithin(model.getObject()));
                    }
                    
                    if(lastReturnedAdapter != null) {
                        final ActionResultResponse resultResponse = 
                                ActionResultResponseType.determineAndInterpretResult(actionModelHint, null, lastReturnedAdapter);
                        resultResponse.getHandlingStrategy().handleResults(this, resultResponse);
                    }

                } catch(final ConcurrencyException ex) {
                    
                    recover();
                    // display a warning to the user so that they know that the action wasn't performed
                    getMessageBroker().addWarning(ex.getMessage());
                    return;

                } catch(final RuntimeException ex) {
                    
                    final RecoverableException appEx = ActionModel.getApplicationExceptionIfAny(ex);
                    if (appEx != null) {

                        recover();
                        
                        getMessageBroker().setApplicationError(appEx.getMessage());
                        
                        // there's no need to abort the transaction, it will have already been done
                        // (in IsisTransactionManager#executeWithinTransaction(...)). 
                        return;
                    } 
                    throw ex;
                }
            }
            
            private void recover() {
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

        final boolean explorationOrPrototype = ObjectAction.Utils.isExplorationOrPrototype(objectAction);
        final String actionIdentifier = ObjectAction.Utils.actionIdentifierFor(objectAction);
        final String description = ObjectAction.Utils.descriptionOf(objectAction);
        final String cssClass = ObjectAction.Utils.cssClassFor(objectAction, null);
        final String cssClassFa = ObjectAction.Utils.cssClassFaFor(objectAction);
        final ActionLayout.CssClassFaPosition cssClassFaPosition = ObjectAction.Utils.cssClassFaPositionFor(objectAction);
        final ActionLayout.Position position = ObjectAction.Utils.actionLayoutPositionOf(objectAction);

        return new LinkAndLabel(link, objectAction.getName(), null, description, false, explorationOrPrototype, actionIdentifier, cssClass, cssClassFa, cssClassFaPosition, position);
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

    protected ServicesInjector getServicesInjector() {
        return IsisContext.getPersistenceSession().getServicesInjector();
    }

}
