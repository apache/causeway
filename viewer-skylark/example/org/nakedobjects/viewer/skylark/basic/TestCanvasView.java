package org.nakedobjects.viewer.skylark.basic;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Drag;
import org.nakedobjects.viewer.skylark.DragStart;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.core.AbstractView;


class TestCanvasView extends AbstractView {
    public void draw(Canvas canvas) {
        canvas.clearBackground(this, Color.WHITE);

        int canvasWidth = getSize().getWidth();
        int canvasHeight = getSize().getHeight();
        
        canvas.drawRectangle(0, 0, canvasWidth, canvasHeight, Color.BLACK);
        
        
        
        int x = 10;
        int y = 10;
        int width = 50;
        int height = 90;
        // outline shapes
        canvas.drawRectangle(x, y, width, height, Color.GRAY);
        canvas.drawRoundedRectangle(x, y, width, height, 20, 20, Color.BLACK);
        canvas.drawOval(x, y, width, height, Color.GREEN);
        canvas.drawLine(x, y, x + width - 1, y + height - 1, Color.RED);
        canvas.drawLine(x, y + height - 1, x + width - 1, y, Color.RED);
        
        // subcanvas
        x = 80;
        canvas.drawRectangle(x, y, width, height, Color.GRAY);
        
        
        Canvas subcanvas = canvas.createSubcanvas(x + 1, y + 1, width - 1, height -1);
        subcanvas.drawRectangle(0, 0, width - 2, height - 2, Color.ORANGE);
        

        x = 150;
        canvas.drawRectangle(x, y, width, height, Color.GRAY);
        
        subcanvas = canvas.createSubcanvas(x + 1, y + 1, width - 1, height -1);
        subcanvas.offset(-100, -200);
        
        subcanvas.drawRectangle(100, 200, width - 2, height - 2, Color.RED);
        subcanvas.drawRectangle(0, 0, 120, 220, Color.GREEN);
        
        // solid shapes
        x = 10;
        y = 105;
        
        canvas.drawRectangle(x - 1, y - 1, width + 2, height + 2, Color.GRAY);
        canvas.drawSolidRectangle(x, y, width, height, Color.BLACK);
        canvas.drawSolidOval(x, y, width, height, Color.GREEN);
        canvas.drawLine(x, y, x + width - 1, y + height - 1, Color.RED);
        canvas.drawLine(x, y + height - 1, x + width - 1, y, Color.RED);
        
        x = 80;
        canvas.drawSolidRectangle(x, y, width, height, Color.BLACK);
        
        subcanvas = canvas.createSubcanvas(x + 1, y + 1, width - 1, height -1);
        subcanvas.drawSolidRectangle(0, 0, width - 2, height - 2, Color.ORANGE);
        

        x = 150;
        canvas.drawRectangle(x, y, width, width, Color.BLACK);
        canvas.drawOval(x, y, width, width, Color.GREEN);

        // 3D rectangles
        canvas.drawRectangle(x, y + 10 + width, 20, 20, Color.BLACK);
        canvas.draw3DRectangle(x, y + 10 + width, 20, 20, Color.GRAY, true);

        canvas.drawRectangle(x + 30, y + 10 + width, 20, 20, Color.BLACK);
        canvas.draw3DRectangle(x + 30, y + 10 + width, 20, 20, Color.GRAY, true);
        
        x = 10;
        y = 240;
        
  //     int leading = Style.NORMAL.get();
        int ascent = Style.NORMAL.getAscent();
        int descent = Style.NORMAL.getDescent();
        int midpoint = Style.NORMAL.getMidPoint();
        int lineHeight = Style.NORMAL.getLineHeight();
        
        //canvas.drawRectangle(left, top, 200, line, Color.GRAY);
        int baseline = y + ascent;
        drawText(canvas, x, lineHeight, baseline, ascent, descent, midpoint);
        baseline +=  lineHeight;
        drawText(canvas, x, lineHeight, baseline, ascent, descent, midpoint);
        baseline +=  lineHeight;
        drawText(canvas, x, lineHeight, baseline, ascent, descent, midpoint);

        /*
        int width = getSize().getWidth();
        int height = getSize().getHeight();
        canvas.drawRectangle(0,0, width - 1, height - 1, Color.GRAY);
        canvas.drawLine(0, 0, width - 1, height - 1, Color.RED);
        canvas.drawLine(width - 1, 0, 0, height - 1, Color.RED);
        */
    }

    private void drawText(Canvas canvas, int x, int lineHeight, int baseline, int ascent, int descent, int midpoint) {
        canvas.drawLine(x, baseline, x + 200 - 1, baseline, Color.GRAY); // baseline
      //  canvas.drawLine(x, baseline - (ascent - descent) / 2, x + 200 - 1, baseline - (ascent - descent) / 2, Color.RED); // mid-point
        canvas.drawLine(x, baseline - midpoint, x + 200 - 1, baseline - midpoint, Color.RED); // mid-point
        canvas.drawLine(x, baseline - ascent, x + 200 - 1, baseline - ascent, Color.LIGHT_GRAY); // ascent
      //  canvas.drawLine(x, baseline - ascent + descent, x + 200 - 1, baseline - ascent + descent, Color.LIGHT_GRAY); // ascent
        canvas.drawLine(x, baseline + descent, x + 200 - 1, baseline + descent, Color.YELLOW); // descent
        canvas.drawText("12345 abcdefghijk ABCDEFG", x, baseline, Color.BLACK, Style.NORMAL);
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