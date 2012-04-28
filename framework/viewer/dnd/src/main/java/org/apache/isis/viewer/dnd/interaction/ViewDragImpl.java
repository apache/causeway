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

package org.apache.isis.viewer.dnd.interaction;

import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Offset;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewDrag;
import org.apache.isis.viewer.dnd.view.Viewer;
import org.apache.isis.viewer.dnd.view.Workspace;

public class ViewDragImpl extends DragImpl implements ViewDrag {
    private Location location;
    /**
     * Offset from the view's top-left corner to the pointer (relative to the
     * view).
     */
    private final Offset overlayOffset;
    private final View sourceView;
    private final View overlayView;
    private View targetView;
    private final Workspace viewsWorkspace;

    /**
     * Creates a new drag event. The source view has its pickup(), and then,
     * exited() methods called on it. The view returned by the pickup method
     * becomes this event overlay view, which is moved continuously so that it
     * tracks the pointer.
     * 
     * @param view
     *            the view over which the pointer was when this event started
     */
    public ViewDragImpl(final View view, final Offset offset, final View dragView) {
        this.sourceView = view;
        this.overlayView = dragView;
        this.overlayOffset = offset;

        viewsWorkspace = view.getWorkspace();
    }

    /**
     * getView().getAbsoluteLocation().getX(),
     * -getView().getAbsoluteLocation().getY() Cancel drag by changing cursor
     * back to pointer.
     */
    @Override
    public void cancel(final Viewer viewer) {
        getSourceView().getFeedbackManager().showDefaultCursor();
    }

    /**
     * Moves the overlay view so it follows the pointer
     */
    protected void drag(final Viewer viewer) {
        if (overlayView != null) {
            overlayView.markDamaged();
            updateDraggingLocation();
            overlayView.markDamaged();
        }
    }

    @Override
    public void drag(final View target, final Location location, final int mods) {
        this.location = location;
        if (overlayView != null) {
            overlayView.markDamaged();
            updateDraggingLocation();
            // this.location.subtract(target.getAbsoluteLocation());
            viewsWorkspace.getViewManager().getSpy().addTrace(target, "   over", getLocation());
            targetView = target;
            target.drag(this);
            overlayView.markDamaged();
        }
    }

    /**
     * Ends the drag by calling drop() on the workspace.
     */
    @Override
    public void end(final Viewer viewer) {
        viewer.clearAction();
        targetView.drop(this);
    }

    @Override
    public View getOverlay() {
        return overlayView;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public View getSourceView() {
        return sourceView;
    }

    @Override
    public Location getViewDropLocation() {
        final Location viewLocation = new Location(location);
        viewLocation.subtract(overlayOffset);
        return viewLocation;
    }

    public void subtract(final Location location) {
        location.subtract(location);
    }

    @Override
    public String toString() {
        return "ViewDrag [" + super.toString() + "]";
    }

    private void updateDraggingLocation() {
        final Location viewLocation = new Location(location);
        viewLocation.subtract(overlayOffset);
        overlayView.setLocation(viewLocation);
        overlayView.limitBoundsWithin(viewsWorkspace.getSize());
    }

    @Override
    public void subtract(final int x, final int y) {
        location.subtract(x, y);
    }

}
