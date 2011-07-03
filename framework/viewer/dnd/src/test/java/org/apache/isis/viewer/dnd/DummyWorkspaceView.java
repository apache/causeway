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

package org.apache.isis.viewer.dnd;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.dnd.view.Placement;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Workspace;

public class DummyWorkspaceView extends DummyView implements Workspace {

    public DummyWorkspaceView() {
        setupAllowSubviewsToBeAdded(true);
    }

    @Override
    public View addIconFor(final ObjectAdapter adapter, final Placement placement) {
        return createAndAddView();
    }

    private DummyView createAndAddView() {
        final DummyView view = new DummyView();
        addView(view);
        return view;
    }

    @Override
    public View addWindowFor(final ObjectAdapter object, final Placement placement) {
        return createAndAddView();
    }

    public View createSubviewFor(final ObjectAdapter object, final boolean asIcon) {
        return createAndAddView();
    }

    @Override
    public void lower(final View view) {
    }

    @Override
    public void raise(final View view) {
    }

    public void removeViewsFor(final ObjectAdapter object) {
    }

    @Override
    public Workspace getWorkspace() {
        return this;
    }

    public void removeObject(final ObjectAdapter object) {
    }

    @Override
    public void addDialog(final View dialog, final Placement placement) {
    }

    @Override
    public void addWindow(final View window, final Placement placement) {
    }

}
