package org.nakedobjects.viewer.skylark;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.Naked;


public interface View extends Cloneable {
    /** Horizontal padding (||) between two components */
    public static final int HPADDING = NakedObjects.getConfiguration().getInteger(Viewer.PROPERTY_BASE + "hpadding", 2);
    /** Vertical padding (=) between two components */
    public static final int VPADDING = NakedObjects.getConfiguration().getInteger(Viewer.PROPERTY_BASE + "vpadding", 2);

    void addView(View view);

    /**
     * Determines if the user is able to change the held value.
     */
    boolean canChangeValue();

    /**
     * Determines whether this view accepts keyboard focus. If so focusLost and
     * focusReceived will be called.
     */
    boolean canFocus();

    boolean contains(View view);

    /**
     * Called when the popup menu is being populated for this view. Any content
     * options that need to appear on the menu should be added to the
     * <code>menuOptions</code> object.
     */
    void contentMenuOptions(MenuOptionSet menuOptions);

    String debugDetails();

    /**
     * Called when a view is no longer needed and its resources can be disposed
     * of.  Dissociates this view from its parent, and removes itself from the list of
     * views that need to be updated.
     * 
     * @see #removeView(View)
     */
    void dispose();

    /**
     * Called as mouse is dragged within and without this view. This only occurs
     * when no content or view is being dragged.
     */
    void drag(InternalDrag drag);

    void dragCancel(InternalDrag drag);

    View dragFrom(Location location);

    /**
     * Called as the content being dragged is dragged into this view. This only
     * occurs when view contents are being dragged, and not when views
     * themselves are being dragged.
     */
    void dragIn(ContentDrag drag);

    /**
     * Called as the content being dragged is dragged out of this view. This
     * only occurs when view contents are being dragged, and not when views
     * themselves are being dragged.
     */
    void dragOut(ContentDrag drag);

    Drag dragStart(DragStart drag);

    /**
     * Called as the drag ends within and without this view. This only occurs
     * when no content or view is being dragged.
     */
    void dragTo(InternalDrag drag);

    /**
     * Called by the frame, or the parent view, when this view must redraw
     * itself.
     */
    void draw(Canvas canvas);

    /**
     * Called as another view's contents (the source) is dropped on this view's
     * contents (the target). The source view can be obtained from the ViewDrag
     * object.
     */
    void drop(ContentDrag drag);

    /**
     * Called as another view (the source) is dropped on this view (the target).
     * The source view can be obtained from the ViewDrag object.
     */
    void drop(ViewDrag drag);

    /**
     * Indicates that editing has been completed and the entry should be saved.
     * Will be called by the view manager when other action place within the
     * parent.
     */
    void editComplete();

    /**
     * Called as the mouse crosses the bounds, and ends up inside, of this view.
     * Is also called as the mouse returns into this view from a contained view.
     */
    void entered();

    /**
     * Called as the mouse moves into one of this view's contained views.
     */
    void enteredSubview();

    /**
     * Called as the mouse crosses the bounds, and ends up outside, of this
     * view.
     */
    void exited();

    /**
     * Called as the mouse moves back into view from one its contained views.
     */
    void exitedSubview();

    /**
     * Called when the user clicks the mouse buttone within this view.
     * 
     * @param click
     *                       the location within the current view where the mouse click
     *                       took place
     */
    void firstClick(Click click);

    void focusLost();

    void focusReceived();

    Location getAbsoluteLocation();

    int getBaseline();

    /**
     * Returns the bounding rectangle that describes where (within it parent),
     * and how big, this view is.
     * 
     * @see #getSize()
     * @see #getLocation()
     * @return Bounds
     */
    Bounds getBounds();

    /**
     * get the object that this view represents
     */
    Content getContent();

    int getId();

    /**
     * Determines the location relative to this object's containing view
     * 
     * @see #getBounds()
     */
    Location getLocation();

    Padding getPadding();

    View getParent();

    Size getRequiredSize();

    /**
     * Determines the size of this view.
     * 
     * @see #getBounds()
     */
    Size getSize();

    ViewSpecification getSpecification();

    ViewState getState();

    View[] getSubviews();

    /**
     * returns the topmost decorator in the chain, or the view itself if not
     * decorated.
     */
    View getView();

    ViewAxis getViewAxis();

    Viewer getViewManager();

    Workspace getWorkspace();

    boolean hasFocus();

    View identify(Location mouseLocation);
    
    /**
     * Flags that the views do not properly represent the content, and hence it
     * needs rebuilding. Contrast this with invalidateLayout(), which deals with
     * an a complete view, but one that is not showing properly.
     * 
     * @see #invalidateLayout()
     */
    void invalidateContent();

    /**
     * Flags that the views are possibly not displaying the content fully - too
     * small, wrong place etc - although views exists for all the content.
     * Contrast this with invalidateContent(), which deals with an incomplete
     * view.
     * 
     * @see #invalidateContent()
     */
    void invalidateLayout();

    /**
     * Called when the user presses any key on the keyboard while this view has
     * the focus.
     */
    void keyPressed(final int keyCode, final int modifiers);

    /**
     * Called when the user releases any key on the keyboard while this view has
     * the focus.
     */
    void keyReleased(int keyCode, int modifiers);

    /**
     * Called when the user presses a non-control key (i.e. data entry keys and
     * not shift, up-arrow etc). Such a key press will result in a prior call to
     * <code>keyPressed</code> and a subsequent call to
     * <code>keyReleased</code>.
     */
    void keyTyped(char keyCode);

    void layout();

    /**
     * Limits the bounds of this view (normally when being moved or dropped) so
     * it never extends beyond the bounds of its containing view.
     */
    void limitBoundsWithin(Bounds bounds);

    void markDamaged();

    void markDamaged(Bounds bounds);

    /**
     * Called as the mouse is moved around within this view. Does nothing;
     * should be overriden when needed.
     * 
     * @param location
     *                       the position relative to the top-left of this view
     */
    void mouseMoved(Location location);

    /**
     * Called when an action generates a result, allowing this view to decide
     * what to do with it.
     * 
     * @param at
     *                       the location where the action took place
     */
    void objectActionResult(Naked result, Location at);

    /**
     * Called as the drag of this view's content starts.
     */
    View pickupContent(Location location);

    /**
     * Called as the drag of this view starts.
     */
    View pickupView(Location location);

    void print(Canvas canvas);

    /**
     * Refreshes this view by reaccessing its content and redisplaying it.
     */
    void refresh();

    /**
     * Removes the specifed view from the subviews contained by this view.
     */
    void removeView(View view);

    void replaceView(View toReplace, View replacement);

    /**
     * Called when the user double-clicked this view. This method will have been
     * preceded by a call to <code>click</code>.
     */
    void secondClick(Click click);

    void setBounds(Bounds bounds);

    /**
     * Specifies the location of this view, relative to its enclosing view.
     * 
     * @param point
     */
    void setLocation(Location point);

    void setParent(View view);

    public void setRequiredSize(Size size);

    void setSize(Size size);

    void setView(View view);

    /**
     * Identifies the subview that contains the specified location within its
     * bounds. Returns null if no subview exists for that location.
     */
    View subviewFor(Location location);
    
    /**
     * Called when the user triple-clicks the mouse buttone within this view.
     * This method will have been preceded by a call to <code>doubleClick</code>.
     */
    void thirdClick(Click click);

    /**
     * notification that the content of this view has changed
     */
    void update(Naked object);

    void updateView();

    /**
     * Determines if the user is invoking an action relating to this view,
     * rather than to whatever this view represents.
     * 
     * @param mouseLocation
     * @return true if the user is targeting the view itself, false if the user
     *                 is targeting what is being represented
     */
    ViewAreaType viewAreaType(Location mouseLocation);

    /**
     * Called when the popup menu is being populated for this view. Any view
     * options that need to appear on the menu should be added to the
     * <code>menuOptions</code> object.
     */
    void viewMenuOptions(MenuOptionSet menuOptions);
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
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
