/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
package org.nakedobjects.viewer.skylark;

public class Shape {
    int count = 0;

    int[] x = new int[6];

    int[] y = new int[6];

    public Shape() {}

    public Shape(int xOrigin, int yOrigin) {
        this.x[0] = xOrigin;
        this.y[0] = yOrigin;
        count = 1;
    }

    public Shape(Shape shape) {
        count = shape.count;
        this.x = new int[count];
        this.y = new int[count];
        for (int i = 0; i < count; i++) {
            this.x[i] = shape.x[i];
            this.y[i] = shape.y[i];
        }
    }

    public void addLine(int width, int height) {
        int x = this.x[count - 1] + width;
        int y = this.y[count - 1] - height;
        addVertex(x, y);
    }

    public void addVertex(int x, int y) {
        this.x[count] = x;
        this.y[count] = y;
        count++;
    }

    public int count() {
        return count;
    }

    public int[] getX() {
        int[] xx = new int[count];
        System.arraycopy(x, 0, xx, 0, count);

        return xx;
    }

    public int[] getY() {
        int[] yy = new int[count];
        System.arraycopy(y, 0, yy, 0, count);

        return yy;
    }

    public String toString() {
        StringBuffer points = new StringBuffer();
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

    public void translate(int x, int y) {
        for (int i = 0; i < count; i++) {
            this.x[i] += x;
            this.y[i] += y;
        }
    }
}