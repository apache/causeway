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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bounds represent a rectangular area on the screen. The top-left corner is
 * represented by the location (available using getLocation(), and getX() and
 * getY()). The extent of the bounds is specified by its height and width
 * (available using getHeight() and getWidth()). The bottom-right point is the
 * offset from the top-left point by width -1 and hieght - 1 pixels.
 * 
 * For example a bounds created as follows
 * 
 * new Bounds(5, 10, 10, 20)
 * 
 * Would represent a rectangle at location (5, 10), with a width of 10 pixels
 * and a height of 20. Note, hower that the lower-right corner would be at (14,
 * 29), as there are 10 pixels between pixel 5 and pixel 14, and 20 between 10
 * and 29.
 */
public class Bounds {
    Logger LOG = LoggerFactory.getLogger("Bounds");
    int x;
    int y;
    int height;
    int width;

    public Bounds() {
        x = 0;
        y = 0;
        width = 0;
        height = 0;
    }

    public Bounds(final Bounds bounds) {
        this(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public Bounds(final int x, final int y, final int width, final int height) {
        super();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Bounds(final Location location, final Size size) {
        this(location.x, location.y, size.width, size.height);
    }

    public Bounds(final Size size) {
        this(0, 0, size.width, size.height);
    }

    public boolean contains(final Location location) {
        final int xp = location.getX();
        final int yp = location.getY();
        final int xMax = x + width - 1;
        final int yMax = y + height - 1;

        return xp >= x && xp <= xMax && yp >= y && yp <= yMax;
    }

    public void contract(final int width, final int height) {
        this.width -= width;
        this.height -= height;
    }

    public void contract(final Padding padding) {
        height -= padding.top + padding.bottom;
        width -= padding.left + padding.right;
        x += padding.left;
        y += padding.top;
    }

    public void contract(final Size size) {
        this.width -= size.width;
        this.height -= size.height;
    }

    public void contractHeight(final int height) {
        this.height -= height;
    }

    public void contractWidth(final int width) {
        this.width -= width;
    }

    public void ensureHeight(final int height) {
        this.height = Math.max(this.height, height);
    }

    public void ensureWidth(final int width) {
        this.width = Math.max(this.width, width);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Bounds) {
            final Bounds b = (Bounds) obj;

            return b.x == x && b.y == y && b.width == width && b.height == height;
        }

        return false;
    }

    public void extend(final int width, final int height) {
        this.width += width;
        this.height += height;
    }

    public void extend(final Padding padding) {
        this.width += padding.getLeftRight();
        this.height += padding.getTopBottom();
    }

    public void extend(final Size size) {
        this.width += size.width;
        this.height += size.height;
    }

    public void extendHeight(final int height) {
        this.height += height;
    }

    public void extendWidth(final int width) {
        this.width += width;
    }

    public int getHeight() {
        return height;
    }

    public Location getLocation() {
        return new Location(x, y);
    }

    public Size getSize() {
        return new Size(width, height);
    }

    public int getWidth() {
        return width;
    }

    public int getX() {
        return x;
    }

    public int getX2() {
        return x + width - 1;
    }

    public int getY() {
        return y;
    }

    public int getY2() {
        return y + height - 1;
    }

    /**
     * Determines whether this bounds overlaps the specified bounds. If any area
     * is shared by the two bounds then this will return true. As the edges of
     * the bounds are of a finite size the bounds overlap if any of the edges
     * overlap.
     */
    public boolean intersects(final Bounds bounds) {
        final int tx1 = this.x;
        final int tx2 = this.x + this.width - 1;
        final int ox1 = bounds.x;
        final int ox2 = bounds.x + bounds.width - 1;

        // tx1 < ox1 < tx2 || tx1 < ox2 < tx2
        final boolean xOverlap = (tx1 <= ox1 && ox1 <= tx2) || (tx1 <= ox2 && ox1 <= tx2) || (ox1 <= tx1 && tx1 <= ox2) || (ox1 <= tx2 && tx1 <= ox2);

        final int ty1 = this.y;
        final int ty2 = this.y + this.height - 1;
        final int oy1 = bounds.y;
        final int oy2 = bounds.y + bounds.height - 1;
        final boolean yOverlap = (ty1 <= oy1 && oy1 <= ty2) || (ty1 <= oy2 && oy1 <= ty2) || (oy1 <= ty1 && ty1 <= oy2) || (oy1 <= ty2 && ty1 <= oy2);
        return xOverlap && yOverlap;

    }

    public void limitLocation(final Size bounds) {
        if (x + width > bounds.width) {
            x = bounds.width - width;
        }
        if (y + height > bounds.height) {
            y = bounds.height - height;
        }
    }

    /**
     * Limits the specified bounds so that it fits within this bounds.
     */
    public boolean limitBounds(final Bounds toLimit) {
        boolean limited = false;
        final Location location = toLimit.getLocation();
        final Size size = toLimit.getSize();

        int viewLeft = location.getX();
        int viewTop = location.getY();
        int viewRight = viewLeft + size.getWidth();
        int viewBottom = viewTop + size.getHeight();

        final Size wd = getSize();

        final int limitLeft = x;
        final int limitTop = y;
        final int limitRight = x + width;
        final int limitBottom = y + height;

        if (viewRight > limitRight) {
            viewLeft = limitRight - size.getWidth();
            limited = true;
            LOG.info("right side oustide limits, moving left to " + viewLeft);
        }

        if (viewLeft < limitLeft) {
            viewLeft = limitLeft;
            limited = true;
            LOG.info("left side outside limit, moving left to " + viewLeft);
        }

        if (viewBottom > limitBottom) {
            viewTop = limitBottom - size.getHeight();
            limited = true;
            LOG.info("bottom outside limit, moving top to " + viewTop);
        }

        if (viewTop < limitTop) {
            viewTop = limitTop;
            limited = true;
            LOG.info("top outside limit, moving top to " + viewTop);
        }

        toLimit.setX(viewLeft);
        toLimit.setY(viewTop);

        viewBottom = viewTop + size.getHeight();
        viewRight = viewLeft + size.getWidth();

        if (viewRight > limitRight) {
            toLimit.width = wd.width;
            limited = true;
            LOG.info("width outside limit, reducing width to " + viewTop);
        }

        if (viewBottom > limitBottom) {
            toLimit.height = wd.height;
            limited = true;
            LOG.info("height outside limit, reducing height to " + viewTop);
        }

        if (limited) {
            LOG.info("limited " + toLimit);
        }
        return limited;
    }

    public void setBounds(final Bounds bounds) {
        x = bounds.x;
        y = bounds.y;
        width = bounds.width;
        height = bounds.height;

    }

    public void setHeight(final int height) {
        this.height = height;
    }

    public void setWidth(final int width) {
        this.width = width;
    }

    public void setX(final int x) {
        this.x = x;
    }

    public void setY(final int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return x + "," + y + " " + width + "x" + height;
    }

    public void translate(final int x, final int y) {
        this.x += x;
        this.y += y;
    }

    public void union(final Bounds bounds) {
        final int newX = Math.min(x, bounds.x);
        final int newY = Math.min(y, bounds.y);
        width = Math.max(x + width, bounds.x + bounds.width) - newX;
        height = Math.max(y + height, bounds.y + bounds.height) - newY;
        x = newX;
        y = newY;
    }

}
