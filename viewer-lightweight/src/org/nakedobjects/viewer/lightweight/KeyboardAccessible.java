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

public interface KeyboardAccessible {
    /**
     * Determines if the user is able to change the held value.
     */
    boolean canChangeValue();

    boolean canFocus();

    /**
     * Indicates that editing has been completed and the entry should be saved.  Will be called by the
     * view manager when other action place within the parent.
     *
     */
    void editComplete();

    void focusLost();

    void focusRecieved();

    boolean hasFocus();

    /**
     * Called when the user presses any key on the keyboard while this view has
     * the focus.
     * @param keyCode
     * @param modifiers
     */
    void keyPressed(final int keyCode, final int modifiers);

    /**
     * Called when the user releases any key on the keyboard while this view has
     * the focus.
     * @param keyCode
     * @param modifiers
     */
    void keyReleased(int keyCode, int modifiers);

    /**
     * Called when the user presses a non-control key (i.e. data entry keys and
     * not shift, up-arrow etc).  Such a key press will result in a prior call
     * to <code>keyPressed</code> and a subsequent call to <code>keyReleased</code>.
     */
    void keyTyped(char keyCode);

    void redraw();
}
