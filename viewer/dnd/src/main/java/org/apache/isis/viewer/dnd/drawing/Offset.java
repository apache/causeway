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

package org.apache.isis.viewer.dnd.drawing;

public class Offset {

    private int dx;
    private int dy;

    public Offset(final Location locationInViewer, final Location locationInView) {
        dx = locationInViewer.getX() - locationInView.getX();
        dy = locationInViewer.getY() - locationInView.getY();
    }

    public Offset(final int dx, final int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public Offset(final Location location) {
        this.dx = location.getX();
        this.dy = location.getY();
    }

    public int getDeltaX() {
        return dx;
    }

    public int getDeltaY() {
        return dy;
    }

    public Location offset(final Location locationInViewer) {
        final Location location = new Location(locationInViewer);
        location.move(dx, dy);
        return location;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Offset) {
            Offset offset;
            offset = (Offset) obj;
            return offset.dx == dx && offset.dy == dy;
        }

        return false;
    }

    @Override
    public String toString() {
        return "Offset " + dx + ", " + dy;
    }

    public void add(final int dx, final int dy) {
        this.dx += dx;
        this.dy += dy;
    }

    public void subtract(final int dx, final int dy) {
        add(-dx, -dy);
    }
}
