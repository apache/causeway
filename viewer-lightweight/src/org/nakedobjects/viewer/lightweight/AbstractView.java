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

import java.awt.Graphics;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.nakedobjects.utility.Configuration;


/**
 * A View is the visual representation of the object the user is working with.
 * A View knows who its parent is (another view), and which frame it belongs
 * to.  If the view is a part of the frame (it is a top-level view) then its
 * parent wil be <code>null</code>.
 *
 * A View also knows it position within the frame.
 *
 * @author rcm
 *
 */
public abstract class AbstractView implements View {
    private static final Logger LOG = Logger.getLogger(AbstractView.class);

    /** Vertical padding (=) between two components */
    public static final int VPADDING = Configuration.getInstance().getInteger("viewer.lightweight.vpadding",
            2);

    /** Horizontal padding (||) between two components */
    public static final int HPADDING = Configuration.getInstance().getInteger("viewer.lightweight.hpadding",
            2);
    public static final int DRAG_HANDLE_WIDTH = Configuration.getInstance().getInteger("viewer.lightweight.handlewidth",
            18);
    private static Workspace workspace;
    protected static final int ICON_SIZE = 16;
    public static boolean DEBUG = false;
    private static int nextId = 0;
    private Border border;
    private Bounds repaintArea;
    private CompositeView parent;
    private Vector controls = new Vector();
    private boolean isLayoutInvalid = true;
    private int height = -1;
    private int id;
    private int width = -1;
    private int x; // coordinate relative parent view
    private int y;

    public final Location getAbsoluteLocation() {
        if (getParent() == null) {
            return getLocation();
        } else {
            Location p = getParent().getAbsoluteLocation();
            p.translate(getLocation().x, getLocation().y);

            return p;
        }
    }

    public void setBorder(Border border) {
        this.border = border;
    }

    public Border getBorder() {
        return border;
    }

    public final void setBounds(Location point, Size size) {
        this.x = point.x;
        this.y = point.y;
        this.width = size.width;
        this.height = size.height;
    }

    public final Bounds getBounds() {
		return new Bounds(x, y, width, height);
    }

    public int getId() {
        return id;
    }

    public boolean isLayoutInvalid() {
        return isLayoutInvalid;
    }

    public void setLayoutValid() {
        isLayoutInvalid = false;
    }

    /**
     * Sets the location of this view.  If this view is being moved then
     * <code>layout</code> should be called subsequently.
     *
     * @param point
     */
    public final void setLocation(Location point) {
        this.x = point.x;
        this.y = point.y;
    }

    public final Location getLocation() {
        return new Location(x, y);
    }

    /**
     * Returns the postion of the baseline for adjacent label.  If the label is
     * drawn on this baseline when top of the label rectangle and this view's
     * rectangle are aligned then the label should share the baseline with
     * whatever text is shown within the view.
     *
     * @return int the baseline to draw the label at.
     */
    public int getBaseline() {
        return 0;
    }

    /*
     * returns the name of the class
     */
    public String getName() {
        String n = getClass().getName();

        return n.substring(n.lastIndexOf('.') + 1);
    }

    public Padding getPadding() {
        if (border == null) {
            return new Padding(0, 0, 0, 0);
        } else {
            return border.getPadding(this);
        }
    }

    /**
     * Sets the parent of the view.
     */
    public void setParent(CompositeView parent) {
        this.parent = parent;
    }

    /**
     * Returns the View that this view belongs to.  If <code>null</code> is
     * returned then this view is a top-level view and is shown directly on the
     * frame.
     */
    public CompositeView getParent() {
        return parent;
    }

    public final void setSize(Size size) {
        this.width = size.width;
        this.height = size.height;
    }

    public final Size getSize() {
//        if (width == -1 && height == -1) {
//            setSize(getRequiredSize());
//        }
//
        return new Size(width, height);
    }

    /**
     * By default a vew is deemed to open, showing its content.
     */
    public boolean isOpen() {
        return true;
    }

    /**
     * By default a view can be replaced by another view representing the same object
     */
    public boolean isReplaceable() {
        return true;
    }

    /**
     * Returns the enclosing ApplicationFrame that this view is part of.
     */
    public Workspace getWorkspace() {
        return workspace;
    }

    /**
     * Adds a control to this view.  If the specified control already exists for this view then it is ignored.
     * @param control
     */
    public void addControl(Control control) {
        if (!controls.contains(control)) {
            controls.addElement(control);
        }
    }

    /**
     * Returns true if the  pointer is within this objects bounds.
     */
    public final boolean contains(Location mousePosition) {
        return getBounds().contains(mousePosition);
    }

    public void calculateRepaintArea() {
        if (repaintArea == null) {
            repaintArea = new Bounds(getAbsoluteLocation(), getSize());
        } else {
            repaintArea = repaintArea.union(new Bounds(getAbsoluteLocation(), getSize()));
        }
    }

    public Canvas createCanvas(Graphics g) {
        return new Canvas(g, getSize().getWidth(), getSize().getHeight());
    }

    /**
     * Returns debug details about this view.
     *
     * @return String
     */
    public String debugDetails() {
        StringBuffer b = new StringBuffer();

        b.append("View:      ");

        String name = getClass().getName();
        b.append(name.substring(name.lastIndexOf('.') + 1) + getId());

        b.append("\nParent:    ");

        String parent = getParent() == null ? "none"
                                            : getParent().getClass().getName() +
            ((ObjectView) getParent()).getId();
        b.append(parent);

        b.append("\nWorkspace: ");

        String workspace = getWorkspace() == null ? ""
                                                  : getWorkspace().getClass().getName() +
            getWorkspace().getId();
        b.append(workspace.substring(workspace.lastIndexOf('.') + 1) + getId());

        b.append("\nBounds:    ");

        Bounds bounds = getBounds();
        b.append(bounds.width + "x" + bounds.height + "+" + bounds.x + "+" + bounds.y);

        b.append("\nReq'd :    ");

        Size required = getRequiredSize();
        b.append(required.width + "x" + required.height);

        b.append("\nPadding:    ");

        Padding insets = getPadding();
        b.append("top/bottom " + insets.top + "/" + insets.bottom + ", left/right " + insets.left +
            "/" + insets.right);

        b.append("\nBorder:    ");
        b.append((border == null) ? "none" : border.debug(this));

        b.append("\nBaseline:  ");
        b.append(getParent() == null ? "none" : "" + getBaseline());

        b.append("\nControls:  ");
        b.append(controls);

        b.append("\nLaid out:  ");
        b.append(!isLayoutInvalid);

		b.append("\nDrawing");
		draw(new DebugCanvas(b));
		b.append("\n");

        return b.toString();
    }

    /**
     * Default implementation, which does nothing,  override when needed.
     */
    public void dispose() {
    }

    /**
     * Default implementation, which does nothing,  override when needed.
     */
    public void entered() {
    }

    /**
     * Default implementation, which does nothing,  override when needed.
     */
    public void enteredSubview() {
    }

    /**
     * Default implementation, which does nothing,  override when needed.
     */
    public void exited() {
    }

    /**
     * Default implementation, which does nothing,  override when needed.
     */
    public void exitedSubview() {
    }

    /**
     * Called when the user clicks the mouse buttone within this view.
     *
     * <p>Passes on to the control at the mouse location.</p>
     */
    public void firstClick(Click click) {
        if (border != null) {
            border.firstClick(this, click);
        }

        for (int i = 0; i < controls.size(); i++) {
            Control control = (Control) controls.elementAt(i);
            Location at = click.getLocation();

            if (control.getBounds().contains(at)) {
                control.invoke(getWorkspace(), this, at);

                return;
            }
        }
    }

    /**
     * Returns the view that mouse pointer is over.  If it is over this view and
     * not over any of it's components then this views reference  is returned.
     * Returns this view; should be overriden for container views.
     * @param mouseLocationer
     * @param current
     * @return View this view
     */
    public View identifyView(Location mouseLocationer, View current) {
        return this;
    }

    /**
     * Default behaviour, returning true to indicate that the view itself is the target of the user's action - rather than
     * what this view represents.
     */
    public boolean indicatesForView(Location mouseLocation) {
        if (border == null) {
            return true;
        } else {
            Bounds r = new Bounds(getSize());
            Padding insets = border.getPadding(this);
            r.x += insets.left;
            r.y += insets.top;
            r.width -= insets.left + insets.right;
            r.height -= insets.top + insets.bottom;

            return !r.contains(mouseLocation);
        }
    }

    public void invalidateLayout() {
        isLayoutInvalid = true;
        if (parent != null) {
            parent.invalidateLayout();
        }
    }

    public final void validateLayout() {
        if (getParent() == null) {
            layout();
            setSize(getRequiredSize());
            getWorkspace().limitBounds(this);
            redraw();
        } else {
            getParent().validateLayout();
        }
    }

    public void draw(Canvas canvas) {
        // fill in background
        if (!transparentBackground()) {
            Size size = getSize();
            canvas.drawFullRectangle(0, 0, size.width, size.height, backgroundColor());
        }

        if (border != null) {
            border.draw(this, canvas);
        }

        // draw controls
        for (int i = 0; i < controls.size(); i++) {
            Control control = (Control) controls.elementAt(i);
            control.paint(canvas);
        }

        Bounds area = new Bounds(getAbsoluteLocation(), getSize());

        if (!area.equals(repaintArea)) {
            repaintArea = area;

            // LOG.debug("paint() new area " + repaintArea + " for " + this);
        }
    }

    /**
     * This method is called when the frame, or the parent view, needs this
     * view to determine is spatial requirements and to lay out its compnents..
     */
    public void layout() {
        if (isLayoutInvalid()) {
            setLayoutValid();
        }
    }

    public void limitBounds() {
        View top = parentOf(this);
        getWorkspace().limitBounds(top);
    }
    
    public void menuOptions(MenuOptionSet options) {
	}

    /**
     * Called as the mouse is moved around within this view.    Does nothing;
     * should be overriden when needed.
     * @param at          the position relative to the top-left of this view
     */
    public void mouseMoved(Location at) {
    }

    public void print(Canvas canvas) {
        draw(canvas);
    }

    /**
    * Removes a control from this view.  If the specified control does not exist for this view then the request is ignored.
    * @param control
    */
    public void removeControl(Control control) {
        if (controls.contains(control)) {
            controls.removeElement(control);
        }
    }

    /**
     * Calls repaint() on the the top-level owner of this view.
     *
     */
    public void repaintAll() {
/*        View top = parentOf(this);
        LOG.debug("Requested repaint of parent veiw: " + top);
        top.redraw();
  */  }

    /**
     * Requests that the frame repaints this view, and only this view.
     */
    public void redraw() {
        ByteArrayOutputStream st = new ByteArrayOutputStream();
        PrintWriter wrt = new PrintWriter(st);
        new RuntimeException().printStackTrace(wrt);
        wrt.flush();
        
        StringTokenizer tk = new StringTokenizer(st.toString(), "\n");
        tk.nextToken();
        tk.nextToken();
        LOG.debug("Requested repaint of view: " + this + " " + tk.nextToken());

        Bounds area = repaintArea;
        calculateRepaintArea();

        if (DEBUG && area != null && !area.equals(repaintArea)) {
            LOG.debug("  Extended repaint area: " + repaintArea + " for " + this);
        }

        getWorkspace().repaint(repaintArea.x, repaintArea.y, repaintArea.width, repaintArea.height);
    }

    /**
     * Called when the user double-clicked this view.  This method will have
     * been preceded by a call to <code>click</code>.  
     * 
     * <p>Does nothing; should be
     * overriden when needed.</p>
     */
    public void secondClick(Click click) {
    }

    /**
     * Called when the user triple-clicks the mouse buttone within this view.
     * This method will have been preceded by a call to
     * <code>doubleClick</code>.
     *
     * <p>Does nothing; should be overriden when needed.</p>
     */
    public void thirdClick(Click click) {
    }

    protected static void setWorkspace(Workspace workspace) {
        AbstractView.workspace = workspace;
    }

    /**
     * Assigns a unique ID to this component
     */
    protected void assignId() {
        id = nextId++;
    }

    protected Color backgroundColor() {
        return isLayoutInvalid() ? new Color(0xcc99cc) : Style.VIEW_BACKGROUND;
    }

    protected int defaultFieldHeight() {
        int iconSize = Style.NORMAL.getHeight() * 120 / 100;
        int height = AbstractView.VPADDING * 2 + iconSize;

        return height;
    }

    protected boolean transparentBackground() {
        return true;
    }

    /**
     * Called when the user is dragging the mouse over this view (but not
     * dragging an view, eg when rubber-banding.    Does nothing; should be
     * overriden when needed.
     */
    void select(Location point) {
    }

    private View parentOf(View child) {
        if (getParent() == null) {
            return child;
        } else {
            return getParent();
        }
    }
}
