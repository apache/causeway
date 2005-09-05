package org.nakedobjects.viewer.skylark.example;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Drag;
import org.nakedobjects.viewer.skylark.DragStart;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.core.AbstractView;


public class TestCanvasView extends AbstractView {
    public TestCanvasView() {
        super(null, null, null);
    }
    
    public void draw(Canvas canvas) {
        canvas.clearBackground(this, Color.WHITE);
        
        int left = 10;
        int top = 10;
        int width = 50;
        int height = 90;
        canvas.drawRectangle(left, top, width, height, Color.BLACK);
        canvas.drawOval(left, top, width, height, Color.GREEN);
        canvas.drawLine(left, top, left + width, top + height, Color.RED);
        canvas.drawLine(left, top + height, left + width, top, Color.RED);
        
        left = 80;
        canvas.drawRectangle(left, top, width, height, Color.BLACK);
        
        Canvas subcanvas = canvas.createSubcanvas(left + 1, top + 1, width - 1, height -1);
        subcanvas.drawRectangle(0, 0, width - 2, height - 2, Color.ORANGE);
        

        left = 150;
        canvas.drawRectangle(left, top, width, height, Color.BLACK);
        
        subcanvas = canvas.createSubcanvas(left + 1, top + 1, width - 1, height -1);
        subcanvas.offset(-100, -200);
        
        subcanvas.drawRectangle(100, 200, width - 2, height - 2, Color.RED);
        subcanvas.drawRectangle(0, 0, 120, 220, Color.GREEN);
        
        
        left = 10;
        top = 180;
        
  //     int leading = Style.NORMAL.get();
        int ascent = Style.NORMAL.getAscent();
        int line = Style.NORMAL.getLineHeight();
        
        canvas.drawRectangle(left, top, 200 - 1, line - 1, Color.GRAY);
        canvas.drawLine(left, top + ascent, left + 200, top + ascent, Color.GRAY);
        canvas.drawText("12345 abcdefghijklmo ABCDEFGHIJK", left, top + ascent, Color.BLACK, Style.NORMAL);
        /*
        int width = getSize().getWidth();
        int height = getSize().getHeight();
        canvas.drawRectangle(0,0, width - 1, height - 1, Color.GRAY);
        canvas.drawLine(0, 0, width - 1, height - 1, Color.RED);
        canvas.drawLine(width - 1, 0, 0, height - 1, Color.RED);
        */
    }
    
    public void firstClick(Click click) {
        debug("first click " + click);
        super.firstClick(click);
    }

    public void secondClick(Click click) {
        debug("second click " + click);
        super.secondClick(click);
    }
    
    public void mouseMoved(Location location) {
        debug("mouse moved " + location);
        super.mouseMoved(location);
    }
    
    private void debug(String str) {
        getViewManager().getSpy().addAction(str);
    }
    
    public Drag dragStart(DragStart drag) {
        debug("drag start " + drag);
        return super.dragStart(drag);
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