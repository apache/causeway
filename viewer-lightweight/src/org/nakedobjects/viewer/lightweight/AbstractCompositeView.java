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

import java.util.Enumeration;
import java.util.Vector;

import org.nakedobjects.viewer.lightweight.util.ViewFactory;
import org.nakedobjects.viewer.lightweight.view.RootBorder;


/**
 * A view that is made up of other views, e.g. a form or list.
 */
public abstract class AbstractCompositeView extends ObjectIconView 
		implements LayoutTarget, CompositeView {
    private Layout layout;
    private Vector components;

    public AbstractCompositeView() {
        components = new Vector();
    }

    /**
     * returns the baseline for the first component
     */
    public int getBaseline() {
        if (includeIcon()) {
            return super.getBaseline();
        } else {
            View[] components = getComponents();

            if (components.length == 0) {
                return 0;
            }

            int top = getPadding().top;

            return top + components[0].getBaseline();
        }
    }

    public InternalView[] getComponents() {
        InternalView[] v = new InternalView[components.size()];

        for (int i = 0; i < v.length; i++) {
            v[i] = (InternalView) components.elementAt(i);
        }

        return v;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public Layout getLayout() {
        return layout;
    }

    public Padding getPadding() {
        Padding padding = super.getPadding();

        if (includeIcon() || includeTitle()) {
            padding.extendTop(titleSize().getHeight());
        }

        return padding;
    }

    public Size getRequiredSize() {
        Size size = super.getRequiredSize();
        Size contents = layout.requiredSize(this);
        size.width = Math.max(size.width, contents.width);
        size.height = Math.max(size.height, contents.height);

        return size;
    }

    public void setRootViewIdentified() {
        View[] views = getComponents();

        for (int i = 0; i < views.length; i++) {
            if (views[i] instanceof ObjectView) {
                ((ObjectView) views[i]).setRootViewIdentified();
            }
        }

        super.setRootViewIdentified();
    }

    public void addView(InternalView view) {
        if (view != null) {
            view.setParent(this);
            components.addElement(view);
            view.invalidateLayout();
        }
    }

    public void calculateRepaintArea() {
        super.calculateRepaintArea();

        for (int i = 0; i < components.size(); i++) {
            View view = (View) components.elementAt(i);
            view.calculateRepaintArea();
        }
    }

    public void clearRootViewIdentified() {
        View[] views = getComponents();

        for (int i = 0; i < views.length; i++) {
            if (views[i] instanceof ObjectView) {
                ((ObjectView) views[i]).clearRootViewIdentified();
            }
        }

        super.clearRootViewIdentified();
    }

    public String debugDetails() {
        StringBuffer b = new StringBuffer();
        b.append(super.debugDetails());

        b.append("\nLayout:    ");

        String layout = getLayout() == null ? "none" : getLayout().getClass().getName();
        b.append(layout.substring(layout.lastIndexOf('.') + 1));

        b.append("\nSubviews:  ");

        debugFieldDetails(b, 0);

        return b.toString();
    }

    protected void debugFieldDetails(StringBuffer b, int level) {
    	if(level++ < 3) {
			View[] c = getComponents();
	
			for (int i = 0; i < c.length; i++) {
				b.append(level == 1 && i ==0 ? "" : "           " );
				b.append(level > 1 ? "    " : "");
				b.append(level > 2 ? "    " : "");
				String fieldType = c[i].getClass().getName();
				b.append(fieldType.substring(fieldType.lastIndexOf('.') + 1) + c[i].getId());
				b.append(" (" + c[i].getBounds() + ")");
				b.append("\n");
				if(c[i] instanceof AbstractCompositeView) {
					((AbstractCompositeView) c[i]).debugFieldDetails(b, level);
				}
			}
    	}
    }

    public void dispose() {
        View[] views = getComponents();

        for (int i = 0; i < views.length; i++) {
            views[i].dispose();
        }

        super.dispose();
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        Enumeration e = components.elements();

        while (e.hasMoreElements()) {
            View view = (View) e.nextElement();

            if (canvas.intersects(view.getBounds())) {
                Canvas c = canvas.createSubcanvas(view.getBounds().x, view.getBounds().y,
                        view.getBounds().width, view.getBounds().height);
                view.draw(c);
            }
        }
    }

    /**
     * Deals with the dropping of a contained and open view such that the view is removed from the containing view and is
     * placed in its own window.
     */
    public void dropView(ViewDrag drag) {
        if (! isRoot() && isOpen() && this instanceof RootView) {
            getWorkspace().showArrowCursor();

            InternalView replacement = ViewFactory.getViewFactory().createInternalView(getObject(),
                    getFieldOf(), true);
            CompositeView parent = (CompositeView) getParent();
            parent.replaceView((InternalView) this, replacement);
			parent.redraw();

            setBorder(new RootBorder());
            setLocation(drag.getViewLocation());
            setParent(null);
            getWorkspace().addRootView((RootView) this);
            invalidateLayout();
            validateLayout();
        } else { 
        	super.dropView(drag);
        }
    }

    public void focusNext(InternalView view) {
        View[] views = getComponents();
        int next = 0;

        // search for current view
        for (int i = 0; i < views.length; i++) {
            if (view == views[i]) {
                next = i + 1;

                break;
            }
        }

        // find next view
        for (int j = next; j < views.length; j++) {
            if (views[j] instanceof AbstractValueView && ((AbstractValueView) views[j]).canFocus()) {
                getWorkspace().makeFocus((AbstractValueView) views[j]);
                redraw();

                return;
            }
        }

        for (int j = 0; j < next; j++) {
            if (views[j] instanceof AbstractValueView && ((AbstractValueView) views[j]).canFocus()) {
                getWorkspace().makeFocus((AbstractValueView) views[j]);
                redraw();

                return;
            }
        }
    }

    public void focusPrevious(InternalView view) {
        View[] views = getComponents();
        int previous = 0;

        // search for current view
        for (int i = 0; i < views.length; i++) {
            if (view == views[i]) {
                previous = i - 1;

                break;
            }
        }

        // find next view
        for (int j = previous; j >= 0; j--) {
            if (views[j] instanceof AbstractValueView && ((AbstractValueView) views[j]).canFocus()) {
                getWorkspace().makeFocus((AbstractValueView) views[j]);
                redraw();

                return;
            }
        }

        for (int j = views.length - 1; j >= previous; j--) {
            if (views[j] instanceof AbstractValueView && ((AbstractValueView) views[j]).canFocus()) {
                getWorkspace().makeFocus((AbstractValueView) views[j]);
                redraw();

                return;
            }
        }
    }

    public View identifyView(Location p, View current) {
        for (int i = 0; i < components.size(); i++) {
            View view = (View) components.elementAt(i);

            if ((view != current) && view.contains(p)) {
                Location offset = view.getLocation();
                p.translate(-offset.x, -offset.y);

                //				LOG.debug("identified contained " + view + " " + p);
                return view.identifyView(p, current);
            }
        }

        return this;
    }

    public void layout() {
        if (isLayoutInvalid()) {
            Enumeration e = components.elements();

            while (e.hasMoreElements()) {
                View view = (View) e.nextElement();
                view.layout();
            }

            if (isRoot()) {
                limitBounds();
            }
            
			getLayout().layout(this);

			setLayoutValid();
        }
    }

    public void removeView(InternalView view) {
        components.removeElement(view);
        invalidateLayout();
    }

    public void replaceView(InternalView toReplace, InternalView replacement) {
        int index = components.indexOf(toReplace);

        if (index == -1) {
            throw new IllegalArgumentException("This container must contain " + toReplace);
        }

        replacement.setParent(this);
        components.removeElement(toReplace);
        components.insertElementAt(replacement, index);
        invalidateLayout();
        validateLayout();
    }

    /*
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
        components = new Vector();

        return super.clone();
    }

    protected void removeAllViews() {
        components.removeAllElements();
        invalidateLayout();
    }
}
