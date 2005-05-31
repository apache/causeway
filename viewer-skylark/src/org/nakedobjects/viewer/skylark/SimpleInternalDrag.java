package org.nakedobjects.viewer.skylark;

public class SimpleInternalDrag extends InternalDrag {

    private final Location location;
    private final Location offset;
    private final View view;

    /**
     * Creates a new drag event. The source view has its pickup(), and then,
     * exited() methods called on it. The view returned by the pickup method
     * becomes this event overlay view, which is moved continuously so that it
     * tracks the pointer,
     * 
     * @param view
     *                       the view over which the pointer was when this event started
     * @param location
     *                       the location within the viewer (the Frame/Applet/Window etc)
     */
    public SimpleInternalDrag(View view, Location location) {
        this.view = view;
 
        this.location = new Location(location);
        offset = view.getAbsoluteLocation();
        
        Padding targetPadding = view.getPadding();
        Padding containerPadding = view.getView().getPadding();
        offset.add(containerPadding.getLeft() - targetPadding.getLeft(), containerPadding.getTop() - targetPadding.getTop());
        
        this.location.subtract(offset);
    }

    protected void cancel(Viewer viewer) {
        view.dragCancel(this);
    }

    protected void drag(Viewer viewer, Location location, int mods) {
        this.location.x = location.x;
        this.location.y = location.y;
        this.location.subtract(offset);
        view.drag(this);
    }

    protected void end(Viewer viewer) {
        view.dragTo(this);
    }

    /**
     * Gets the location of the pointer relative to the view.
     */
    public Location getLocation() {
        return new Location(location);
    }

    public View getOverlay() {
        return null;
    }

    protected void start(Viewer viewer) {}
    
    public String toString() {
        return "InternalDrag [location=" + location + ",relative=" + getLocation() + "," + super.toString() + "]";
    }

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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