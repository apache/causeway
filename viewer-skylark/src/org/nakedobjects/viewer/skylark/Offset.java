package org.nakedobjects.viewer.skylark;

public class Offset {

    private int dx;
    private int dy;

    public Offset(Location locationInViewer, Location locationInView) {
        dx = locationInViewer.getX() - locationInView.getX();
        dy = locationInViewer.getY() - locationInView.getY();
    }

    public Offset(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public Offset(Location location) {
        this.dx = location.getX();
        this.dy = location.getY();
    }

    public int getDeltaX() {
        return dx;
    }

    public int getDeltaY() {
        return dy;
    }

    public Location offset(Location locationInViewer) {
        Location location = new Location(locationInViewer);
        location.move(dx, dy);
        return location;
    }

    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        
        if(obj instanceof Offset) {
            Offset offset;
            offset = (Offset) obj;
            return offset.dx == dx && offset.dy == dy;
        }
        
        return false;
    }
    
    public String toString() {
        return "Offset " + dx + ", " + dy;
    }

    public void add(int dx, int dy) {
        this.dx += dx;
        this.dy += dy;
    }

    public void subtract(int dx, int dy) {
        add(-dx, -dy);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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