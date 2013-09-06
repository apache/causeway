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

import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Offset;
import org.apache.isis.viewer.dnd.drawing.Padding;
import org.apache.isis.viewer.dnd.view.InternalDrag;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Viewer;

public class SimpleInternalDrag extends DragImpl implements InternalDrag {
    private final Location location;
    // TODO replace Location with Offset
    private final Location offset;
    private final View view;

    /**
     * Creates a new drag event. The source view has its pickup(), and then,
     * exited() methods called on it. The view returned by the pickup method
     * becomes this event overlay view, which is moved continuously so that it
     * tracks the pointer,
     * 
     * @param view
     *            the view over which the pointer was when this event started
     * @param location
     *            the location within the viewer (the Frame/Applet/Window etc)
     * 
     *            TODO combine the two constructors
     */
    public SimpleInternalDrag(final View view, final Location location) {
        this.view = view;

        this.location = new Location(location);
        offset = view.getAbsoluteLocation();

        final Padding targetPadding = view.getPadding();
        final Padding containerPadding = view.getView().getPadding();
        offset.add(containerPadding.getLeft() - targetPadding.getLeft(), containerPadding.getTop() - targetPadding.getTop());

        this.location.subtract(offset);
    }

    public SimpleInternalDrag(final View view, final Offset off) {
        this.view = view;

        location = new Location();

        offset = new Location(off.getDeltaX(), off.getDeltaY());

        final Padding targetPadding = view.getPadding();
        final Padding containerPadding = view.getView().getPadding();
        offset.add(containerPadding.getLeft() - targetPadding.getLeft(), containerPadding.getTop() - targetPadding.getTop());

        this.location.subtract(offset);
    }

    @Override
    public void cancel(final Viewer viewer) {
        view.dragCancel(this);
    }

    @Override
    public void drag(final View target, final Location location, final int mods) {
        this.location.setX(location.getX());
        this.location.setY(location.getY());
        this.location.subtract(offset);
        view.drag(this);
    }

    @Override
    public void end(final Viewer viewer) {
        view.dragTo(this);
    }

    /**
     * Gets the location of the pointer relative to the view.
     */
    @Override
    public Location getLocation() {
        return new Location(location);
    }

    @Override
    public View getOverlay() {
        return null;
    }

    @Override
    public String toString() {
        final ToString s = new ToString(this, super.toString());
        s.append("location", location);
        s.append("relative", getLocation());
        return s.toString();
    }

}
