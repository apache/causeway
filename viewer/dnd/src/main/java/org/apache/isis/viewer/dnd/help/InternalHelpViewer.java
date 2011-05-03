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

package org.apache.isis.viewer.dnd.help;

import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Viewer;

public class InternalHelpViewer implements HelpViewer {
    private final Viewer viewer;

    public InternalHelpViewer(final Viewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public void open(final Location location, final String name, final String description, final String help) {
        viewer.clearAction();

        final View helpView = new HelpView(name, description, help);
        location.add(20, 20);
        helpView.setLocation(location);

        viewer.setOverlayView(helpView);
    }

}
