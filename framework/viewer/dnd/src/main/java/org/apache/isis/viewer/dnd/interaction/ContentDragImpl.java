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
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.ContentDrag;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Viewer;
import org.apache.isis.viewer.dnd.view.Workspace;

public class ContentDragImpl extends DragImpl implements ContentDrag {
    private final View dragView;
    private Location location;
    private View previousTarget;
    private final Content sourceContent;
    private View target;
    private final Workspace workspace;
    private final Location offset;
    private final View source;

    /**
     * Creates a new drag event. The source view has its pickup(), and then,
     * exited() methods called on it. The view returned by the pickup method
     * becomes this event overlay view, which is moved continuously so that it
     * tracks the pointer,
     * 
     * @param source
     *            the view over which the pointer was when this event started
     */
    public ContentDragImpl(final View source, final Location offset, final View dragView) {
        if (dragView == null) {
            throw new NullPointerException();
        }
        workspace = source.getWorkspace();
        sourceContent = source.getContent();
        this.dragView = dragView;
        this.offset = offset;
        this.source = source.getView();
    }

    /**
     * Cancels drag by calling dragOut() on the current target, and changes the
     * cursor back to the default.
     */
    @Override
    public void cancel(final Viewer viewer) {
        if (target != null) {
            target.dragOut(this);
        }
        viewer.clearAction();
    }

    @Override
    public void drag(final View target, final Location location, final int mods) {
        this.location = location;
        this.target = target;
        this.mods = mods;

        moveDragView();
        crossBoundary(target);
        target.drag(this);
    }

    private void crossBoundary(final View target) {
        if (target != previousTarget) {
            if (previousTarget != null) {
                previousTarget.dragOut(this);
                previousTarget = null;
            }

            target.dragIn(this);
            previousTarget = target;
        }
    }

    private void moveDragView() {
        if (dragView != null) {
            dragView.markDamaged();
            final Location newLocation = new Location(this.location);
            newLocation.subtract(offset);
            dragView.setLocation(newLocation);
            dragView.limitBoundsWithin(workspace.getSize());
            dragView.markDamaged();
        }
    }

    /**
     * Ends the drag by calling drop() on the current target, and changes the
     * cursor back to the default.
     */
    @Override
    public void end(final Viewer viewer) {
        viewer.getSpy().addAction("drop on " + target);
        target.drop(this);
        viewer.clearAction();
    }

    @Override
    public View getOverlay() {
        return dragView;
    }

    @Override
    public View getSource() {
        return source;
    }

    /**
     * Returns the Content object from the source view.
     */
    @Override
    public Content getSourceContent() {
        return sourceContent;
    }

    @Override
    public Location getTargetLocation() {
        final Location location = new Location(this.location);
        location.subtract(target.getAbsoluteLocation());
        // location.add(-getOffset().getX(), -getOffset().getY());
        // location.add(-getOffset().getX(), -getOffset().getY());

        return location;
    }

    @Override
    public Location getOffset() {
        return offset;
    }

    /**
     * Returns the current target view.
     */
    @Override
    public View getTargetView() {
        return target;
    }

    @Override
    public String toString() {
        return "ContentDrag [" + super.toString() + "]";
    }

    @Override
    public void subtract(final int left, final int top) {
    }
}
