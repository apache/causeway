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

import java.awt.event.MouseEvent;


/**
 * Wraps mouse events that occur within the ApplicationFrame before passing them to the
 * frames components.
 *
 */
public abstract class DragHandler {
    protected DragView dragging;
    private final Location absoluteOrigin;
    private final Size relativeDifference;
    private final Size viewOffset;
    private Location absoluteLocation;
    private final boolean alt;
    private final boolean button1;
    private final boolean button2;
    private final boolean button3;
    private final boolean ctrl;
    private final boolean meta;
    private final boolean shift;

    protected DragHandler(View source, MouseEvent me, Location downAt) {
        // locations
        absoluteOrigin = new Location(downAt);
        absoluteLocation = new Location(me.getPoint());

        Location offsetFrom = source.getAbsoluteLocation();
        relativeDifference = new Size(offsetFrom.x - (absoluteLocation.x - absoluteOrigin.x),
                offsetFrom.y - (absoluteLocation.y - absoluteOrigin.y));

        Location pos = source.getAbsoluteLocation();
        viewOffset = new Size(downAt.x - pos.x, downAt.y - pos.y);

        //        locationInView = new Location(dragOffset.width, dragOffset.height);
        // button details
        int mods = me.getModifiers();
        button1 = (mods & MouseEvent.BUTTON1_MASK) > 0;
        button2 = (mods & MouseEvent.BUTTON2_MASK) > 0;
        button3 = (mods & MouseEvent.BUTTON3_MASK) > 0;

        shift = (mods & MouseEvent.SHIFT_MASK) > 0;
        alt = (mods & MouseEvent.ALT_MASK) > 0;
        meta = (mods & MouseEvent.META_MASK) > 0;
        ctrl = (mods & MouseEvent.CTRL_MASK) > 0;

        Location p = new Location(downAt);
        p.translate(-offsetFrom.x, -offsetFrom.y);
    }

    /**
     * Determines whether the ALT key is depressed.
     * @return boolean
     */
    public boolean isAlt() {
        return alt;
    }

    /**
     * @return boolean
     */
    public boolean isAlts() {
        return meta;
    }

    /**
     * Determines whether the left mouse button is depressed.
     * @return boolean
     */
    public boolean isButton1() {
        return button1;
    }

    /**
     * Determines whether the middle mouse button is depressed.
     * @return boolean
     */
    public boolean isButton2() {
        return button2;
    }

    /**
     * Determines whether the right mouse button is depressed.
     * @return boolean
     */
    public boolean isButton3() {
        return button3;
    }

    /**
     * Determines whether the CTRL key is depressed.
     * @return boolean
     */
    public boolean isCtrl() {
        return ctrl;
    }

    /**
    *
    */
    public DragView getDragging() {
        return dragging;
    }

    public Location getRelativeLocation() {
        Location at = new Location(absoluteLocation);
        at.translate(-relativeDifference.width, -relativeDifference.height);

        return at;
    }

    /**
     * Returns the location of the mouse within the view it is over.  Returns null
     * if not over a view.
     */
    public Location getRelativeOrigin() {
        Location at = new Location(absoluteOrigin);
        at.translate(-relativeDifference.width, -relativeDifference.height);

        return at;
    }

    /**
     * Determines whether the SHIFT key is depressed.
     * @return boolean
     */
    public boolean isShift() {
        return shift;
    }

    /**
     * Returns the location of the view that the mouse is over.
     */
    public Location getViewLocation() {
        Location at = new Location(absoluteLocation);
        at.translate(-viewOffset.width, -viewOffset.height);

        return at;
    }

    public abstract void dragEnd(View identified);

    public abstract void dragIn(View over);

    public abstract void dragOut(View over);

    public String toString() {
        Location relative = new Location(absoluteLocation);
        relative.translate(relativeDifference.width, relativeDifference.height);

        return "Mouse [location=(" + relative.x + "." + relative.y + "),frameLocation=(" +
        absoluteLocation.x + "." + absoluteLocation.y + ")]";
    }

    /**
     * Updates the absolute location to reflect the mouse pointer's position.
     * @param me
     */
    void update(MouseEvent me) {
        absoluteLocation.x = me.getX();
        absoluteLocation.y = me.getY();
    }
}
