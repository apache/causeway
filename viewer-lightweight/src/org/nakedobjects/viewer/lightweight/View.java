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

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.reflect.Field;


public interface View extends Cloneable {
    /**
     * the location of this view relative to the containing java.awt.Window object.
     * @return Location
     */
    Location getAbsoluteLocation();

    int getBaseline();

    void setBorder(Border border);

    Border getBorder();

    void setBounds(Location point, Size size);

    /**
     * Returns the bounding rectangle that describes where (within it parent), and how big,
     * this view is.
     * @see #getSize()
     * @see #getLocation()
     * @return Bounds
     */
    Bounds getBounds();

    int getId();

    /**
     * Returns true when this views layout is not current, i.e. it needs to be laid out again.
     */
    boolean isLayoutInvalid();

    /**
     * Specifies the location of this view, relative to its enclosing view.
     * @param point
     */
    void setLocation(Location point);

    /**
     * Determines the location relative to this object's containing view
     * @see #getBounds()
     */
    Location getLocation();

    String getName();

    /**
     * Indicates whether this view is expanded, or iconized.
     * @return true if it is showing the object's details; false if it is showing the object only.
     */
    boolean isOpen();

    Padding getPadding();

    CompositeView getParent();

    /**
     * Indicates whether this view can be replaced with another view (for the
     * same value or reference).
     * @return true if it can be replaced by another view; false if it can't be replaces
     */
    boolean isReplaceable();

    Size getRequiredSize();

    void setSize(Size size);

    /**
      * Determines the size of this view.
      * @see #getBounds()
      */
    Size getSize();

    Workspace getWorkspace();

    /**
     * Called to mark this view's on-screen state as invalid, i.e. it needs to be redrawn.
     */
    void calculateRepaintArea();

    /**
     * Determines whether the specified point (from within the parent view) is
     * within this view.  I.e., whether the mouse pointer is over this object.
     */
    boolean contains(Location point);

    String debugDetails();

    /**
     * Called when a view is no longer needed and its resources can be disposed of.
     */
    void dispose();

    /**
     * Called by the frame, or the parent view, when this view must redraw
     * itself.
     */
    void draw(Canvas canvas);

    /**
     * Called as the mouse crosses the bounds, and ends up inside, of this view.  Is also
     * called as the mouse returns into this view from a contained view.
     */
    void entered();

    /**
     * Called as the mouse moves into one of this view's contained views.
     */
    void enteredSubview();

    /**
     * Called as the mouse crosses the bounds, and ends up outside, of this view.
     */
    void exited();

    /**
         * Called as the mouse moves back into view from one its contained views.
         */
    void exitedSubview();

    /**
     * Called when the user clicks the mouse buttone within this view.
     * @param click        the location within the current view where the mouse click
     * took place
     */
    void firstClick(Click click);

    /**
     * Returns the view that mouse pointer is over.  If it is over this view and
     * not over any of it's components then this views reference  is returned.
     * Returns this view; should be overriden for container views.
     * @param mouseLocationer
     * @param current
     * @return View this view
     */
    View identifyView(Location mouseLocationer, View current);

    /**
     * Determines if the user is invoking an action relating to this view, rather than to whatever this view represents.
     * @param mouseLocation
     * @return true if the user is targeting the view itself, false if the user is targeting what is being represented
     */
    boolean indicatesForView(Location mouseLocation);

    /**
     * sets this view layout flag to show that the layout is no longer valid
     */
    void invalidateLayout();

    void layout();

    /**
     * Creates a new concrete view, using this view as a prototype, reassigning
     * the naked object in the process.  The field is the field within the parent view's object that this field
     * represents, and is null if the view is not part of another - i.e. it is not a field within an object.
     * @param object  the object the new view is for
     * @return View
     * @throws CloneNotSupportedException
     */
    View makeView(Naked object, Field field) throws CloneNotSupportedException;

    /**
     * Called when the popup menu is being populated for this view.  Any
     * options that need to appear on the menu should be added to the
     * <code>menuOptions</code> object.
     * @param menuOptions
     */
    void menuOptions(MenuOptionSet menuOptions);

    /**
     * Called as the mouse is moved around within this view.    Does nothing;
     * should be overriden when needed.
     * @param at          the position relative to the top-left of this view
     */
    void mouseMoved(Location at);

    void print(Canvas canvas);

    void redraw();

    /**
     * Called when the user double-clicked this view.  This method will have
     * been preceded by a call to <code>click</code>.
     */
    void secondClick(Click click);

    /**
     * Called when the user triple-clicks the mouse buttone within this view.
     * This method will have been preceded by a call to
     * <code>doubleClick</code>.
     */
    void thirdClick(Click click);

    /**
     * Reqests that this view tree needs to be laid out. The root view will start calling
     * layout on each of the invalid views in the tree.
     */
    void validateLayout();
}
