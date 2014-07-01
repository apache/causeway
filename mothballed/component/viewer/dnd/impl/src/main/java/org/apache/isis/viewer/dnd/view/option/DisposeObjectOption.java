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

package org.apache.isis.viewer.dnd.view.option;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.Persistor;
import org.apache.isis.core.runtime.system.transaction.UpdateNotifier;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.ObjectContent;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Workspace;

/**
 * Destroy this object
 */
public class DisposeObjectOption extends UserActionAbstract {
    public DisposeObjectOption() {
        super("Dispose Object", ActionType.EXPLORATION);
    }

    @Override
    public Consent disabled(final View view) {
        final ObjectAdapter adapter = view.getContent().getAdapter();
        if (adapter.isDestroyed()) {
            // TODO: move logic into Facet
            return new Veto("Can't do anything with a destroyed object");
        }
        if (isObjectInRootView(view)) {
            return Allow.DEFAULT;
        } else {
            // TODO: move logic into Facet
            return new Veto("Can't dispose an object from within another view.");
        }
    }

    private boolean isObjectInRootView(final View view) {
        final View rootView = rootView(view);
        return view.getContent() == rootView.getContent();
    }

    private View rootView(final View view) {
        final View parent = view.getParent();
        if (view.getWorkspace() == parent) {
            return view;
        } else {
            return rootView(parent);
        }
    }

    @Override
    public void execute(final Workspace workspace, final View view, final Location at) {

        final ObjectAdapter object = ((ObjectContent) view.getContent()).getObject();

        // xactn mgmt now done by PersistenceSession#destroyObject()
        // getTransactionManager().startTransaction();

        getPersistenceSession().destroyObject(object);

        // getTransactionManager().endTransaction();

        getUpdateNotifier().addDisposedObject(object);
        view.getViewManager().disposeUnneededViews();
        view.getFeedbackManager().showMessagesAndWarnings();
    }

    // /////////////////////////////////////////////////////
    // Dependencies (from context)
    // /////////////////////////////////////////////////////

    private static Persistor getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private static UpdateNotifier getUpdateNotifier() {
        return IsisContext.getUpdateNotifier();
    }

}
