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

public class Location {
    int x;
    int y;

    public Location() {
        x = 0;
        y = 0;
    }

    public Location(final int x, final int y) {
        super();
        this.x = x;
        this.y = y;
    }

    public Location(final Location location) {
        x = location.x;
        y = location.y;
    }

    public void add(final int x, final int y) {
        move(x, y);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Location) {
            final Location object = (Location) obj;

            return object.x == this.x && object.y == this.y;
        }

        return false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void move(final int dx, final int dy) {
        x += dx;
        y += dy;
    }

    public Offset offsetFrom(final Location location) {

        Offset offset;
        offset = new Offset(x - location.x, y - location.y);
        return offset;
    }

    public void setX(final int x) {
        this.x = x;
    }

    public void setY(final int y) {
        this.y = y;
    }

    public void subtract(final int x, final int y) {
        move(-x, -y);
    }

    public void subtract(final Location location) {
        move(-location.x, -location.y);
    }

    public void subtract(final Offset offset) {
        move(-offset.getDeltaX(), -offset.getDeltaY());
    }

    @Override
    public String toString() {
        return x + "," + y;
    }

    public void translate(final Location offset) {
        move(offset.x, offset.y);
    }

    public void translate(final Offset offset) {
        move(offset.getDeltaX(), offset.getDeltaY());
    }
}
