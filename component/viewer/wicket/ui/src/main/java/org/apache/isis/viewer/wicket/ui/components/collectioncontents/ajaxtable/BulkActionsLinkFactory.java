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

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuItem;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuLinkFactory;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlBehaviour;

final class BulkActionsLinkFactory implements CssMenuLinkFactory {
    private static final long serialVersionUID = 1L;
    private final EntityCollectionModel model;
    
    @SuppressWarnings("unused")
    private final DataTable<ObjectAdapter,String> dataTable;

    BulkActionsLinkFactory(EntityCollectionModel model, DataTable<ObjectAdapter,String> dataTable) {
        this.model = model;
        this.dataTable = dataTable;
    }

    @Override
    public LinkAndLabel newLink(final ObjectAdapterMemento serviceAdapterMemento, final ObjectAction objectAction, final String linkId) {
        final ActionMemento actionMemento = new ActionMemento(objectAction);
        final AbstractLink link = new Link<Object>(linkId) {
            
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                final ObjectAction objectAction = actionMemento.getAction();
                
                for(ObjectAdapterMemento entityAdapterMemento: model.getToggleMementosList()) {
                    // REVIEW: have disabled concurrency checking here...
                    final ObjectAdapter entityAdapter = entityAdapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK);

                    int numParameters = objectAction.getParameterCount();
                    if(objectAction.isContributed()) {
                        // a contributed action
                        if(numParameters != 1) {
                            return;
                        }
                        if(serviceAdapterMemento == null) {
                            // not expected
                            return;
                        }
                        final ObjectAdapter serviceAdapter = serviceAdapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK);
                        objectAction.execute(serviceAdapter, new ObjectAdapter[]{entityAdapter});
                    } else {
                        // an entity action
                        if(numParameters != 0) {
                            return;
                        }
                        objectAction.execute(entityAdapter, new ObjectAdapter[]{});
                    }
                }
                model.clearToggleMementosList();
                model.setObject(persistentAdaptersWithin(model.getObject()));
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

        return new LinkAndLabel(link, objectAction.getName(), null, false, explorationOrPrototype, actionIdentifier);
    }
}