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
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NotPersistableException;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.utility.DebugInfo;
import org.nakedobjects.utility.NotImplementedException;
import org.nakedobjects.viewer.lightweight.options.PrintOption;
import org.nakedobjects.viewer.lightweight.util.CascadeAlignment;
import org.nakedobjects.viewer.lightweight.util.IconAlignment;
import org.nakedobjects.viewer.lightweight.util.ViewFactory;
import org.nakedobjects.viewer.lightweight.view.EmptyBorder;

import java.awt.Cursor;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public class Workspace extends AbstractView implements Background, DragTarget, PrintableView {
    private static final Logger LOG = Logger.getLogger(Workspace.class);
    private static final Border EMPTY_BORDER = new EmptyBorder(5);

    /**
    * Strategy for rendering the background
    */
    private Background background;
    private Border border = EMPTY_BORDER;
    private Viewer viewer;
    private Vector icons = new Vector();
    private Vector openViews = new Vector();

    /** The view identified by the user.  Set to the view that is under the
     * mouse when the mouse is being moved but not dragged Reset to null when
     * the mouse moves outside of that view.  */
    private View identifiedView = this;

    /** The view that the user identified before dragging.  Set to the view that was under the
     * mouse when the drag was started. Reset to null when the mouse key is
     * released. */
    private View overlayView;
    private boolean objectIdentified;

    public Workspace(Viewer mechanism) {
        viewer = mechanism;
        setWorkspace(this);
    }

    /**
    * Sets the background strategy.
    * @param background
    */
    public void setBackground(Background background) {
        this.background = background;
    }

    public void setBorder(Border border) {
        this.border = border;
    }

    public Border getBorder() {
        return border;
    }

    /**
     * Determines whether the specified view is the one that is currently
     * identified.
     * @param view
     * @return boolean
     */
    public boolean isIdentified(View view) {
        return identifiedView == view;
    }

    public void setIdentifiedView(View view) {
        identifiedView = view;
    }

    public View getIdentifiedView() {
        return identifiedView;
    }

    /**
     * Determines whether the specified view is the one that is currently
     * identified in a way that indicates that the object itself (vs the view) is of interest.
     * @param view
     * @return boolean
     * @deprecated
     */
    public boolean isObjectIdentified(View view) {
        return objectIdentified && isIdentified(view);
    }

    public void setOverlayView(View view) {
        if (overlayView != null) {
            LOG.error("An overlay view already exists: " + overlayView);
        }

        limitBounds(view);
        overlayView = view;
        view.redraw();
    }

    public View getOverlayView() {
    	return overlayView;
    }
    
    public Padding getPadding() {
        return viewer.getPadding();
    }

    public Size getRequiredSize() {
        return viewer.getSize();
    }

    public void setStatus(String string) {
        viewer.status(string);
    }

    /**
     * Adds a view to the list of iconized views for this workspace and re-layout all the
     * views.  The icon's position is also limited so it is inside the bounds of this frame.
     * @param view  the view to add
     */
    public void addIcon(final DesktopView view) {
        icons.addElement(view);
        view.setSize(view.getRequiredSize());
        redraw();
    }

    public void addNotificationView(ObjectView view) {
        viewer.addNotificationView(view);
    }

    /**
     * Adds a view to the list of open view views for this workspace and re-layout all the
     * views.  The open view's position is also limited so it is inside the bounds of this frame.
     * @param view  the view to add
     */
    public void addRootView(RootView view) {
        openViews.addElement(view);
        view.invalidateLayout();
        view.validateLayout();
    }

    public void clearOverlayView() {
        if (overlayView != null) {
            Bounds bounds = overlayView.getBounds();
            overlayView.dispose();
            overlayView = null;
            repaint(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    public void dragObjectIn(ObjectDrag drag) {
    }

    public void dragObjectOut(ObjectDrag drag) {
    }

    public void draw(Canvas canvas) {
    	super.draw(canvas);
    	
        // background layer
        if (background != null) {
            background.paintBackground(canvas, getSize());
        }

        // icon layer
        for (int i = 0; i < icons.size(); i++) {
            View vw = (View) icons.elementAt(i);
            Bounds viewBounds = vw.getBounds();

            if (canvas.intersects(viewBounds)) {
                Canvas iconCanvas = canvas.createSubcanvas(viewBounds.x, viewBounds.y,
                        viewBounds.width, viewBounds.height);
                vw.draw(iconCanvas);
            }
        }

        // open-view layer
        for (int i = 0; i < openViews.size(); i++) {
            ObjectView vw = (ObjectView) openViews.elementAt(i);
            Bounds viewBounds = vw.getBounds();

            if (canvas.intersects(viewBounds)) {
                Canvas rootCanvas = canvas.createSubcanvas(viewBounds.x, viewBounds.y,
                        viewBounds.width, viewBounds.height);
                vw.draw(rootCanvas);
            }
        }

        // overlay
        if (overlayView != null) {
            Bounds viewBounds = overlayView.getBounds();

            if (canvas.intersects(viewBounds)) {
                Canvas dragCanvas = canvas.createSubcanvas(viewBounds.x, viewBounds.y,
                        viewBounds.width, viewBounds.height);
                overlayView.draw(dragCanvas);
            }
        }

        LOG.debug("\n");
    }

    public void dropObject(ObjectDrag drag) {
        showArrowCursor();

        View source = drag.getSource();

        if (source instanceof ClassView) {
            LOG.info("new " + getName() + " instance");
            newInstance(((ClassView) source).forNakedClass(), getWorkspace(),
                drag.getViewLocation(), !drag.isShift());
        } else {
            // place object onto desktop as icon
            DesktopView newIcon = (DesktopView) ViewFactory.getViewFactory().createIconView(((ObjectView) source).getObject(),
                    null);
            newIcon.setLocation(drag.getViewLocation());
            getWorkspace().addIcon(newIcon);
        }
    }

    public boolean hasFocus(AbstractValueView view) {
        return viewer.hasFocus(view);
    }

    /**
     * Returns the <code>View</code> that is currently under the pointer.  Works
     * through each top-level view and when the mouse is found in the that the
     * same method is then called within that view, and so on.  This ensures
     * that views within views are identified.  An optional parameter ensures
     * that an already identified view is not returned.  This is used when the
     * dragged view needs to be ignored so a drop target can be identified.
     *
     * @param framePosition        the mouse position within the frame
     * @param current the object that is being dragged, or <code>null</code> if none
     * @return Component
     */
    public View identifyView(Location framePosition, View current) {
        View identified = identifyView2(framePosition, current);

        if (identified != null) {
            Location ppp = new Location(framePosition);
            Location offset1 = identified.getAbsoluteLocation();
            ppp.translate(-offset1.x, -offset1.y);
        } else {
            identified = this;
        }

        return identified;
    }

    public void layoutIcons() {
        WorkspaceAlignment alignment = new IconAlignment();

        int noIcons = icons.size();
        View[] views = new View[noIcons];

        for (int i = 0; i < noIcons; i++) {
            views[i] = (View) icons.elementAt(i);
        }

        alignment.align(views, this);
    }

    /**
     * Limits the bounds of the given view (when being moved or dropped) so its never extends outside
     * the bounds of the containing open view
    */
    public void limitBounds(View view) {
        Location location = view.getLocation();
        Size size = view.getSize();

        int viewLeft = location.x;
        int viewTop = location.y;
        int viewRight = viewLeft + size.width;
        int viewBottom = viewTop + size.height;

 //       Size wd = getSize();
		Size wd = viewer.getSize();
        Padding insets = getPadding();

        int limitLeft = insets.left;
        int limitTop = insets.top;
        int limitRight = wd.width - insets.right;
        int limitBottom = wd.height - insets.bottom;

        if (viewRight > limitRight) {
            viewLeft = limitRight - size.width;
        }

        if (viewLeft < limitLeft) {
            viewLeft = limitLeft;
        }

        if (viewBottom > limitBottom) {
            viewTop = limitBottom - size.height;
        }

        if (viewTop < limitTop) {
            viewTop = limitTop;
        }

        location.x = viewLeft;
        location.y = viewTop;
        view.setLocation(location);
    }

    public void makeFocus(KeyboardAccessible view) {
        viewer.makeFocus(view);
    }

    public View makeView(Naked object, Field field) throws CloneNotSupportedException {
        throw new NotImplementedException();
    }

    public void menuOptions(MenuOptionSet options) {
    	super.menuOptions(options);
    	
        viewer.menuOptions(options);

        options.add(MenuOptionSet.OBJECT,
            new MenuOption("Naked Class...") {
                public void execute(Workspace workspace, View view, Location at) {
                    DesktopView classesView = ViewFactory.getViewFactory().createClassView(NakedClass.SELF);
                    classesView.setLocation(at);
                    addIcon(classesView);
                }
            });

        options.add(MenuOptionSet.VIEW, new PrintOption());

        options.add(MenuOptionSet.VIEW,
            new MenuOption("Close all") {
                public void execute(Workspace workspace, View view, Location at) {
                    ObjectView[] vw = new ObjectView[openViews.size()];

                    for (int i = 0; i < openViews.size(); i++) {
                        vw[i] = (ObjectView) openViews.elementAt(i);
                    }

                    for (int i = 0; i < vw.length; i++) {
                        removeView(vw[i]);
                    }

                    redraw();
                }
            });

        options.add(MenuOptionSet.VIEW,
            new MenuOption("Tidy up views") {
                public void execute(Workspace workspace, View view, Location at) {
                    layoutOpenViews();
                    redraw();
                }
            });

        options.add(MenuOptionSet.EXPLORATION,
            new MenuOption("Tidy up icons") {
                public void execute(Workspace workspace, View view, Location at) {
                    layoutIcons();
                    redraw();
                }
            });

        options.add(MenuOptionSet.DEBUG,
            new MenuOption("View's debug info...") {
                public void execute(Workspace workspace, View view, Location at) {
                    DebugFrame f = new DebugFrame();
                    f.setInfo(new DebugOpenViewList());
                    f.show(at.x + 50, workspace.getBounds().y + 6);
                }
            });

        options.add(MenuOptionSet.DEBUG, loggingOption("Error", Level.ERROR));
        options.add(MenuOptionSet.DEBUG, loggingOption("Info", Level.INFO));
        options.add(MenuOptionSet.DEBUG, loggingOption("Debug", Level.DEBUG));
        options.add(MenuOptionSet.DEBUG, loggingOption("Off", Level.OFF));
        
        options.setColor(Style.WORKSPACE_MENU);
    }

    private MenuOption loggingOption(String name, final Level level) {
    	return 
    			new MenuOption("Log " + level +" " + name + "...") {
    		public Permission disabled(Workspace workspace, View component,
    				Location location) {
    			
    			return Permission.allow(LogManager.getLoggerRepository().getThreshold() != level);
    		}
    		public void execute(Workspace workspace, View view, Location at) {
    			LogManager.getLoggerRepository().setThreshold(level);
    		}
    	};
    }
    
    /*
     * default implementation of the Background strategy - clears background
     * */
    public void paintBackground(Canvas canvas, Size size) {
        canvas.clearBackground(Style.APPLICATION_BACKGROUND);
    }

    public void removeFromNotificationList(ObjectView view) {
        viewer.removeFromNotificationList(view);
    }

    /**
     * Removes and views (icons and open views) from the frame that are for the specified
     * object.
     * @param object
     */
    public final void removeViewsFor(NakedObject object) {
        Vector toRemove = new Vector();
        removeViewsFor(object, toRemove);

        for (int i = 0; i < toRemove.size(); i++) {
            removeView((View) toRemove.elementAt(i));
        }
    }

    /**
     * Removes all the views - other than the specified one - from the workspace
     * @param view
     */
    public void removeOtherRootViews(RootView view) {
        Enumeration e = openViews.elements();

        while (e.hasMoreElements()) {
            ObjectView element = (ObjectView) e.nextElement();

            if (element != view) {
                element.dispose();
            }
        }

        openViews.removeAllElements();
        openViews.addElement(view);
        redraw();
    }

    /**
     * Removes the specified view from the workspace
     * @param view
     */
    public void removeView(View view) {
        if (openViews.contains(view)) {
            openViews.removeElement(view);
            view.dispose();
            view.redraw();
        } else if (icons.contains(view)) {
            icons.removeElement(view);
            view.dispose();
            view.redraw();
        }
    }

    public void repaint(int x, int y, int width, int height) {
        viewer.repaint(x, y, width, height);
    }

    /**
     * Pushes the specified view to the back of all views
     * @param view  to lower
     */
    protected void lower(RootView view) {
        // move view to bottom of stack
        if (openViews.contains(view) && (openViews.firstElement() != view)) {
            openViews.removeElement(view);
            openViews.insertElementAt(view, 0);
            view.redraw();
        }
    }

    /**
     * Pulls the specified view to the top of all views
     * @param view to raise
     */
    protected void raise(RootView view) {
        // move view to top of stack
        if (openViews.contains(view) && (openViews.lastElement() != view)) {
            openViews.removeElement(view);
            openViews.addElement(view);
            view.redraw();
        }
    }

    protected void removeViewsFor(NakedObject object, Vector toRemove) {
        for (int i = 0; i < openViews.size(); i++) {
            ObjectView view = (ObjectView) openViews.elementAt(i);

            if (view.getObject().equals(object)) {
                toRemove.addElement(view);
            } else {
                view.removeViewsFor(object, toRemove);
            }
        }

        for (int i = 0; i < icons.size(); i++) {
            DesktopView view = (DesktopView) icons.elementAt(i);

            if (view instanceof ObjectView && ((ObjectView) view).getObject().equals(object)) {
                toRemove.addElement(view);
            }
        }
    }

    private View identifyView2(Location framePosition, View current) {
        for (int i = openViews.size() - 1; i >= 0; i--) {
            ObjectView openView = (ObjectView) openViews.elementAt(i);

            if ((openView != current) && openView.contains(framePosition)) {
                Location pp = new Location(framePosition);
                Location offset = openView.getLocation();
                pp.translate(-offset.x, -offset.y);

                return openView.identifyView(pp, (View) current);
            }
        }

        for (int i = icons.size() - 1; i >= 0; i--) {
            View icon = (View) icons.elementAt(i);

            if ((icon != current) && icon.contains(framePosition)) {
                View identifed = icon.identifyView(framePosition, (View) current);

                //    LOG.debug("identified icon " + identifed);
                return identifed;
            }
        }

        return null;
    }

    private void layoutOpenViews() {
        WorkspaceAlignment alignment = new CascadeAlignment();

        int noViews = openViews.size();
        View[] views = new View[noViews];

        for (int i = 0; i < views.length; i++) {
            views[i] = (View) openViews.elementAt(i);
        }

        alignment.align(views, this);
    }

    private void newInstance(NakedClass cls, Workspace frame, Location at, boolean openAView) {
        NakedObject object;

        try {
            object = cls.acquireInstance();

            // TODO remove this, and replace with Save option for non-persistent objects
            NakedObjectManager.getInstance().makePersistent(object);
            object.created();
            object.objectChanged();
        } catch (NotPersistableException e) {
            object = new NakedError("Failed to create instance of " + cls.fullName(), e);

            LOG.error("Failed to create instance of " + cls.fullName());
            e.printStackTrace();
        }

        if (openAView) {
            RootView view = ViewFactory.getViewFactory().createRootView(object);
            view.setLocation(at);
            frame.addRootView(view);
        } else {
            DesktopView icon = ViewFactory.getViewFactory().createIconView(object, null);
            icon.setLocation(at);
            frame.addIcon(icon);
        }
    }

    /**
     * a DebugInfo strategy that  is used to list the views in the specified application frame.
     */
    class DebugOpenViewList implements DebugInfo {
        public String getDebugData() {
            StringBuffer info = new StringBuffer();

			info.append("WORKSPACE\n");
			info.append("Bounds:    ");

			 Bounds bounds = getBounds();
			 info.append(bounds.width + "x" + bounds.height + "+" + bounds.x + "+" + bounds.y);

			 info.append("\nReq'd :    ");

			 Size required = getRequiredSize();
			 info.append(required.width + "x" + required.height);

			 info.append("\nPadding:   ");

			 Padding insets = getPadding();
			 info.append("top/bottom " + insets.top + "/" + insets.bottom + ", left/right " + insets.left +
				 "/" + insets.right);

			 info.append("\nBorder:    ");
			 info.append((border == null) ? "none" : border.debug(Workspace.this));


            // display details
            info.append("\n\nBACKGROUND\n");
            info.append("----------\n");
            info.append(background == null ? "none" : background.toString());
            info.append("\n");

            info.append("\nDESKTOP Layer\n");
            info.append("-------------\n");

            for (int i = 0; i < icons.size(); i++) {
                View vw = (View) icons.elementAt(i);
                info.append(vw.toString());
                info.append("\n");
            }

            info.append("\nOPEN VIEW Layer\n");
            info.append("---------------\n");

            for (int i = 0; i < openViews.size(); i++) {
                ObjectView vw = (ObjectView) openViews.elementAt(i);
                info.append(vw.toString());
                info.append("\n");
            }

			info.append("\nOVERLAY VIEW Layer\n");
			info.append("------------------\n");
			info.append(overlayView);
			info.append("\n");

            return info.toString();
        }

        public String getDebugTitle() {
            return "Workspace contents";
        }
    }

	public void showTextCursor() {
		viewer.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
	}
	
	public void showArrowCursor() {
		viewer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	public void showMoveCursor() {
		viewer.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
	}
	
	public void showResizeCursor() {
		viewer.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
	}
	
	public void showColumnResizeCursor() {
		viewer.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
	}

}
