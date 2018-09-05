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
import java.util.stream.Collectors;

import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.annotation.InvokedOn;
import org.apache.isis.applib.services.actinvoc.ActionInvocationContext;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.Command.Executor;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.ToggledMementosProvider;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponse;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseType;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterToggleboxColumn;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.ActionLinkFactory;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlBehaviour;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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

    /**
     * @param objectAction
     * @param linkId
     */
    @Override
    public LinkAndLabel newLink(
            final ObjectAction objectAction,
            final String linkId,
            final ToggledMementosProvider toggledMementosProviderIfAny) {

        final ActionMemento actionMemento = new ActionMemento(objectAction);
        final AbstractLink link = new Link<Object>(linkId) {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                final ObjectAction objectAction = actionMemento.getAction(getSpecificationLoader());
                final ConcurrencyChecking concurrencyChecking =
                        ConcurrencyChecking.concurrencyCheckingFor(objectAction.getSemantics());

                try {
                    final List<ObjectAdapter> toggledAdapters = _NullSafe.stream(model.getToggleMementosList())
                            .map(ObjectAdapterMemento.Functions.fromMemento(
                                    concurrencyChecking, getPersistenceSession(), getSpecificationLoader()))
                            .collect(Collectors.toList());

                    final List<Object> domainObjects =
                            _Lists.transform(toggledAdapters, ObjectAdapter.Functions.getObject());

                    final ActionInvocationContext actionInvocationContext = getServicesInjector().lookupService(ActionInvocationContext.class);
                    if (actionInvocationContext != null) {
                        actionInvocationContext.setInvokedOn(InvokedOn.COLLECTION);
                        actionInvocationContext.setDomainObjects(domainObjects);
                    }

                    ObjectAdapter lastReturnedAdapter = null;
                    int i=0;
                    for(final ObjectAdapter adapter : toggledAdapters) {

                        final CommandContext commandContext = getServicesInjector().lookupService(CommandContext.class);
                        final Command command;
                        if (commandContext != null) {
                            command = commandContext.getCommand();
                            command.setExecutor(Executor.USER);
                        }

                        int numParameters = objectAction.getParameterCount();
                        if(numParameters != 0) {
                            return;
                        }

                        final ObjectAdapter mixedInAdapter = null;
                        final ObjectAdapter[] arguments = {};

                        lastReturnedAdapter = objectAction.executeWithRuleChecking(
                                adapter, mixedInAdapter, arguments,
                                InteractionInitiatedBy.USER, ActionModel.WHERE_FOR_ACTION_INVOCATION
                                );
                        TransactionService transactionService =
                                getServicesInjector().lookupService(TransactionService.class);
                        transactionService.nextTransaction();
                    }


                    model.clearToggleMementosList();
                    toggleboxColumn.clearToggles();

                    final ActionModel actionModel = model.getActionModelHint();
                    if(actionModel != null && actionModel.getActionMemento().getAction(getSpecificationLoader()).getSemantics().isIdempotentInNature()) {
                        actionModel.detach(); // force reload
                        ObjectAdapter resultAdapter = actionModel.getObject();
                        model.setObjectList(resultAdapter);
                    } else {
                        model.setObject(persistentAdaptersWithin(model.getObject()));
                    }

                    if(lastReturnedAdapter != null) {
                        final ActionResultResponse resultResponse =
                                ActionResultResponseType.determineAndInterpretResult(actionModel, null, lastReturnedAdapter);
                        resultResponse.getHandlingStrategy().handleResults(resultResponse, model.getIsisSessionFactory());
                    }

                } catch(final ConcurrencyException ex) {

                    recover();
                    // display a warning to the user so that they know that the action wasn't performed
                    getMessageBroker().addWarning(ex.getMessage());
                    return;

                } catch(final RuntimeException ex) {

                    final RecoverableException appEx = RecoverableException.Util.getRecoverableExceptionIfAny(ex);
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
                    oam.getObjectAdapter(ConcurrencyChecking.NO_CHECK, getPersistenceSession(), getSpecificationLoader());
                }

                // discard any adapters that might have been deleted
                model.setObject(persistentAdaptersWithin(model.getObject()));

                // attempt to preserve the toggled adapters
                final List<ObjectAdapter> adapters = model.getObject();
                model.clearToggleMementosList();
                for (ObjectAdapterMemento oam : toggleMementosList) {
                    final ObjectAdapter objectAdapter = oam.getObjectAdapter(ConcurrencyChecking.NO_CHECK,
                            getPersistenceSession(), getSpecificationLoader());
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

        final String disabledReasonIfAny = null;
        final boolean blobOrClob = false;
        final ObjectAdapter objectAdapter = null;

        return LinkAndLabel.newLinkAndLabel(objectAdapter, objectAction, link, disabledReasonIfAny, blobOrClob);
    }

    ///////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////

    SpecificationLoader getSpecificationLoader() {
        return getIsisSessionFactory().getSpecificationLoader();
    }

    PersistenceSession getPersistenceSession() {
        return getIsisSessionFactory().getCurrentSession().getPersistenceSession();
    }

    public AuthenticationSession getAuthenticationSession() {
        return getIsisSessionFactory().getCurrentSession().getAuthenticationSession();
    }

    protected MessageBroker getMessageBroker() {
        return getAuthenticationSession().getMessageBroker();
    }

    protected ServicesInjector getServicesInjector() {
        return getIsisSessionFactory().getServicesInjector();
    }

    private IsisSessionFactory getIsisSessionFactory() {
        return model.getIsisSessionFactory();
    }

}
