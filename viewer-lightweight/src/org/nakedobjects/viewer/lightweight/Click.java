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


public class Click {
    private Location absolute;
    private Location location;
    private final boolean alt;
    private final boolean button1;
    private final boolean button2;
    private final boolean button3;
    private final boolean ctrl;
    private final boolean forView;
    private final boolean shift;

    /**
     *
     */
    public Click(View over, MouseEvent me) {
        int mods = me.getModifiers();
        button1 = (mods & MouseEvent.BUTTON1_MASK) > 0;
        button2 = (mods & MouseEvent.BUTTON2_MASK) > 0;
        button3 = (mods & MouseEvent.BUTTON3_MASK) > 0;

        shift = (mods & MouseEvent.SHIFT_MASK) > 0;
        alt = (mods & MouseEvent.ALT_MASK) > 0;
        ctrl = (mods & MouseEvent.CTRL_MASK) > 0;

        absolute = new Location(me.getPoint());
        location = new Location(absolute);

        Location viewLocation = over.getAbsoluteLocation();
        location.translate(-viewLocation.x, -viewLocation.y);

        forView = over.indicatesForView(location);
    }

    Click(Location absolute, Location location, final boolean alt, final boolean button1,
        final boolean button2, final boolean button3, final boolean ctrl, final boolean forView,
        final boolean shift) {
    	
        super();
        this.absolute = absolute;
        this.location = location;
        this.alt = alt;
        this.button1 = button1;
        this.button2 = button2;
        this.button3 = button3;
        this.ctrl = ctrl;
        this.forView = forView;
        this.shift = shift;
    }

    /**
      * Returns the absolute location of the mouse
      */
    public Location getAbsoluteLocation() {
        return absolute;
    }

    /**
     * Returns true if the 'Alt' key is depressed
     */
    public boolean isAlt() {
        return alt;
    }

    /**
     * Returns true if the left-hand button on the mouse is depressed
     */
    public boolean isButton1() {
        return button1;
    }

    /**
     * Returns true if the middle button on the mouse is depressed
     */
    public boolean isButton2() {
        return button2;
    }

    /**
     * Returns true if the right-hand button on the mouse is depressed
     */
    public boolean isButton3() {
        return button3;
    }

    /**
     * Returns true if the control key is depressed
     */
    public boolean isCtrl() {
        return ctrl;
    }

    /**
     * Returns true if the click is directed to the view itself, rather than to whatever the view is
     * representing.
     */
    public boolean isForView() {
        return forView;
    }

    /**
     * Returns the location of the mouse within the bounds of the view this object is recieved by
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Returns true if the shift key is depressed
     */
    public boolean isShift() {
        return shift;
    }

    public String toString() {
        String buttons = (button1 ? "^" : "-") + (button2 ? "^" : "-") + (button3 ? "^" : "-");
        String modifiers = (shift ? "S" : "-") + (alt ? "A" : "-") + (ctrl ? "C" : "-");

        return "Click [buttons=" + buttons + ",modifiers=" + modifiers + "]";
    }
}
