package org.nakedobjects.viewer.skylark.special;


import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewDrag;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractView;


class TreeBrowserFrame extends AbstractView implements ViewAxis {
    private ViewSpecification mainViewFormSpec;
    private ViewSpecification mainViewTableSpec;
    private boolean invalidLayout = true;
    private View left;
    private View right;
    private View selectedNode;

    protected TreeBrowserFrame(Content content, ViewSpecification specification) {
        super(content, specification, null);
        
        mainViewFormSpec = new TreeBrowserFormSpecification();
        mainViewTableSpec = new InternalTableSpecification();
    }

    public String debugDetails() {
        StringBuffer b = new StringBuffer();
        b.append(super.debugDetails());
        b.append("\nBrowser:   ");
        b.append(this);
        b.append("\n           left " + left);
        b.append("\n           right " + right);

        b.append("\n\n");

        return b.toString();
    }

    public void dispose() {
        left.dispose();

        if (right != null) {
            right.dispose();
        }

        super.dispose();
    }

    public void draw(Canvas canvas) {
        Bounds bounds = left.getBounds();
        Canvas subCanvas = canvas.createSubcanvas(bounds);
        left.draw(subCanvas);

    	int y1 = getPadding().getTop();
    	int y2 = getSize().getHeight();
          for (int i = 0; i < 3; i++) {
        	int x = bounds.getWidth() + i;
			canvas.drawLine(x, y1, x, y2, Style.SECONDARY2 );
		}
        
        if (right != null) {
            bounds = right.getBounds();
            subCanvas = canvas.createSubcanvas(bounds);
            right.draw(subCanvas);
        }
    }

    public Size getRequiredSize() {
        Size size = left.getRequiredSize();
        size.extend(getPadding());
        size.extendWidth(5);
        
        if (right != null) {
            Size rightSize = right.getRequiredSize();
            size.ensureHeight(rightSize.getHeight());
            size.extendWidth(rightSize.getWidth());
        } else {
            size.extendWidth(150);
        }

        return size;
    }

    public View getSelectedNode() {
        return selectedNode;
    }

    public View[] getSubviews() {
        return (right == null) ? new View[] { left } : new View[] { left, right };
    }

    public void invalidateLayout() {
        super.invalidateLayout();
        invalidLayout = true;
    }

    public void layout() {
        if (invalidLayout) {
            left.layout();
            left.setSize(left.getRequiredSize());

            if (right != null) {
                right.layout();
                right.setLocation(new Location(left.getSize().getWidth() + 5, 0));
                right.setSize(right.getRequiredSize());
            }

            invalidLayout = false;
        }
    }

    public View pickup(ViewDrag drag) {
        return super.pickup(drag);
    }

    public void replaceView(View toReplace, View replacement) {
        if (toReplace == left) {
            left = replacement;
            replacement.setParent(getView());
            invalidateLayout();
        } else {
            throw new NakedObjectRuntimeException();
        }
    }
    
    
    public void removeView(View view) {
        if(view == left) {
            left = null;
        } else if(view == right) {
            right = null;
        }
    }

    void initLeftPane(View view) {
        left = view;
        left.setParent(getView());
    }

    private void showInRightPane(View view) {
        right = view;
        right.setParent(getView());
        invalidateLayout();
    }

    public void setSelectedNode(View view) {
        NakedObject object = ((ObjectContent) view.getContent()).getObject();
        if(object != null) {
	        if(mainViewFormSpec.canDisplay(object)) {
		        selectedNode = view;
		        showInRightPane(mainViewFormSpec.createView(view.getContent(), null));
	        }
	        if(mainViewTableSpec.canDisplay(object)) {
		        selectedNode = view;
		        showInRightPane(mainViewTableSpec.createView(view.getContent(), null));
	        }
        }
   }

    public String toString() {
        return "TreeBrowserFrame" + getId();
    }
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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
