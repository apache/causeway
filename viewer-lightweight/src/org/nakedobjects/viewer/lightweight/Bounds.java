/*
        Naked Objects - a framework that exposes behaviourally complete
        business objects directly to the user.
        Copyright (C) 2000 - 2003  Naked Objects Group Ltd

        This program is free software; you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation; either version 2 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program; if not, write to the Free Software
        Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

        The authors can be contacted via www.nakedobjects.org (the
        registered address of Naked Objects Group is Kingsway House, 123 Goldworth
        Road, Woking GU21 1NR, UK).
*/
package org.nakedobjects.viewer.lightweight;


public class Bounds {
    int height;
    int width;
    int x;
    int y;

    public Bounds(int x, int y, int width, int height) {
        super();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Bounds() {
        x = 0;
        y = 0;
        width = 0;
        height = 0;
    }

    public Bounds(Location location, Size size) {
        this(location.x, location.y, size.width, size.height);
    }

    public Bounds(Size size) {
        this(0, 0, size.width, size.height);
    }

    public Bounds(Bounds bounds) {
        this(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void setHeight(int height) {
        this.height = height;
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

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return x;
    }

    public int getX2() {
        return x + width;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public int getY2() {
        return y + height;
    }

    public boolean contains(Location mousePosition) {
        int xp = mousePosition.getX();
        int yp = mousePosition.getY();
        int xMax = x + width;
        int yMax = y + height;

        return xp >= x && xp <= xMax && yp >= y && yp <= yMax;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Bounds) {
            Bounds b = (Bounds) obj;

            return b.x == x && b.y == y && b.width == width && b.height == height;
        }

        return false;
    }

    //Logger LOG = Logger.getLogger("Bounds"); 
    public boolean intersects(Bounds bounds) {
    	/*
    	LOG.debug(this);
		LOG.debug(bounds + " " + ((bounds.getX2() < bounds.x || bounds.getX2() > x) &&
		(getX2() < x || getX2() > bounds.x) &&
		(bounds.getY2() < bounds.y || bounds.getY2() > y) &&
		(getY2() < y || getY2() > bounds.y)));
    	
        return (bounds.getX2() < bounds.x || bounds.getX2() > x) &&
			(getX2() < x || getX2() > bounds.x) &&
			(bounds.getY2() < bounds.y || bounds.getY2() > y) &&
			(getY2() < y || getY2() > bounds.y);
			*/
    	
    	return true;
    }

    public String toString() {
        return x + "," + y + " " + width + "x" + height;
    }

    public Bounds union(Bounds bounds) {
        Bounds newBounds = new Bounds();
        newBounds.x = Math.min(x, bounds.x);
        newBounds.y = Math.min(y, bounds.y);
        newBounds.width = Math.max(x + width, bounds.x + bounds.width) - newBounds.x;
        newBounds.height = Math.max(y + height, bounds.y + bounds.height) - newBounds.y;

        return newBounds;
    }

	public void translate(int x, int y) {
		this.x += x;
		this.y += y;
	}

	public void grow(int x, int y) {
		this.width += x;
		this.height += y;
	}
}
