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

public abstract class OpenOptionFieldBorder extends AbstractBorder {
    private boolean over;

    public OpenOptionFieldBorder(View wrappedView) {
        super(wrappedView);
        right = 16;
    }

    public void draw(Canvas canvas) {
        Size size = getSize();
        int x = size.getWidth() - right + 5;
        int y = (size.getHeight() - 6) / 2;
        Color color = over ?  Style.SECONDARY1 : Style.PRIMARY2;

        Shape triangle = new Shape(0, 0);
        triangle.addVertex(5, 6);
        triangle.addVertex(11, 0);

        canvas.drawSolidShape(triangle, x, y, color);
        
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
        float x = click.getLocation().getX() - 2;
        float boundary = getSize().getWidth() - right;
        if (x >= boundary) {
            View overlay = createOverlay();

            Size size = overlay.getRequiredSize();
            size.ensureWidth(getSize().getWidth());
            overlay.setSize(size);

            Location location = getAbsoluteLocation();
            location.add(getView().getPadding().getLeft(), getSize().getHeight());
            overlay.setLocation(location);

            overlay.markDamaged();
            getViewManager().setOverlayView(overlay);
            
            overlay.layout();
        }
    }

    protected abstract View createOverlay();
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