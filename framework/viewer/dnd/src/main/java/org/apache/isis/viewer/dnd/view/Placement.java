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

package org.apache.isis.viewer.dnd.view;

import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;

/**
 * Used to determine the placement of a new view on the workspace. It can be: an
 * absolute placement given a Location; a position relative to a given view; or
 * in the center. A relative placement uses the PlacementStrategy to determine
 * an optimum location.
 */
public class Placement {
    private static final int ABSOLUTE = 1;
    private static final int RELATIVE = 2;
    public static final int CENTER = 3;
    private static PlacementStrategy placementStrategy = new PlacementStrategyImpl();
    private final Location location;
    private final View relativeTo;
    private final int position;

    public Placement(final Location location) {
        this.location = location;
        relativeTo = null;
        position = ABSOLUTE;
    }

    public Placement(final View relativeTo) {
        this.relativeTo = relativeTo.getView();
        location = null;
        position = RELATIVE;
    }

    public Placement(final int position) {
        this.relativeTo = null;
        location = null;
        this.position = position;
    }

    private Location center(final View workspace, final View view) {
        final Size rootSize = workspace.getSize();
        final Location location = new Location(rootSize.getWidth() / 2, rootSize.getHeight() / 2);
        final Size dialogSize = view.getRequiredSize(new Size(rootSize));
        location.subtract(dialogSize.getWidth() / 2, dialogSize.getHeight() / 2);
        return location;
    }

    public void position(final Workspace workspace, final View view) {
        switch (position) {
        case ABSOLUTE:
            view.setLocation(location);
            break;

        case RELATIVE:
            view.setLocation(placementStrategy.determinePlacement(workspace, relativeTo, view));
            break;

        case CENTER:
            view.setLocation(center(workspace, view));
            break;
        }
    }

}
