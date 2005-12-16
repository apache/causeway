package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.object.Naked;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.utility.NakedObjectRuntimeException;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.CompositeViewBuilder;
import org.nakedobjects.viewer.skylark.CompositeViewSpecification;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Padding;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewAxis;

import java.util.Vector;

import org.apache.log4j.Logger;


public class CompositeView extends ObjectView {
    private static final Logger LOG = Logger.getLogger(CompositeView.class);
    private int buildCount = 0;
    private CompositeViewBuilder builder;
    private boolean buildInvalid = true;
    private boolean canDragView = true;
    private int layoutCount = 0;
    private boolean layoutInvalid = true;
    protected Vector views;

    public CompositeView(Content content, CompositeViewSpecification specification, ViewAxis axis) {
        super(content, specification, axis);
        views = new Vector();
        builder = specification.getSubviewBuilder();
    }

    public void refresh() {
        View views[] = getSubviews();
        for (int i = 0; i < views.length; i++) {
            views[i].refresh();
        }
    }

    public void addView(View view) {
        LOG.debug("adding " + view + " to " + this);

        views.addElement(view);
        view.setParent(getView());
        invalidateLayout();
    }

    public boolean canDragView() {
        return canDragView;
    }

    public String debugDetails() {
        DebugString b = new DebugString();
        b.append(super.debugDetails());
        b.appendTitle("Composite view");
        b.appendln(0, "Built", (buildInvalid ? "no" : "yes") + ", " + buildCount + " builds");
        b.appendln(0, "Laid out:  " + (layoutInvalid ? "no" : "yes") + ", " + layoutCount + " layouts");
        b.appendln(0, "Subviews");
        View views[] = getSubviews();
        for (int i = 0; i < views.length; i++) {
            View subview = views[i];
            b.appendln(4, subview.getSpecification().getName());
         //   b.appendln(8, subview.getSpecification().getClass().getName());
            b.appendln(8, "Bounds", subview.getBounds());
            b.appendln(8, "Required size ", subview.getRequiredSize());
            b.appendln(8, "Content", subview.getContent().getName());
    //        b.appendln(subview.getBounds() + " " + subview + ": " + subview.getContent() + "\n           ");
        }

        b.append("\n");

        return b.toString();
    }

    public void dispose() {
        View views[] = getSubviews();
        for (int i = 0; i < views.length; i++) {
            views[i].dispose();
        }
        super.dispose();
    }

    public void draw(Canvas canvas) {
        View views[] = getSubviews();
        for (int i = 0; i < views.length; i++) {
            View subview = views[i];
            Bounds bounds = subview.getBounds();
            if (AbstractView.debug) {	                   
                LOG.debug("compare: " + bounds +"  " + canvas);
            }
            if(canvas.overlaps(bounds)) {
	            //Canvas subCanvas = canvas.createSubcanvas();
	            Canvas subCanvas = canvas.createSubcanvas(bounds.getX(), bounds.getY(), bounds.getWidth() - 0 , bounds.getSize().getHeight());
	         //   subCanvas.offset(subview.getBounds().getX(), subview.getBounds().getY());
	            if (AbstractView.debug) {
	                LOG.debug("-- repainting " + subview );
	                LOG.debug("subcanvas " + subCanvas);
	            }
	            subview.draw(subCanvas);
	            if (AbstractView.debug) {	                   
	       //        canvas.drawRectangle(subviewBounds.getX(), subviewBounds.getY(), subviewBounds.getWidth() - 1, subviewBounds.getHeight() - 1, org.nakedobjects.viewer.skylark.Color.DEBUG_REPAINT_BOUNDS);
	            }
            }
        }
    }

    public int getBaseline() {
        View[] e = getSubviews();
        if (e.length == 0) {
            return 14;
        } else {
            View subview = e[0];
            return subview.getBaseline();
        }
    }

    public Size getRequiredSize() {
        Size size = builder.getRequiredSize(this);
        size.extend(getPadding());

        size.ensureHeight(18);
        size.ensureWidth(45);

        return size;
    }

    public View[] getSubviews() {
        View v[] = new View[views.size()];
        views.copyInto(v);
        return v;
    }
    
    public void invalidateContent() {
        buildInvalid = true;
        invalidateLayout();
    }

    public void invalidateLayout() {
        layoutInvalid = true;
        super.invalidateLayout();
    }

    /**
     * The default layout for composite views, which asks each subview to lay itself out first
     * before asking its own builder to layout its own views. The act of laying out the children
     * first ensures that the parent is big enough to accomodate all its children.
     */
    public void layout() {
        if (buildInvalid) {
            getViewManager().showWaitCursor();
            buildInvalid = false;
            builder.build(getView());
            buildCount++;
            getViewManager().showDefaultCursor();
        }
        if (layoutInvalid) {
            getViewManager().showWaitCursor();
            markDamaged();
            View views[] = getSubviews();
            for (int i = 0; i < views.length; i++) {
                views[i].layout();
            }

            layoutInvalid = false;

            Size size = getView().getRequiredSize();
            getView().setSize(size);

            builder.layout(getView());
             
            layoutCount++;
            markDamaged();
            getViewManager().showDefaultCursor();
        }
    }

    protected boolean isLayoutInvalid() {
        return layoutInvalid;
    }
    
    public View subviewFor(Location location) {
        Location l = new Location(location);
        Padding padding = getPadding();
        l.subtract(padding.getLeft(), padding.getTop());
        View views[] = getSubviews();
        for (int i = views.length - 1; i >= 0; i--) {
            if (views[i].getBounds().contains(l)) {
                return views[i];
            }
        }
        return null;
    }
    
    public View pickupView(Location location) {
        return canDragView ? super.pickupView(location) : null;
    }

    public void removeView(View view) {
        if (views.contains(view)) {
            LOG.debug("removing " + view + " from " + this);
            views.removeElement(view);
            markDamaged();
            invalidateLayout();
        } else {
            throw new NakedObjectRuntimeException(view + " not in " + getView());
        }
    }

    public void replaceView(View toReplace, View replacement) {
        for (int i = 0; i < views.size(); i++) {
            if (views.elementAt(i) == toReplace) {
                replacement.setParent(getView());
                replacement.setLocation(toReplace.getLocation());
                views.insertElementAt(replacement, i);
                invalidateLayout();
                toReplace.dispose();

                return;
            }
        }

        throw new NakedObjectRuntimeException(toReplace + " not found to replace");
    }

    public void setCanDragView(boolean canDragView) {
        this.canDragView = canDragView;
    }

    public String toString() {
        return "ObjectCompositeView" + getId();
    }

    public void update(Naked object) {
        LOG.debug("update notify on " + this);
        invalidateContent();
        //builder.build(getView());
        //invalidateLayout();
    }

    public ViewAreaType viewAreaType(Location location) {
        View subview = subviewFor(location);
        if(subview == null) {
            return ViewAreaType.VIEW;
        } else {
            location.subtract(subview.getLocation());
            return subview.viewAreaType(location);
        }
    }
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