package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedClassManager;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.InternalCollectionContent;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.basic.ClassOption;
import org.nakedobjects.viewer.skylark.basic.IconGraphic;
import org.nakedobjects.viewer.skylark.basic.TitleText;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;

public class TreeNodeBorder extends AbstractBorder {
	private static final int BOX_PADDING = 2;
	private static final int BOX_X_OFFSET = 5;
	private static final int BOX_SIZE = 8;
	private final static Text LABEL_STYLE = Style.NORMAL;
	private int baseline;
	private ViewSpecification replaceWithSpecification;
	private IconGraphic icon;
	private TitleText text;
	
	public TreeNodeBorder(View wrappedView, ViewSpecification replaceWith) {
		super(wrappedView);
		
		replaceWithSpecification = replaceWith;
		
		if(getContent() instanceof InternalCollectionContent) {
		    InternalCollection collection = ((InternalCollection) ((InternalCollectionContent) getContent()).getObject());
            String type = collection.getType().getName();
            final NakedClass nc = NakedClassManager.getInstance().getNakedClass(type);
		    icon = new IconGraphic(this, LABEL_STYLE) {
		        protected String iconName(NakedObject object) {
		            return nc.getIconName();
		        }
		    };
		    text = new TitleText(this, LABEL_STYLE) {
		        protected String title() {
                    return nc.getPluralName();
                }
		    };
		    
		} else {
		    icon = new IconGraphic(this, LABEL_STYLE);
		    text = new TitleText(this, LABEL_STYLE);
		}
		int height = icon.getSize().getHeight();
		
		baseline = icon.getBaseline();
		
		left = 22;
		right = 0;
		top = height;
		bottom = 0;
	}

    public void debugDetails(StringBuffer b) {
        b.append("TreeNodeBorder " + left + " pixels\n");
    	b.append("           titlebar " + (top) + " pixels");
    }
	
	public void draw(Canvas canvas) {
		boolean isOpen = getSpecification().isOpen();
		boolean canOpen = isOpen || ((TreeNodeSpecification) getSpecification()).canOpen(getContent());
		
		if(((TreeBrowserFrame) getViewAxis()).getSelectedNode() == getView()) {
			canvas.drawSolidRectangle(left, 0, getSize().getWidth() - left, top - 1, Style.PRIMARY2);
		}
		
		// lines
		int x = 0;
		int y = top / 2;
		canvas.drawLine(x, y, x + left, y, Style.SECONDARY2);
		if(canOpen) {
			x += BOX_X_OFFSET;
			canvas.drawLine(x, y, x + BOX_SIZE, y, Style.SECONDARY3);
			canvas.drawRectangle(x, y - BOX_SIZE / 2, BOX_SIZE, BOX_SIZE, Style.SECONDARY2);
			canvas.drawLine(x + BOX_PADDING, y, x + BOX_SIZE - BOX_PADDING, y, Style.SECONDARY2);
			if(!isOpen) {
				x += BOX_SIZE / 2;
				canvas.drawLine(x, y - BOX_SIZE / 2 + BOX_PADDING, x, y + BOX_SIZE / 2 - BOX_PADDING, Style.SECONDARY2);
			}
		}
		
		View[] nodes = getSubviews();
		if(nodes.length > 0) {
			int y1 = top / 2; 
			View node = nodes[nodes.length - 1];
			int y2 = top + node.getLocation().getY() + top / 2;
			canvas.drawLine(left - 1, y1, left - 1, y2, Style.SECONDARY2);
		}
		
		// icon & title
		x = left;
		icon.draw(canvas, x, baseline);
		x += icon.getSize().getWidth();
		text.draw(canvas, x, baseline);

		// components
		super.draw(canvas);
	}
    
	public void firstClick(Click click) {
		int x = click.getLocation().getX();
		int y = click.getLocation().getY();
		if(((TreeNodeSpecification) getSpecification()).canOpen(getContent()) && x >= BOX_X_OFFSET && x <= BOX_X_OFFSET + BOX_SIZE && y >= (top - BOX_SIZE) / 2 && y <= (top + BOX_SIZE) / 2) {
			View newView = replaceWithSpecification.createView(getContent(), getViewAxis());
			getParent().replaceView(getView(), newView);
		} else if(x > left) {
			((TreeBrowserFrame) getViewAxis()).setSelectedNode(getView());
		}
	}
	
 	public int getBaseline() {
    	return wrappedView.getBaseline() + baseline;
	}
  
	public Size getRequiredSize() {
		Size size = super.getRequiredSize();
		
		size.ensureWidth(left + icon.getSize().getWidth() + text.getSize().getWidth() + right);
		return size;
	}
	
 	public String toString() {
		return wrappedView.toString() + "/TreeNodeBorder";
	}

 	
 	public ViewAreaType viewAreaType(Location mouseLocation) {
		int iconWidth = icon.getSize().getWidth();
 		int textWidth = text.getSize().getWidth();
 		
 		Bounds bounds = new Bounds(0, 0, iconWidth + textWidth, top);
 		
 		if (bounds.contains(mouseLocation)) {
 			return ViewAreaType.CONTENT;
 		} else {
 			return super.viewAreaType(mouseLocation);
 			
 		}
 	}
 	
	public void menuOptions(MenuOptionSet options) {
	    if(getContent() instanceof InternalCollectionContent) {
	        InternalCollection collection = ((InternalCollectionContent) getContent()).getCollection();
	        NakedClass nakedClass = NakedClassManager.getInstance().getNakedClass(collection.getType().getName());
	        
	        ClassOption.menuOptions(nakedClass, options);
	        
		    // TODO same as InternalCollectionBorder
	    }
        super.menuOptions(options);
    }
	
    public void objectActionResult(Naked result, Location at) {
        if(getContent() instanceof InternalCollectionContent) {
	        //      TODO same as InternalCollectionBorder
            InternalCollectionContent internalCollectionContent = (InternalCollectionContent) getContent();
            OneToManyAssociation field = (OneToManyAssociation) internalCollectionContent.getField();
            NakedObject target = ((ObjectContent) getParent().getContent()).getObject();
        	field.setAssociation(target, (NakedObject) result);
        } else {
            super.objectActionResult(result, at);
        }
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