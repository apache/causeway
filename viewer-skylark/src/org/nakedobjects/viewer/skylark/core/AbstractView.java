package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.control.defaults.Allow;
import org.nakedobjects.object.reflect.UndoStack;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ContentDrag;
import org.nakedobjects.viewer.skylark.IdentifiedView;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.Offset;
import org.nakedobjects.viewer.skylark.Padding;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.UserAction;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewDrag;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.ViewState;
import org.nakedobjects.viewer.skylark.ViewerAssistant;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.util.ViewFactory;

import java.util.Enumeration;

import org.apache.log4j.Logger;


public abstract class AbstractView implements View {
    private static final UserAction CLOSE_ALL_OPTION = new CloseAllViewsOption();
    private static final UserAction CLOSE_OPTION = new CloseViewOption();
    public static boolean DEBUG = false;
    private static final Logger LOG = Logger.getLogger(AbstractView.class);

    private static int nextId = 0;
    private Content content;
    private int height;
    private int id = 0;
    private View parent;
    private ViewSpecification specification;
    private ViewState state;
    private View view;
    private ViewAxis viewAxis;
    private int width;
    private int x;
    private int y;

    protected AbstractView(Content content, ViewSpecification specification, ViewAxis axis) {
        assignId();
        this.content = content;
        this.specification = specification;
        this.viewAxis = axis;
        state = new ViewState();
        view = this;
    }
    
    public Location getLocationWithinViewer() {
           if(getParent() != null) {
               Location location = getParent().getLocationWithinViewer();
               getViewManager().getSpy().trace(this, "parent location ", location);
               
               Padding parentPadding = parent.getPadding();
               getViewManager().getSpy().trace(this, "parent padding ", parentPadding);
               location.move(parentPadding.getTop(), parentPadding.getLeft());
               
               location.move(x, y);
               getViewManager().getSpy().trace(this, "new location ", location);
               return location;
           } else {
               getViewManager().getSpy().trace(this, "my location ", getLocation());
               
               return getLocation();
           }
       }

    public void run(final BackgroundTask task) {
        Thread t = new Thread("background task") {
            public void run() {
                //isSaving = true;
                state.setActive();
                repaint();

                task.execute();

                // isSaving = false;
                state.setInactive();
                markDamaged();
                repaint();
            }
        };

        t.start();
    }

    public void addView(View view) {
        throw new NakedObjectRuntimeException();
    }

    protected void assignId() {
        id = nextId++;
    }

    public boolean canChangeValue() {
        return false;
    }

    public boolean canFocus() {
        return false;
    }

    /**
     * Returns debug details about this view.
     */
    public String debugDetails() {
        StringBuffer b = new StringBuffer();

        b.append("View:      ");
        String name = getClass().getName();
        b.append(name.substring(name.lastIndexOf('.') + 1) + getId());

        b.append("\n           size " + getSize());
        b.append("\n           req'd " + getRequiredSize());
        b.append("\n           padding " + getPadding());
        b.append("\n           baseline " + getBaseline());
        b.append("\n");

        b.append("\nSelf:      " + getView());
        b.append("\nAxis:      " + getViewAxis());
        b.append("\nState:     " + getState());
        b.append("\nLocation:  " + getLocation());
        if(specification == null) {
            b.append("\nSpec:      none");
        } else {
            b.append("\nSpec:      " + specification.getName() + " (" + specification + ")");
            b.append("\n           " + (specification.isOpen() ? "open" : "closed"));
            b.append("\n           " + (specification.isReplaceable() ? "replaceable" : "non-replaceable"));
            b.append("\n           " + (specification.isSubView() ? "subview" : "main view"));
        }
        b.append("\n           " + (canFocus() ? "focusable" : "non-focusable"));

        b.append("\nParent:    ");
        
        View p = getParent();
        String parent = p == null ? "none" : "" + p;
        b.append(parent);
        
        while(p != null) {
            p = p.getParent();
            b.append("\n           " + p);
            
        }
        
        
        b.append("\nWorkspace: " + getWorkspace());

        b.append("\n\n");

        return b.toString();
    }

    public void dispose() {
        if (parent != null) {
            parent.removeView(getView());
        } else {
            ViewerAssistant.getInstance().clearOverlayView(this);
        }
    }

    public void drag(InternalDrag drag) {
    }

    public void dragCancel(InternalDrag drag) {
        getViewManager().showArrowCursor();
    }

    public View dragFrom(InternalDrag drag) {
        return null;
    }

    public void dragIn(ContentDrag drag) {}

    public void dragOut(ContentDrag drag) {
    }

    public void dragTo(InternalDrag drag) {
    }

    public void draw(Canvas canvas) {
    }

    public void drop(ContentDrag drag) {}

    /**
     * No default behaviour, views can only be dropped on workspaces
     */
    public void drop(ViewDrag drag) {}

    public void editComplete() {
    }

    public void entered() {
    }

    public void enteredSubview() {
    }

    public void exited() {
    }

    public void exitedSubview() {
    }

    public void firstClick(Click click) {
        Workspace workspace = getWorkspace();
        if (workspace != null) {
            if (click.isButton2() || (click.isButton1() && click.isShift())) {
                workspace.lower(getView());
                markDamaged();
            } else if (click.isButton1()) {
                workspace.raise(getView());
                markDamaged();
            }
        }
    }

    public void focusLost() {}

    public void focusNext() {
        View[] views = getParent().getSubviews();

        for (int i = 0; i < views.length; i++) {
            if (views[i] == getView()) {
                for (int j = i + 1; j < views.length; j++) {
                    if (views[j].canFocus()) {
                        getViewManager().makeFocus(views[j]);
                        return;
                    }
                }
                for (int j = 0; j < i; j++) {
                    if (views[j].canFocus()) {
                        getViewManager().makeFocus(views[j]);
                        return;
                    }
                }
                // no other focusable view; stick with the view we've got
                return;
            }
        }

        throw new NakedObjectRuntimeException();
    }

    public void focusPrevious() {
        View[] views = getParent().getSubviews();

        for (int i = 0; i < views.length; i++) {
            if (views[i] == getView()) {
                for (int j = i - 1; j >= 0; j--) {
                    if (views[j].canFocus()) {
                        getViewManager().makeFocus(views[j]);
                        return;
                    }
                }
                for (int j = views.length - 1; j > i; j--) {
                    if (views[j].canFocus()) {
                        getViewManager().makeFocus(views[j]);
                        return;
                    }
                }
                // no other focusable view; stick with the view we've got
                return;
            }
        }

        throw new NakedObjectRuntimeException();
    }

    public void focusRecieved() {}

    public int getBaseline() {
        return 0;
    }

    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }

    public Content getContent() {
        return content;
    }

    public int getId() {
        return id;
    }

    public Location getLocation() {
        return new Location(x, y);
    }

    public Padding getPadding() {
        return new Padding(0, 0, 0, 0);
    }

    public final View getParent() {
        Assert.assertEquals(parent == null ? null : parent.getView(), parent);

        return parent;
    }

    public Size getRequiredSize() {
        return new Size();
    }

    public Size getSize() {
        return new Size(width, height);
    }

    public ViewSpecification getSpecification() {
        return specification;
    }

    public ViewState getState() {
        return state;
    }

    public View[] getSubviews() {
        return new View[0];
    }

    public final View getView() {
        return view;
    }

    public final ViewAxis getViewAxis() {
        return viewAxis;
    }

    public ViewerAssistant getViewManager() {
        return ViewerAssistant.getInstance();
    }

    public Workspace getWorkspace() {
        return getParent() == null ? null : getParent().getWorkspace();
    }

    public boolean hasFocus() {
        return false;
    }
    
    public View identify(Location location) {
        location.move(-x, -y);
        getViewManager().getSpy().trace(this, "node view location", location);
          return getView();
    }

    public IdentifiedView identify2(Location location) {
        getViewManager().getSpy().trace(this, "mouse location within node view", location);
        getViewManager().getSpy().trace("----");
        return new IdentifiedView(getView(), location, getLocation());
    }

    public IdentifiedView identify3(Location locationWithinView, Offset offset) {
      getViewManager().getSpy().trace(this, "mouse location within node view", locationWithinView);
      getViewManager().getSpy().trace("----");
      return new IdentifiedView(getView(), locationWithinView, getLocation());
  }


    /**
     * Flags that the views do not properly represent the content, and hence it
     * needs rebuilding. Contrast this with invalidateLayout(), which deals with
     * an a complete view, but one that is not showing properly.
     * 
     * @see #invalidateLayout()
     */
    public void invalidateContent() {}

    /**
     * Flags that the views are possibly not displaying the content fully - too
     * small, wrong place etc - although views exists for all the content.
     * Contrast this with invalidateContent(), which deals with an incomplete
     * view.
     * 
     * @see #invalidateContent()
     */
    public void invalidateLayout() {
        if (parent != null) {
            parent.invalidateLayout();
        }
    }

    public void keyPressed(int keyCode, int modifiers) {
    // TODO Auto-generated method stub
    }

    public void keyReleased(int keyCode, int modifiers) {
    // TODO Auto-generated method stub
    }

    public void keyTyped(char keyCode) {
    // TODO Auto-generated method stub
    }

    public void layout() {}

    /**
     * Limits the bounds of the given view (when being moved or dropped) so its
     * never extends outside the bounds of the containing open view
     */
    public void limitBounds(View view) {
        Location location = view.getLocation();
        Size size = view.getSize();

        int viewLeft = location.getX();
        int viewTop = location.getY();
        int viewRight = viewLeft + size.getWidth();
        int viewBottom = viewTop + size.getHeight();

        Size wd = getSize();
        Padding insets = getPadding();

        int limitLeft = insets.getLeft();
        int limitTop = insets.getTop();
        int limitRight = wd.getWidth() - insets.getRight();
        int limitBottom = wd.getHeight() - insets.getBottom();

        if (viewRight > limitRight) {
            viewLeft = limitRight - size.getWidth();
            LOG.info("right side oustide limits, moving left to" + viewLeft + " on " + view);
        }

        if (viewLeft < limitLeft) {
            viewLeft = limitLeft;
            LOG.info("left side outside limit, moving left to " + viewLeft + " on " + view);
        }

        if (viewBottom > limitBottom) {
            viewTop = limitBottom - size.getHeight();
            LOG.info("bottom outside limit, moving top to " + viewTop + " on " + view);
        }

        if (viewTop < limitTop) {
            viewTop = limitTop;
            LOG.info("top outside limit, moving top to " + viewTop + " on " + view);
        }

        location.setX(viewLeft);
        location.setY(viewTop);
        view.setLocation(location);
    }

    public void markDamaged() {
        markDamaged(getView().getBounds());
    }

    public void markDamaged(Bounds bounds) {
        if (parent == null) {
            getViewManager().markDamaged(bounds);
        } else {
            Location pos = parent.getLocation();
            bounds.translate(pos.getX(), pos.getY());
            Padding pad = parent.getPadding();
            bounds.translate(pad.getLeft(), pad.getTop());
            parent.markDamaged(bounds);
        }
    }

    public Location getAbsoluteLocation() {
        if (parent == null) {
            return getLocation();
        } else {
            Location location = parent.getAbsoluteLocation();
            location.move(x, y);
            Padding pad = parent.getPadding();
            location.move(pad.getLeft(), pad.getTop());
            return location;
        }
    }
    
    public void menuOptions(MenuOptionSet options) {
        if (options.isForView()) {
            viewMenuOptions(options);

        } else {
            options.setColor(Style.CONTENT_MENU);
            if (getContent() != null) {
                getContent().menuOptions(options);
            }
        }

        options.add(MenuOptionSet.DEBUG, new MenuOption("Refresh view") {
            public void execute(Workspace workspace, View view, Location at) {
                refresh();
            }
        });
        
        options.add(MenuOptionSet.DEBUG, new MenuOption("Invalidate content") {
            public void execute(Workspace workspace, View view, Location at) {
                invalidateContent();
            }
        });
        
        options.add(MenuOptionSet.DEBUG, new MenuOption("Invalidate layout") {
            public void execute(Workspace workspace, View view, Location at) {
                invalidateLayout();
            }
        });
        
        final UndoStack undoStack = getViewManager().getUndoStack();
            if (!undoStack.isEmpty()) {
                options.add(MenuOptionSet.VIEW, new MenuOption("Undo " + undoStack.getNameOfUndo() ){
                    public void execute(Workspace workspace, View view, Location at) {
                        undoStack.undoLastCommand();
                    }
                 
                    public Permission disabled(View component) {
                        return new Allow(undoStack.descriptionOfUndo());
                    }
                });
            }
    }

    public void mouseMoved(Location at) {
    }

    public void objectActionResult(Naked result, Location at) {
        getWorkspace().addOpenViewFor(result, at);
    }

    public View pickup(ContentDrag drag) {
        return null;
    }

    public View pickup(ViewDrag drag) {
        View dragView = new DragViewOutline(drag);
        getViewManager().setDeveloperStatus("Moving " + this);
        return dragView;
    }

    /**
     * Delegates all printing the the draw method.
     * 
     * @see #draw(Canvas)
     */
    public void print(Canvas canvas) {
        draw(canvas);
    }

    /**
     * Forces a repaint; should only be used in places markDamaged() does not
     * work because no user action occurs - within the required timeframe - that
     * would otherwise cause a redraw automatically.
     */
    protected void repaint() {
        markDamaged();
        getViewManager().forceRepaint();
    }

    public void refresh() {}

    public void removeView(View view) {
        throw new NakedObjectRuntimeException();
    }

    protected void replaceOptions(Enumeration possibleViews, MenuOptionSet options) {
        while (possibleViews.hasMoreElements()) {
            ViewSpecification specification = (ViewSpecification) possibleViews.nextElement();

            if (specification != getSpecification() && view.getClass() != getClass()) {
                MenuOption viewAs = new ReplaceViewOption(specification);
                options.add(MenuOptionSet.VIEW, viewAs);
            }
        }
    }

    public void replaceView(View toReplace, View replacement) {
        throw new NakedObjectRuntimeException();
    }

    public void secondClick(Click click) {
    }

    public void setBounds(Bounds bounds) {
        x = bounds.getX();
        y = bounds.getY();
        width = bounds.getWidth();
        height = bounds.getHeight();
    }

    public void setLocation(Location location) {
        x = location.getX();
        y = location.getY();
    }

    public final void setParent(View view) {
        parent = view.getView();

        LOG.debug("set parent " + parent + " for " + this);
    }

    public void setRequiredSize(Size size) {}
    
    public void setSize(Size size) {
        width = size.getWidth();
        height = size.getHeight();
    }

    public final void setView(View view) {
        this.view = view;
    }

    public void thirdClick(Click click) {
    }

    public String toString() {
        String name = getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1) + getId() + " " + getContent();
    }

    public void update(NakedObject object) {
    }

    public ViewAreaType viewAreaType(Location mouseLocation) {
        return ViewAreaType.CONTENT;
    }

    public void viewMenuOptions(MenuOptionSet options) {
        options.setColor(Style.VIEW_MENU);
        Content content = getContent();

        if (view.getSpecification().isSubView()) {
            if (view.getSpecification().isReplaceable()) {
                replaceOptions(ViewFactory.getViewFactory().openSubviews(content, this), options);
                replaceOptions(ViewFactory.getViewFactory().closedSubviews(content, this), options);
            }
        } else {
            options.add(MenuOptionSet.VIEW, CLOSE_OPTION);
            options.add(MenuOptionSet.VIEW, CLOSE_ALL_OPTION);
            options.add(MenuOptionSet.VIEW, new PrintOption());
            if (view.getSpecification().isReplaceable()) {
                // offer other/alternative views
                replaceOptions(ViewFactory.getViewFactory().openRootViews(content, this), options);
            }
        }

        Enumeration possibleViews = ViewFactory.getViewFactory().openRootViews(content, null);

        while (possibleViews.hasMoreElements()) {
            ViewSpecification specification = (ViewSpecification) possibleViews.nextElement();
            MenuOption viewAs = new OpenViewOption(specification);
            options.add(MenuOptionSet.VIEW, viewAs);
        }
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
