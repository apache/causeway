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

public class Size {
    public static final Size createMax() {
        return new Size(10000, 10000);
    }

    int height;
    int width;

    public Size() {
        width = 0;
        height = 0;
    }

    public Size(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    public Size(final Size size) {
        width = size.width;
        height = size.height;
    }

    public void contract(final int width, final int height) {
        this.width -= width;
        this.height -= height;
    }

    public void contract(final Size size) {
        this.width -= size.width;
        this.height -= size.height;
    }

    public void contractHeight(final int height) {
        this.height -= height;
    }

    public void contract(final Padding padding) {
        height -= padding.top + padding.bottom;
        width -= padding.left + padding.right;
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

        if (obj instanceof Size) {
            final Size object = (Size) obj;

            return object.width == this.width && object.height == this.height;
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

    public int getWidth() {
        return width;
    }

    /**
     * Limits the height of this Size so that if its height is greater than the
     * specified maximum then the height is reduced to the maximum. If the
     * height is less than maximum (or equal to it) then nothing is done.
     */
    public void limitHeight(final int maximum) {
        height = Math.min(height, maximum);
    }

    /**
     * Limits the width of this Size so that if its width is greater than the
     * specified maximum then the width is reduced to the maximum. If the width
     * is less than maximum (or equal to it) then nothing is done.
     */
    public void limitWidth(final int maximum) {
        width = Math.min(width, maximum);
    }

    /**
     * Limits the width and height of this Size so it is no larger than the
     * specified maximum size.
     * 
     * @see #limitWidth(int)
     * @see #limitHeight(int)
     */
    public void limitSize(final Size maximum) {
        limitWidth(maximum.width);
        limitHeight(maximum.height);
    }

    public void setHeight(final int height) {
        this.height = height;
    }

    public void setWidth(final int width) {
        this.width = width;
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }

}
