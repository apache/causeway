package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.defaults.collection.AbstractTypedNakedCollectionVector;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.CompositeViewBuilder;
import org.nakedobjects.viewer.skylark.CompositeViewSpecification;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;
import org.nakedobjects.viewer.skylark.core.AbstractBuilderDecorator;
import org.nakedobjects.viewer.skylark.core.AbstractCompositeViewSpecification;

class TableBorder extends AbstractBorder {

    private int resizeColumn;


    public TableBorder(View view) {
        super(view);
        top = VPADDING + Style.LABEL.getHeight() + VPADDING;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas.createSubcanvas());     

        int y = VPADDING + Style.LABEL.getAscent();

        TableColumnAxis axis = ((TableColumnAxis) getViewAxis());
        
        int x = axis.getHeaderOffset();
        int columns = axis.getColumnCount();
        for (int i = 0; i < columns; i++) {
            canvas.drawText(axis.getColumnName(i), x + HPADDING, y, Style.SECONDARY1, Style.LABEL);
            canvas.drawLine(x, 0, x, getSize().getHeight() - 1, Style.SECONDARY2);
            x += axis.getColumnWidth(i);
        }
        canvas.drawRectangle(0, VPADDING + Style.LABEL.getHeight(), getSize().getWidth() - 1, getSize().getHeight() - top - 1, Style.SECONDARY2);
    }

    public String toString() {
        return wrappedView.toString() + "/TableHeader";
    }
    
    public View identify(Location location) {
        getViewManager().getSpy().addTrace("Identify over column " + location);
        if(isOverColumnBorder(location)) {
            getViewManager().getSpy().addAction("Identified over column ");
           return getView();
        }
        return super.identify(location);
    }
    
	
	public void mouseMoved(Location at) {
		if(isOverColumnBorder(at)) {
			getViewManager().showResizeRightCursor();
		} else {
		    super.mouseMoved(at);
			getViewManager().showDefaultCursor();
		} 
	}
	
	private boolean isOverColumnBorder(Location at) {
		int x = at.getX();
		return getColumnAt(x) >= 0;
	}
	
	private int getColumnAt(int xPosition) {
        getViewManager().getSpy().addTrace("Looking for column at " + xPosition);
	    TableColumnAxis axis = ((TableColumnAxis) getViewAxis());
	    for (int i = 0, width = axis.getHeaderOffset(), cols = axis.getColumnCount(); i < cols; i++) {
            width += axis.getColumnWidth(i);
            getViewManager().getSpy().addTrace("Checking bounda around " + width);
            if(xPosition >= width - 1 && xPosition <= width + 1) {
                getViewManager().getSpy().addTrace("Identified column " + i);
                return i;
            }
        }
	    return -1;
	}

	public View dragFrom(InternalDrag drag) {
	    if(isOverColumnBorder(drag.getMouseLocationRelativeToView())) {
	        resizeColumn = getColumnAt(drag.getMouseLocationRelativeToView().getX());
	        
	        return new ViewResizeOutline(drag, this, ViewResizeOutline.RIGHT);
	    } else {
	        return super.dragFrom(drag);
	    }
	}
	
	public void dragTo(InternalDrag drag) {
		getViewManager().showDefaultCursor();

	    TableColumnAxis axis = ((TableColumnAxis) getViewAxis());
	    int totalWidth = axis.getHeaderOffset();
	    for (int i = 0; i < resizeColumn; i++) {
            totalWidth += axis.getColumnWidth(i);
        }
	    
	    int newWidth = drag.getMouseLocationRelativeToView().getX() - totalWidth;
		getViewManager().getSpy().addAction("Resize column to " + newWidth);
	    axis.setWidth(resizeColumn, newWidth);
	    
	    axis.invalidateLayout();
	}
	
	
	public ViewAreaType viewAreaType(Location at) {
		int x = at.getX();
		
		if(getColumnAt(x) >= 0) {
			return ViewAreaType.INTERNAL;
		} else {
			return super.viewAreaType(at);
		}
	}

    
    
}

class TableHeaderBuilder extends AbstractBuilderDecorator {
    // could this be the axis?
    public TableHeaderBuilder(CompositeViewBuilder design) {
        super(design);
    }

    public View createCompositeView(Content content, CompositeViewSpecification specification, ViewAxis axis) {
        View view = wrappedBuilder.createCompositeView(content, specification, axis);
        return new TableBorder(view);
    }
}

public class TableSpecification extends AbstractCompositeViewSpecification implements SubviewSpec {
    private ViewSpecification rowSpecification = new TableRowSpecification();

    public TableSpecification() {
        builder = new TableHeaderBuilder(new StackLayout(new CollectionElementBuilder(this, true)));
    }

    public View createView(Content content, ViewAxis axis) {
        AbstractTypedNakedCollectionVector coll = (AbstractTypedNakedCollectionVector) ((ObjectContent) content).getObject();
        NakedObjectSpecification elementSpecification = NakedObjectSpecificationLoader.getInstance().loadSpecification(coll.getElementSpecification().getFullName());
        NakedObject exampleObject = (NakedObject) elementSpecification.acquireInstance();
        FieldSpecification[] viewFields = elementSpecification.getVisibleFields(exampleObject, ClientSession.getSession());
        TableColumnAxis tableAxis = new TableColumnAxis(viewFields, 120);

        View table = super.createView(content, tableAxis);
        tableAxis.setRoot(table);

        View view = new ScrollBorder(table);
        return view;
    }
    
    public boolean canDisplay(Naked object) {
        return object instanceof AbstractTypedNakedCollectionVector;
    }

    public View createSubview(Content content, ViewAxis axis) {
        return rowSpecification.createView(content, axis);
    }

    public String getName() {
        return "Standard Table";
    }

    public boolean isReplaceable() {
        return false;
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