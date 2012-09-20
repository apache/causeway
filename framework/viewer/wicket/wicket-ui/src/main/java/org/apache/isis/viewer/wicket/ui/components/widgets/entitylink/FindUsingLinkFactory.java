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

package org.apache.isis.viewer.wicket.ui.components.widgets.entitylink;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.Link;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.common.NoResultsHandler;
import org.apache.isis.viewer.wicket.model.common.SelectionHandler;
import org.apache.isis.viewer.wicket.model.mementos.ActionMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionModel.SingleResultsMode;
import org.apache.isis.viewer.wicket.model.util.Actions;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuLinkFactory;

public final class FindUsingLinkFactory implements CssMenuLinkFactory {
    
    public interface Callback {
        public void onSelected(ObjectAdapter adapter);
        public void onNoResults();
        public void onClick(ActionModel actionModel);
    }

    private static final long serialVersionUID = 1L;

    private final Callback callback;

    public FindUsingLinkFactory(final Callback entityLink) {
        this.callback = entityLink;
    }

    @Override
    public LinkAndLabel newLink(final ObjectAdapterMemento adapterMemento, final ObjectAction action, final String linkId) {
        final ActionMemento actionMemento = new ActionMemento(action);
        final ActionModel.Mode actionMode = ActionModel.determineMode(action);
        final ActionModel actionModel = ActionModel.create(adapterMemento, actionMemento, actionMode, SingleResultsMode.SELECT);

        actionModel.setSelectionHandler(new SelectionHandler() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSelected(final Component context, final ObjectAdapter selectedAdapter) {
                callback.onSelected(selectedAdapter);
            }
        });
        actionModel.setNoResultsHandler(new NoResultsHandler() {
            private static final long serialVersionUID = 1L;

            @Override
            public void onNoResults(final Component context) {
                callback.onNoResults();
            }
        });

        return new LinkAndLabel(new Link<String>(linkId) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                callback.onClick(actionModel);
            }
        }, Actions.labelFor(action));
    }

}