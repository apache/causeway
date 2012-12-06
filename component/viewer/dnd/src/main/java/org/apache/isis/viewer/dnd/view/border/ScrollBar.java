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

package org.apache.isis.viewer.dnd.view.border;

public class ScrollBar {
    private int maximum;
    private int minimum;
    private int scrollPosition = 0;
    private int visibleAmount;

    public ScrollBar() {
        super();
    }

    public void setPostion(final int position) {
        scrollPosition = Math.min(position, maximum);
        scrollPosition = Math.max(scrollPosition, minimum);
    }

    public void firstClick(final int x, final boolean alt) {
        if (alt) {
            setPostion(x - visibleAmount / 2);
        } else {
            if (x < scrollPosition) {
                setPostion(scrollPosition - visibleAmount);
            } else if (x > scrollPosition + visibleAmount) {
                setPostion(scrollPosition + visibleAmount);
            }
        }
    }

    public int getMaximum() {
        return maximum;
    }

    public int getMinimum() {
        return minimum;
    }

    public int getPosition() {
        return scrollPosition;
    }

    public int getVisibleAmount() {
        return visibleAmount;
    }

    public void limit() {
        if (scrollPosition > maximum) {
            scrollPosition = maximum;
        }
    }

    public void reset() {
        scrollPosition = 0;
    }

    public boolean isOnThumb(final int pos) {
        return pos > scrollPosition && pos < scrollPosition + visibleAmount;
    }

    public void setSize(final int viewportSize, final int contentSize) {
        visibleAmount = contentSize == 0 ? 0 : (viewportSize * viewportSize / contentSize);
        maximum = viewportSize - visibleAmount;
    }

    public void secondClick(final int y) {
        final int midpoint = (maximum + visibleAmount) / 2;
        setPostion(y < midpoint ? minimum : maximum);
    }
}
