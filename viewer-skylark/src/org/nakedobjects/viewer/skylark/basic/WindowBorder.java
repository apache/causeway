package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;

public class WindowBorder extends AbstractBorder {
	private final static Text TITLE_STYLE = Style.TITLE;
	private int baseline;
	private int padding = 2;

	private int thickness;
	private IconGraphic icon;
	private TitleText text;
	
	public WindowBorder(int size, View wrappedView) {
		super(wrappedView);
		
		thickness = size;
		
		icon = new IconGraphic(this, TITLE_STYLE);
		text = new TitleText(this, TITLE_STYLE);
		int height = icon.getSize().getHeight();
		
		baseline = icon.getBaseline();
		
		left = size;
		right = size;
		top = size + padding + height + padding + 1;
		bottom = size;
	}

	public WindowBorder(View wrappedView) {
		this(5, wrappedView);
	}

    public void debugDetails(StringBuffer b) {
        b.append("WindowBorder " + left + " pixels\n");
    	b.append("           titlebar " + (top - thickness) + " pixels");
    }
	
	public void draw(Canvas canvas) {
		Size s  = getSize();
		int x = left;
		int y = thickness;
		int width = s.getWidth();

		boolean active = getState().isObjectIdentified();
		for (int i = 0; i < left; i++) {
			canvas.drawRectangle(i, i, width - 2 * i - 1, s.getHeight() - 2 * i - 1, active ? Style.PRIMARY1 : Style.SECONDARY2);
		}
		canvas.drawSolidRectangle(x, y, width - left - right - 1, top - y - 1, active ? Style.PRIMARY3 : Style.SECONDARY3);
		canvas.drawLine(x, top - 1, width - right, top - 1, active ? Style.PRIMARY1 : Style.SECONDARY2);

		// icon & title
		icon.draw(canvas, x, baseline + thickness);
		x += icon.getSize().getWidth();
		text.draw(canvas, x, baseline + thickness);

		// handle
		x += text.getSize().getWidth() + padding; 
		if(getState().isViewIdentified() || getState().isObjectIdentified()) {
			int x2 = width - right - 2;
			for (int y1 = thickness + 2; y1 < top - 2; y1 += 3 ) {
				canvas.drawLine(x, y1, x2, y1, Style.SECONDARY2);
			}
		}
		
		// components
		super.draw(canvas);
	}
    
    public int getBaseline() {
    	return wrappedView.getBaseline() + baseline + thickness;
	}
  
	public Size getRequiredSize() {
		Size size = super.getRequiredSize();
		
		size.ensureWidth(left + icon.getSize().getWidth() + text.getSize().getWidth() + padding + right);
		return size;
	}
	
	public void secondClick(Click click) {
        if(click.getViewAreaType() == ViewAreaType.VIEW) {
            View iconView = new IconSpecification().createView(getContent(), null);
            iconView.setLocation(getView().getLocation());
            getWorkspace().removeView(getView());
            getWorkspace().addView(iconView);
        } else {
            super.secondClick(click);
        }
    }
	
 	public String toString() {
		return wrappedView.toString() + "/WindowBorder [width=" + left + "]";
	}

 	public ViewAreaType viewAreaType(Location mouseLocation) {
		int iconWidth = icon.getSize().getWidth();
 		int textWidth = text.getSize().getWidth();
 		
 		Bounds bounds = new Bounds(thickness, thickness, iconWidth + textWidth, top - thickness);
 		
 		if (bounds.contains(mouseLocation)) {
 			return ViewAreaType.CONTENT;
 		} else {
 			return super.viewAreaType(mouseLocation);
 			
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