package org.nakedobjects.viewer.skylark;

/**
 * Details a drag event - from drag start to drop,
 */
public abstract class Drag extends PointerEvent {
    /**
     * Creates a new drag event object.
     * @param source
     *                       the view over which the pointer was when this event started
     * @param mods
     *                       the button and key modifiers (@see java.awt.event.MouseEvent)
     */
    protected Drag(View source, int mods) {
        super(source, mods);
     }

    /**
     * Indicates the drag has been cancelled; no action should be taken.
     */
    protected abstract void cancel();

    /**
     * Indicates that the drag state has changed.
     */
    protected abstract void drag();

    /**
     * Indicates the drag has properly ended (the mouse button has been
     * released)
     *  
     */
    protected abstract void end();
 
    /**
     * Translate the location of this event by the specified offset.
     */
    public abstract void subtract(int x, int y);

    public void add(Offset offset) {
        subtract(-offset.getDeltaX(), -offset.getDeltaY());
    }

    /**
     * Updates the location with the viewer to reflect the mouse pointer's
     * position.
      */
    abstract void update(Location mouseLocation, View target);
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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
