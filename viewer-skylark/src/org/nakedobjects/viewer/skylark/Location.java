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
package org.nakedobjects.viewer.skylark;

import java.awt.Point;


public class Location {
    int x;
    int y;

    public Location(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    public Location(Location location) {
        x = location.x;
        y = location.y;
    }

    public Location() {
        x = 0;
        y = 0;
    }

    Location(Point point) {
        x = point.x;
        y = point.y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Location) {
            Location object = (Location) obj;

            return object.x == this.x && object.y == this.y;
        }

        return false;
    }

    public String toString() {
        return x + "," + y;
    }

    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

	public void translate(Location offset) {
		move(offset.x, offset.y);
	}

    public void translate(Offset offset) {
        move(offset.getDeltaX(), offset.getDeltaY());
    }

    public Offset offsetFrom(Location location) {
        
        Offset offset;
        offset = new Offset(x - location.x, y - location.y);
        return offset;
    }

    public void subtract(Offset offset) {
        move(-offset.getDeltaX(), -offset.getDeltaY());
    }

    public void subtract(Location location) {
        move(-location.x, -location.y);    
    }

    public void add(Offset offset) {
        move(offset.getDeltaX(), offset.getDeltaY());
    }

    public void subtract(int x, int y) {
        move(-x, -y);    
    }

    public void add(int x, int y) {
        move(x, y);        
    }
}
