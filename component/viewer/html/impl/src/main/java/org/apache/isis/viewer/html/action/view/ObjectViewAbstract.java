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

package org.apache.isis.viewer.html.action.view;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.Persistor;
import org.apache.isis.viewer.html.action.Action;
import org.apache.isis.viewer.html.action.ActionException;
import org.apache.isis.viewer.html.action.view.util.MenuUtil;
import org.apache.isis.viewer.html.component.Page;
import org.apache.isis.viewer.html.component.ViewPane;
import org.apache.isis.viewer.html.context.Context;
import org.apache.isis.viewer.html.request.Request;

public abstract class ObjectViewAbstract implements Action {

    @Override
    public final void execute(final Request request, final Context context, final Page page) {
        final String idString = request.getObjectId();
        final ObjectAdapter adapter = context.getMappedObject(idString);
        if (adapter == null) {
            throw new ActionException("No such object: " + idString);
        }

        getPersistenceSession().resolveImmediately(adapter);

        page.setTitle(adapter.titleString());

        final ViewPane content = page.getViewPane();
        content.setWarningsAndMessages(context.getMessages(), context.getWarnings());
        content.setTitle(adapter.titleString(), adapter.getSpecification().getDescription());
        content.setIconName(adapter.getIconName());

        if (addObjectToHistory()) {
            context.addObjectToHistory(idString);
        }

        context.purgeObjectsAndCollections();

        content.setMenu(MenuUtil.menu(adapter, idString, context));

        String iconName = adapter.getIconName();
        if (iconName == null) {
            iconName = adapter.getSpecification().getShortIdentifier();
        }

        content.setIconName(iconName);

        final String field = request.getProperty();
        doExecute(context, content, adapter, field);

        context.clearMessagesAndWarnings();
    }

    protected void doExecute(final Context context, final ViewPane content, final ObjectAdapter object, final String field) {
    }

    protected boolean addObjectToHistory() {
        return false;
    }

    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

    protected Persistor getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

}
