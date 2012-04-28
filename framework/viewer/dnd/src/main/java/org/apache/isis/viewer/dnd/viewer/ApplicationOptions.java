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

package org.apache.isis.viewer.dnd.viewer;

import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.help.AboutView;
import org.apache.isis.viewer.dnd.view.MenuOptions;
import org.apache.isis.viewer.dnd.view.Placement;
import org.apache.isis.viewer.dnd.view.ShutdownListener;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

public class ApplicationOptions implements MenuOptions {
    private final ShutdownListener listener;

    public ApplicationOptions(final ShutdownListener listener) {
        this.listener = listener;
    }

    @Override
    public void menuOptions(final UserActionSet options) {
        options.add(new UserActionAbstract("About...") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                final AboutView dialogView = new AboutView();
                final Size windowSize = dialogView.getRequiredSize(new Size());
                final Size workspaceSize = workspace.getSize();
                final int x = workspaceSize.getWidth() / 2 - windowSize.getWidth() / 2;
                final int y = workspaceSize.getHeight() / 2 - windowSize.getHeight() / 2;
                workspace.addDialog(dialogView, new Placement(new Location(x, y)));
            }
        });

        options.add(new UserActionAbstract("Log out") {
            @Override
            public Consent disabled(final View view) {
                final boolean runningAsExploration = view.getViewManager().isRunningAsExploration();
                if (runningAsExploration) {
                    // TODO: move logic to Facet
                    return new Veto("Can't log out in exploration mode");
                } else {
                    return Allow.DEFAULT;
                }
            }

            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                listener.logOut();
            }
        });

        options.add(new UserActionAbstract("Quit") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                listener.quit();
            }
        });

    }
}
