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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.link.AbstractLink;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuLinkFactory;

final class BulkCollectionsLinkFactory implements CssMenuLinkFactory {
    private static final long serialVersionUID = 1L;
    private final EntityCollectionModel model;
    private final DataTable<ObjectAdapter,String> dataTable;

    BulkCollectionsLinkFactory(EntityCollectionModel model, DataTable<ObjectAdapter,String> dataTable) {
        this.model = model;
        this.dataTable = dataTable;
    }

    @Override
    public LinkAndLabel newLink(final ObjectAdapterMemento contributorAdapterMemento, final ObjectAction objectAction, final String linkId) {
        final ActionMemento actionMemento = new ActionMemento(objectAction);
        AbstractLink link = new AjaxLink<Void>(linkId) {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                final ObjectAction objectAction = actionMemento.getAction();
                for(ObjectAdapterMemento contributeeAdapterMemento: model.getToggleMementosList()) {
                    final ObjectAdapter contributorAdapter = contributorAdapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK);
                    final ObjectAdapter contributeeAdapter = contributeeAdapterMemento.getObjectAdapter(ConcurrencyChecking.CHECK);
                    objectAction.execute(contributorAdapter, new ObjectAdapter[]{contributeeAdapter});
                }
                model.clearToggleMementosList();
                target.add(dataTable);
            }
        };
        return new LinkAndLabel(link, objectAction.getName());
    }
}