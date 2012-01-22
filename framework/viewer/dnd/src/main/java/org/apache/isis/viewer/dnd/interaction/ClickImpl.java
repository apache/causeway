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
import org.apache.isis.viewer.dnd.view.Click;

/**
 * Describes a mouse click event.
 */
public class ClickImpl extends PointerEvent implements Click {
    private final Location location;
    private final Location locationWithinViewer;

    /**
     * Creates a new click event object.
     * 
     * @param mouseLocation
     *            the location of the mouse relative to the viewer
     * @param modifiers
     *            the button and key held down during the click (@see
     *            java.awt.event.MouseEvent)
     */
    public ClickImpl(final Location mouseLocation, final int modifiers) {
        super(modifiers);

        this.location = new Location(mouseLocation);
        this.locationWithinViewer = new Location(mouseLocation);
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Location getLocationWithinViewer() {
        return locationWithinViewer;
    }

    /**
     * Translate the location of this event by the specified offset.
     */
    @Override
    public void subtract(final int x, final int y) {
        location.subtract(x, y);
    }

    @Override
    public String toString() {
        return "Click [location=" + location + "," + super.toString() + "]";
    }

    public void add(final Offset offset) {
        location.add(offset.getDeltaX(), offset.getDeltaY());
    }

    public void subtract(final Offset offset) {
        subtract(offset.getDeltaX(), offset.getDeltaY());
    }

    @Override
    public void subtract(final Location location) {
        subtract(location.getX(), location.getY());
    }
}
