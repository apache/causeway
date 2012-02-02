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

public class Padding {
    int bottom;
    int left;
    int right;
    int top;

    public Padding(final int top, final int left, final int bottom, final int right) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

    public Padding() {
        top = 0;
        bottom = 0;
        left = 0;
        right = 0;
    }

    public Padding(final Padding padding) {
        this.top = padding.top;
        this.bottom = padding.bottom;
        this.left = padding.left;
        this.right = padding.right;
    }

    public void setBottom(final int bottom) {
        this.bottom = bottom;
    }

    public int getBottom() {
        return bottom;
    }

    public void setLeft(final int left) {
        this.left = left;
    }

    public int getLeft() {
        return left;
    }

    public int getLeftRight() {
        return left + right;
    }

    public void setRight(final int right) {
        this.right = right;
    }

    public int getRight() {
        return right;
    }

    public void setTop(final int top) {
        this.top = top;
    }

    public int getTop() {
        return top;
    }

    public int getTopBottom() {
        return top + bottom;
    }

    /**
     * Extend the padding on the bottom by the specified amount.
     */
    public void extendBottom(final int pad) {
        bottom += pad;
    }

    /**
     * Extend the padding on the left by the specified amount.
     */
    public void extendLeft(final int pad) {
        left += pad;
    }

    /**
     * Extend the padding on the right by the specified amount.
     */
    public void extendRight(final int pad) {
        right += pad;
    }

    /**
     * Extend the padding on the top by the specified amount.
     */
    public void extendTop(final int pad) {
        top += pad;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Padding) {
            final Padding object = (Padding) obj;

            return object.top == this.top && object.bottom == this.bottom && object.left == this.left && object.right == this.right;
        }

        return false;
    }

    @Override
    public String toString() {
        return "Padding [top=" + top + ",bottom=" + bottom + ",left=" + left + ",right=" + right + "]";
    }
}
