package org.nakedobjects.viewer.skylark.metal;

import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.basic.IconGraphic;
import org.nakedobjects.viewer.skylark.basic.ObjectTitleText;
import org.nakedobjects.viewer.skylark.basic.RootIconSpecification;
import org.nakedobjects.viewer.skylark.basic.TitleText;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;

public class WindowBorder extends AbstractBorder {
	private final static Text TITLE_STYLE = Style.TITLE;
	private final static int LINE_THICKNESS = 5;
	private final static int BUTTON_HEIGHT = 13;
	private final static int BUTTON_WIDTH = BUTTON_HEIGHT + 2;
	private int baseline;
    private int titlebarHeight;
    private int padding = 2;

	private IconGraphic icon;
	private TitleText text;
	
	public WindowBorder(View wrappedView) {
		super(wrappedView);
		
		icon = new IconGraphic(this, TITLE_STYLE);
		text = new ObjectTitleText(this, TITLE_STYLE);
		titlebarHeight = icon.getSize().getHeight();

		left = LINE_THICKNESS;
		right = LINE_THICKNESS;
        top = LINE_THICKNESS + titlebarHeight;
		bottom = LINE_THICKNESS;

		baseline = icon.getBaseline() + LINE_THICKNESS;
	}
	
    public void debugDetails(StringBuffer b) {
        b.append("WindowBorder " + left + " pixels\n");
    	b.append("           titlebar " + (top - titlebarHeight) + " pixels");
    }
	
	public void draw(Canvas canvas) {
		Size s  = getSize();
		int x = left;
		int y = titlebarHeight;
		int width = s.getWidth();
		int height = s.getHeight();
		
		// blank background
		canvas.drawSolidRectangle(x, x, width - x - 1, height - x - 1, Style.SECONDARY3);

		// slightly rounded grey border
		canvas.drawRectangle(1, 0, width - 3, height - 1, Style.SECONDARY1);
		canvas.drawRectangle(0, 1, width - 1, height - 3, Style.SECONDARY1);
		
		for (int i = 2; i < left; i++) {
			canvas.drawRectangle(i, i, width - 2 * i - 1, height - 2 * i - 1,Style.SECONDARY1);
		}
		canvas.drawLine(x, top - 1, width - right, top - 1, Style.SECONDARY1);

		// vertical lines within border
		canvas.drawLine(2, 15, 2, height - 15, Style.BLACK);
		canvas.drawLine(3, 16, 3, height - 14, Style.PRIMARY1);
		canvas.drawLine(width - 3, 15, width - 3, height - 15, Style.BLACK);
		canvas.drawLine(width - 2, 16, width - 2, height - 14, Style.PRIMARY1);

		// horizontal lines within border
		canvas.drawLine(15, 2, width - 15, 2, Style.BLACK);
		canvas.drawLine(16, 3, width - 14, 3, Style.PRIMARY1);
		canvas.drawLine(15, height - 3, width - 15, height - 3, Style.BLACK);
		canvas.drawLine(16, height - 2, width - 14, height - 2, Style.PRIMARY1);
		
		// icon & title
		icon.draw(canvas, x, baseline + 1);
		x += icon.getSize().getWidth();
		text.draw(canvas, x, baseline + 1);

		// window buttons
		x = width - right - 3 * (BUTTON_WIDTH + padding) - 1;
		y = LINE_THICKNESS + padding;
		int w = BUTTON_WIDTH - 1;
		int h = BUTTON_HEIGHT - 1;
		canvas.drawRectangle(x + 1, y + 1, w, h, Style.WHITE);
		canvas.drawRectangle(x, y, w, h, Style.BLACK);
		canvas.drawLine(x + 5, y + 9,  x + 10, y + 9, Style.BLACK);
		canvas.drawLine(x + 5, y + 10,  x + 11, y + 10, Style.BLACK);

		x += BUTTON_WIDTH + padding;
		canvas.drawRectangle(x + 1, y + 1, w, h, Style.WHITE);
		canvas.drawRectangle(x, y, w, h, Style.SECONDARY1);
		canvas.drawRectangle(x + 2, y + 2,  8, 8, Style.SECONDARY2);
		canvas.drawLine(x + 3, y + 3,  x + 10, y + 3, Style.SECONDARY2);

		// close button
		x += BUTTON_WIDTH + padding;
		canvas.drawRectangle(x + 1, y + 1, w, h, Style.WHITE);
		canvas.drawRectangle(x, y, w, h, Style.SECONDARY1);
		canvas.drawLine(x + 4, y + 3,  x + 10, y + 9, Style.BLACK);
		canvas.drawLine(x + 5, y + 3,  x + 11, y + 9, Style.BLACK);
		canvas.drawLine(x + 10, y + 3,  x + 4, y + 9, Style.BLACK);
		canvas.drawLine(x + 11, y + 3,  x + 5, y + 9, Style.BLACK);

		// components
		super.draw(canvas);
	}
    
	
    public void firstClick(Click click) {
        Bounds bounds = new Bounds(getSize().getWidth() - right - 3 * (BUTTON_WIDTH + padding) - 1, LINE_THICKNESS + padding, BUTTON_WIDTH, BUTTON_WIDTH);
        if(bounds.contains(click.getLocation())) {
            View iconView = new RootIconSpecification().createView(getContent(), null);
            iconView.setLocation(getView().getLocation());
            getWorkspace().removeView(getView());
            getWorkspace().addView(iconView);
            return;
        } 

        bounds.translate(BUTTON_WIDTH + padding, 0);
        bounds.translate(BUTTON_WIDTH + padding, 0);
        if(bounds.contains(click.getLocation())) {
            dispose();
            return;
        }
        
        super.firstClick(click);
    }
    
    public int getBaseline() {
    	return wrappedView.getBaseline() + baseline + titlebarHeight;
	}
  
	public Size getRequiredSize() {
		Size size = super.getRequiredSize();
		
		size.ensureWidth(left + icon.getSize().getWidth() + text.getSize().getWidth() + 
		        padding + 3 * (padding + BUTTON_WIDTH)  + right);
		return size;
	}
	
	public void secondClick(Click click) {
 /*       if(click.getViewAreaType() == ViewAreaType.VIEW) {
            View iconView = new RootIconSpecification().createView(getContent(), null);
            iconView.setLocation(getView().getLocation());
            getWorkspace().removeView(getView());
            getWorkspace().addView(iconView);
        } else {
            
        }
        */
	    super.secondClick(click);
    }

 	public String toString() {
		return wrappedView.toString() + "/WindowBorder [width=" + left + "]";
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