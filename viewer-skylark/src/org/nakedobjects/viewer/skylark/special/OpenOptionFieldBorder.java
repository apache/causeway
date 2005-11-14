package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Shape;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;
import org.nakedobjects.viewer.skylark.core.BackgroundTask;
import org.nakedobjects.viewer.skylark.core.BackgroundThread;

public abstract class OpenOptionFieldBorder extends AbstractBorder {
    private boolean over;

    public OpenOptionFieldBorder(View wrappedView) {
        super(wrappedView);
        right = 18;
    }

    public void draw(Canvas canvas) {
        Size size = getSize();
        int x = size.getWidth() - right + 5 - HPADDING;
        int y = (size.getHeight() - 6) / 2;

        if(isAvailable()) {
            Shape triangle = new Shape(0, 0);
            triangle.addVertex(6, 6);
            triangle.addVertex(12, 0);
            
            canvas.drawShape(triangle, x, y, Style.SECONDARY2);
            if(over) {
                Color color = over ?  Style.SECONDARY1 : Style.PRIMARY2;
                canvas.drawSolidShape(triangle, x, y, color);
            }
        }
        
        super.draw(canvas);
    }
    
    public void mouseMoved(Location at) {
        if(at.getX() >= getSize().getWidth() - right) {
            getViewManager().showArrowCursor();
            if(!over) {
                markDamaged();
            }
            over = true;
        } else {
            if(over) {
                markDamaged();
            }
            over = false;
            super.mouseMoved(at);
        }
    }

    public void exited() {
        if(over) {
            markDamaged();
        }
        over = false;
       super.exited();
    }
    
    public void firstClick(Click click) {
        if(isAvailable()) {
	        float x = click.getLocation().getX() - 2;
	        float boundary = getSize().getWidth() - right;
	        if (x >= boundary) {
                
                BackgroundThread.run(this, new BackgroundTask() {
                    public void execute() {
        	            View overlay = createOverlay();
        	
        	            Size size = overlay.getRequiredSize();
        	            size.ensureWidth(getSize().getWidth());
        	            overlay.setSize(size);
        	
        	            Location location = getView().getAbsoluteLocation();
        	            location.add(getView().getPadding().getLeft() - 1, getSize().getHeight() + 2);
        	            overlay.setLocation(location);
        	
        	            overlay.layout();
        	            getViewManager().setOverlayView(overlay);
                    }
                    
                    public String getName() {
                        return "Opening lookup";
                    }
                    
                    public String getDescription() {
                        return "";
                    }
                });
	        }
        }
    }
    
    protected boolean isAvailable() {
        return true;
    }

    public Size getRequiredSize() {
        Size size = super.getRequiredSize();
        size.extendWidth(HPADDING);
        return size;
    }
    
    protected abstract View createOverlay();
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