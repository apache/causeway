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
import org.apache.isis.viewer.dnd.view.DragStart;

public class DragStartImpl extends PointerEvent implements DragStart {
    private final Location location;

    public DragStartImpl(final Location location, final int mods) {
        super(mods);
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void subtract(final Location location) {
        this.location.subtract(location);
    }

    @Override
    public void subtract(final int x, final int y) {
        location.subtract(x, y);
    }

    public void add(final Offset offset) {
        location.add(offset.getDeltaX(), offset.getDeltaY());
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("location", location);
        str.append("buttons", super.toString());
        return str.toString();
    }
}
