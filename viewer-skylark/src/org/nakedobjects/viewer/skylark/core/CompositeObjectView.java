package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.CompositeViewBuilder;
import org.nakedobjects.viewer.skylark.CompositeViewSpecification;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewAxis;

import java.util.Vector;

import org.apache.log4j.Logger;


public class CompositeObjectView extends ObjectView {
    private static final Logger LOG = Logger.getLogger(CompositeObjectView.class);
    private int buildCount = 0;

    private CompositeViewBuilder builder;
    private boolean buildInvalid = true;
    private boolean canDragView = true;
    private int layoutCount = 0;
    private boolean layoutInvalid = true;
    protected Vector views;

    public CompositeObjectView(Content content, CompositeViewSpecification specification, ViewAxis axis) {
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
        StringBuffer b = new StringBuffer();
        b.append(super.debugDetails());
        b.append("\nBuilt:     " + (buildInvalid ? "no" : "yes") + ", " + buildCount + " builds");
        b.append("\nLaid out:  " + (layoutInvalid ? "no" : "yes") + ", " + layoutCount + " layouts");
        b.append("\nSubviews:  ");

        View views[] = getSubviews();

        for (int i = 0; i < views.length; i++) {
            View subview = views[i];
            b.append(subview.getBounds() + " " + subview + ": " + subview.getContent() + "\n           ");
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
            if (AbstractView.DEBUG) {	                   
                LOG.debug("compare: " + bounds +"  " + canvas);
            }
            if(canvas.overlaps(bounds)) {
	            //Canvas subCanvas = canvas.createSubcanvas();
	            Canvas subCanvas = canvas.createSubcanvas(bounds.getX(), bounds.getY(), bounds.getWidth() - 0 , bounds.getSize().getHeight());
	         //   subCanvas.offset(subview.getBounds().getX(), subview.getBounds().getY());
	            if (AbstractView.DEBUG) {
	                LOG.debug("-- repainting " + subview );
	                LOG.debug("subcanvas " + subCanvas);
	            }
	            subview.draw(subCanvas);
	            if (AbstractView.DEBUG) {	                   
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

    public void layout() {
        if (buildInvalid) {
            buildInvalid = false;
            builder.build(getView());
            buildCount++;
        }
        if (layoutInvalid) {
            markDamaged();
            View views[] = getSubviews();

            for (int i = 0; i < views.length; i++) {
                View subview = views[i];
                subview.layout();
            }

            layoutInvalid = false;

//            Size size = builder.getRequiredSize(getView());
            Size size =getView().getRequiredSize();
            getView().setSize(size);

            builder.layout(getView());
             
            layoutCount++;
            markDamaged();
        }
    }

    protected boolean isLayoutInvalid() {
        return layoutInvalid;
    }
    
    protected View subviewFor(Location location) {
        View views[] = getSubviews();
        for (int i = views.length - 1; i >= 0; i--) {
            if (views[i].getBounds().contains(location)) {
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
            markDamaged();
            views.removeElement(view);
            invalidateLayout();
        } else {
            throw new NakedObjectRuntimeException(view + " not in " + getView());
        }
    }

    public void replaceView(View toReplace, View replacement) {
        for (int i = 0; i < views.size(); i++) {
            if (views.elementAt(i) == toReplace) {
                toReplace.dispose();
                replacement.setParent(getView());
                replacement.setLocation(toReplace.getLocation());
                views.insertElementAt(replacement, i);
                invalidateLayout();

                return;
            }
        }

        throw new NakedObjectRuntimeException(toReplace + " not found to replace");
    }

    public void setCanDragView(boolean canDragView) {
        this.canDragView = canDragView;
    }

    protected String title() {
        return getContent().title();
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
            boolean overTitle = new Bounds(5, 0, Style.TITLE.stringWidth(title()), 24).contains(location);
            return overTitle ? ViewAreaType.CONTENT : ViewAreaType.VIEW;
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