package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.ContentDrag;
import org.nakedobjects.viewer.skylark.Drag;
import org.nakedobjects.viewer.skylark.DragStart;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.basic.DragContentIcon;
import org.nakedobjects.viewer.skylark.basic.IconGraphic;
import org.nakedobjects.viewer.skylark.basic.ObjectTitleText;
import org.nakedobjects.viewer.skylark.basic.TitleText;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;
import org.nakedobjects.viewer.skylark.core.AbstractView;

public class IconBorder extends AbstractBorder {
	private final static Text TITLE_STYLE = Style.TITLE;
	private int baseline;
    private int titlebarHeight;
    private int padding = 0;

	private IconGraphic icon;
	private TitleText text;
	
	public IconBorder(View wrappedView) {
		super(wrappedView);
		
		icon = new IconGraphic(this, TITLE_STYLE);
		text = new ObjectTitleText(this, TITLE_STYLE);
		titlebarHeight = icon.getSize().getHeight() + 1;

		top = titlebarHeight;
		
		baseline = icon.getBaseline() + 1;
	}
	
    public void debugDetails(StringBuffer b) {
        b.append("IconBorder " + left + " pixels\n");
    	b.append("           titlebar " + (top - titlebarHeight) + " pixels");
    	super.debugDetails(b);
    }
    
    public Drag dragStart(DragStart drag) {
        if(overBorder(drag.getLocation())) {
            View dragOverlay = new DragContentIcon(getContent());
            return new ContentDrag(this, drag.getLocation(), dragOverlay);
        } else {
            return super.dragStart(drag);
        }
    }
	
	public void draw(Canvas canvas) {
	    int x = left + HPADDING;
		
	    if(AbstractView.debug) {
	        canvas.drawDebugOutline(new Bounds(getSize()), baseline, Color.DEBUG_DRAW_BOUNDS);
	    }
	    
		// icon & title
		icon.draw(canvas, x, baseline);
		x += icon.getSize().getWidth();
        x += View.HPADDING;
		text.draw(canvas, x, baseline);

		// components
		super.draw(canvas);
	}
    	
    public int getBaseline() {
    	return wrappedView.getBaseline() + baseline + titlebarHeight;
	}
  
	public Size getRequiredSize() {
		Size size = super.getRequiredSize();
		
		size.ensureWidth(left + icon.getSize().getWidth() + View.HPADDING + text.getSize().getWidth() + 
		        padding + right);
		return size;
	}
	
	public ViewAreaType viewAreaType(Location mouseLocation) {
	    Bounds title = new Bounds(new Location(), icon.getSize());
	    title.extendWidth(left);
	    title.extendWidth(text.getSize().getWidth());
	    if(title.contains(mouseLocation)) {
	        return ViewAreaType.CONTENT;
	    } else {
	        return super.viewAreaType(mouseLocation);
	    }
    }

 	public String toString() {
		return wrappedView.toString() + "/WindowBorder [" + getSpecification() + "]";
	}
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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