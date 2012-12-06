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

import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Workspace;

public class IconizeViewOption extends UserActionAbstract {
    public IconizeViewOption() {
        super("Iconize");
    }

    @Override
    public Consent disabled(final View view) {
        return Allow.DEFAULT;
    }

    @Override
    public void execute(final Workspace workspace, final View view, final Location at) {
        final View minimizedView = Toolkit.getViewFactory().createMinimizedView(view);
        minimizedView.setLocation(view.getLocation());
        final View[] views = workspace.getSubviews();
        for (final View view2 : views) {
            if (view2 == view) {
                workspace.removeView(view);
                workspace.addView(minimizedView);
                workspace.invalidateLayout();
                return;
            }
        }

        /*
         * // TODO change so that an iconized version of the window is created
         * and displayed, which holds the original view. View iconView = new
         * RootIconSpecification().createView(view.getContent(), null);
         * iconView.setLocation(view.getLocation()); workspace.replaceView(view,
         * iconView);
         */
    }

    @Override
    public String getDescription(final View view) {
        return "Show this object as an icon on the workspace";
    }
}
