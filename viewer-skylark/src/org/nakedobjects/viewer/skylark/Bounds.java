package org.nakedobjects.viewer.skylark;

import org.apache.log4j.Logger;

/**
 * Bounds represent a rectangular area on the screen.  The top-left corner is represented by the location 
 * (available using getLocation(), and getX() and getY()).  The extent of the bounds is specified by 
 * its height and width (available using getHeight() and getWidth()).  The bottom-right point is the offset from 
 * the top-left point by width -1 and hieght - 1 pixels.
 * 
 * For example a bounds created as follows
 * 
 *   new Bounds(5, 10, 10, 20)
 * 
 * Would represent a rectangle at location (5, 10), with a width of 10 pixels and a height of 20.  Note, hower 
 * that the lower-right corner would be at (14, 29), as there are 10 pixels between pixel 5 and pixel 14, 
 * and 20 between 10 and 29. 
 */
public class Bounds {
    int height;

    Logger LOG = Logger.getLogger("Bounds"); 
    int width;
    int x;
    int y;

    public Bounds() {
        x = 0;
        y = 0;
        width = 0;
        height = 0;
    }

    public Bounds(Bounds bounds) {
        this(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public Bounds(int x, int y, int width, int height) {
        super();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Bounds(Location location, Size size) {
        this(location.x, location.y, size.width, size.height);
    }

    public Bounds(Size size) {
        this(0, 0, size.width, size.height);
    }

    public boolean contains(Location location) {
        int xp = location.getX();
        int yp = location.getY();
        int xMax = x + width - 1;
        int yMax = y + height - 1;

        return xp >= x && xp <= xMax && yp >= y && yp <= yMax;
    }

    public void contract(int width, int height) {
        this.width -= width;
        this.height -= height;
    }

	public void contract(Padding padding) {
		height -= padding.top + padding.bottom;
		width -= padding.left + padding.right;
		x += padding.left;
		y += padding.top;
	}

    public void contract(Size size) {
        this.width -= size.width;
        this.height -= size.height;
    }

    public void contractHeight(int height) {
        this.height -= height;
    }

    public void contractWidth(int width) {
        this.width -= width;
    }

    public void ensureHeight(int height) {
        this.height = Math.max(this.height, height);
    }

    public void ensureWidth(int width) {
        this.width = Math.max(this.width, width);
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
    
    public void extend(int width, int height) {
        this.width += width;
        this.height += height;
    }
    
	public void extend(Padding padding) {
        this.width += padding.getLeftRight();
        this.height += padding.getTopBottom();
	}

    public void extend(Size size) {
        this.width += size.width;
        this.height += size.height;
    }

    public void extendHeight(int height) {
        this.height += height;
    }

    public void extendWidth(int width) {
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
     * Determines whether this bounds overlaps the specified bounds.  If any area is shared by the two bounds
     * then this will return true.  As the edges of the bounds are of a finite size the bounds overlap if any of the 
     * edges overlap. 
     */
    public boolean intersects(Bounds bounds) {
        int tx1 = this.x;
        int tx2 = this.x + this.width - 1;
        int ox1 = bounds.x;
        int ox2 = bounds.x + bounds.width - 1;
       
      //  tx1 < ox1 < tx2 || tx1 < ox2 < tx2
        boolean xOverlap = (tx1 <= ox1 && ox1 <= tx2) || (tx1 <= ox2 && ox1 <= tx2) || (ox1 <= tx1 && tx1 <= ox2) || (ox1 <= tx2 && tx1 <= ox2);
  
        int ty1 = this.y;
        int ty2 = this.y + this.height - 1;
        int oy1 = bounds.y;
        int oy2 = bounds.y + bounds.height - 1;     
        boolean yOverlap = (ty1 <= oy1 && oy1 <= ty2) || (ty1 <= oy2 && oy1 <= ty2) || (oy1 <= ty1 && ty1 <= oy2) || (oy1 <= ty2 && ty1 <= oy2);
        return xOverlap && yOverlap;
    	/*
    	LOG.debug(this);
		LOG.debug(bounds + " " + ((bounds.getX2() < bounds.y || bounds.getX2() > x) &&
		(getX2() < x || getX2() > bounds.x) &&
		(bounds.getY2() < bounds.y || bounds.getY2() > y) &&
		(getY2() < y || getY2() > bounds.y)));
    	
        return (bounds.getX2() < bounds.x || bounds.getX2() > x) &&
			(getX2() < x || getX2() > bounds.x) &&
			(bounds.getY2() < bounds.y || bounds.getY2() > y) &&
			(getY2() < y || getY2() > bounds.y);
			*/
    	
    //	return true;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String toString() {
        return x + "," + y + " " + width + "x" + height;
    }

	public void translate(int x, int y) {
		this.x += x;
		this.y += y;
	}

    public Bounds union(Bounds bounds) {
        Bounds newBounds = new Bounds();
        newBounds.x = Math.min(x, bounds.x);
        newBounds.y = Math.min(y, bounds.y);
        newBounds.width = Math.max(x + width, bounds.x + bounds.width) - newBounds.x;
        newBounds.height = Math.max(y + height, bounds.y + bounds.height) - newBounds.y;

        return newBounds;
    }

}


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