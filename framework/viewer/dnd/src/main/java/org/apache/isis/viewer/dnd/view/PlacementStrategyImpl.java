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

// TODO move out of this package and inject it
public class PlacementStrategyImpl implements PlacementStrategy {
    private static final int PADDING = 10;

    @Override
    public Location determinePlacement(final Workspace workspace, final View relativeToView, final View newView) {
        if (relativeToView == null) {
            return new Location();
        }

        final Size workspaceSize = workspace.getSize();
        final View rootView = rootView(workspace, relativeToView);
        final Location rootViewLocation = rootView.getLocation();
        final Size rootViewSize = rootView.getSize();
        final Location newLocation = new Location(rootViewLocation);
        final Size requiredSize = newView.getView().getRequiredSize(Size.createMax());

        if (rootViewLocation.getX() + rootViewSize.getWidth() + PADDING + requiredSize.getWidth() < workspaceSize.getWidth()) {
            newLocation.add(rootViewSize.getWidth() + PADDING, 0);
        } else if (rootViewLocation.getY() + rootViewSize.getHeight() + PADDING + requiredSize.getHeight() < workspaceSize.getHeight()) {
            newLocation.add(0, rootViewSize.getHeight() + PADDING);
        } else if (requiredSize.getWidth() + PADDING < rootViewLocation.getX()) {
            newLocation.subtract(requiredSize.getWidth() + PADDING, 0);
        } else if (requiredSize.getHeight() + PADDING < rootViewLocation.getY()) {
            newLocation.subtract(0, requiredSize.getHeight() + PADDING);
        } else {
            newLocation.add(PADDING * 6, PADDING * 6);
        }

        final int maxSpaceToLeft = workspaceSize.getWidth() - requiredSize.getWidth();
        final int maxSpaceAbove = workspaceSize.getHeight() - requiredSize.getHeight();

        ensureWidth(newLocation, maxSpaceToLeft);
        ensureHeight(newLocation, maxSpaceAbove);

        final Location firstAttempt = new Location(newLocation);

        while (workspace.subviewFor(newLocation) != null && workspace.subviewFor(newLocation).getLocation().equals(newLocation)) {
            newLocation.add(PADDING * 4, PADDING * 4);
            ensureWidth(newLocation, maxSpaceToLeft);
            ensureHeight(newLocation, maxSpaceAbove);

            if (newLocation.equals(firstAttempt)) {
                break;
            }
        }
        return newLocation;
    }

    private void ensureHeight(final Location ofLocation, final int availableHeight) {
        final int yoffset = availableHeight - ofLocation.getY();
        if (yoffset < 0) {
            ofLocation.add(0, yoffset);
            ofLocation.setY(Math.max(0, ofLocation.getY()));
        }
    }

    private void ensureWidth(final Location ofLocation, final int availableWifth) {
        final int xoffset = availableWifth - ofLocation.getX();
        if (xoffset < 0) {
            ofLocation.add(xoffset, 0);
            ofLocation.setX(Math.max(0, ofLocation.getX()));
        }
    }

    private View rootView(final View workspace, final View relativeTo) {
        final View parent = relativeTo.getParent().getView();
        return parent == null || parent == workspace ? relativeTo : rootView(workspace, parent);
    }

}
