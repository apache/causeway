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

public class Shape {
    int count = 0;
    int[] x = new int[6];
    int[] y = new int[6];

    public Shape() {
    }

    public Shape(final int xOrigin, final int yOrigin) {
        this.x[0] = xOrigin;
        this.y[0] = yOrigin;
        count = 1;
    }

    public Shape(final Shape shape) {
        count = shape.count;
        this.x = new int[count];
        this.y = new int[count];
        for (int i = 0; i < count; i++) {
            this.x[i] = shape.x[i];
            this.y[i] = shape.y[i];
        }
    }

    public void addVector(final int width, final int height) {
        final int x = this.x[count - 1] + width;
        final int y = this.y[count - 1] + height;
        addPoint(x, y);
    }

    public void addPoint(final int x, final int y) {
        if (this.x.length == count) {
            final int[] newX = new int[count * 2];
            final int[] newY = new int[count * 2];
            System.arraycopy(this.x, 0, newX, 0, count);
            System.arraycopy(this.y, 0, newY, 0, count);
            this.x = newX;
            this.y = newY;
        }
        this.x[count] = x;
        this.y[count] = y;
        count++;
    }

    public int count() {
        return count;
    }

    public int[] getX() {
        final int[] xx = new int[count];
        System.arraycopy(x, 0, xx, 0, count);
        return xx;
    }

    public int[] getY() {
        final int[] yy = new int[count];
        System.arraycopy(y, 0, yy, 0, count);
        return yy;
    }

    @Override
    public String toString() {
        final StringBuffer points = new StringBuffer();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                points.append("; ");
            }
            points.append(this.x[i]);
            points.append(",");
            points.append(this.y[i]);
        }
        return "Shape {" + points + "}";
    }

    public void translate(final int x, final int y) {
        for (int i = 0; i < count; i++) {
            this.x[i] += x;
            this.y[i] += y;
        }
    }
}
