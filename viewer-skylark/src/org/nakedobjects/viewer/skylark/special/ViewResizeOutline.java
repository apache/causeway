/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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
package org.nakedobjects.viewer.skylark.special;

import org.apache.log4j.Logger;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.core.AbstractView;



public class ViewResizeOutline extends AbstractView {
    public static final int TOP = 1;
    public static final int BOTTOM = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;
    public static final int TOP_LEFT = 5;
    public static final int TOP_RIGHT = 6;
    public static final int BOTTOM_LEFT = 7;
    public static final int BOTTOM_RIGHT = 8;
    public static final int CENTER = 9;
    
    private final int thickness = 1;
	private final int direction;
	private final Bounds origin;
	private String label = "";
    
    public ViewResizeOutline(InternalDrag drag, View forView, int direction) {
    	super(forView.getContent(), null, null);
    	this.direction = direction;
    	
 /*   	Location offset = drag.getSourceLocation();
    	Location location = drag.getSourceLocationWithinViewer();
    	location.translate(-offset.getX(), -offset.getY());
   */ 	
    	setLocation(forView.getLocationWithinViewer());
    	setSize(forView.getSize());
    	
    	Logger.getLogger(getClass()).debug("drag outline intial size " + getSize() +  "  " + forView.getSize());
    	
    	origin = getBounds();
    	
    	switch (direction) {
			case TOP:
		    	getViewManager().showResizeUpCursor();
				break;
				
			case BOTTOM:
		    	getViewManager().showResizeDownCursor();
				break;
				
			case LEFT:
		    	getViewManager().showResizeLeftCursor();
				break;
				
			case RIGHT:
		    	getViewManager().showResizeRightCursor();
				break;
				
			case TOP_LEFT:
		    	getViewManager().showResizeUpLeftCursor();
				break;
				
			case TOP_RIGHT:
		    	getViewManager().showResizeUpRightCursor();
				break;
				
			case BOTTOM_LEFT:
		    	getViewManager().showResizeDownLeftCursor();
				break;
				
			case BOTTOM_RIGHT:
		    	getViewManager().showResizeDownRightCursor();
				break;
				
			case CENTER:
		    	getViewManager().showMoveCursor();
				break;
				
			default :
				break;
		}
    }
    
    public void adjust(InternalDrag drag) {
    	switch (direction) {
			case ViewResizeOutline.TOP :
				extendUpward(drag);
				break;

			case ViewResizeOutline.BOTTOM :
				extendDownward(drag);
				break;

			case ViewResizeOutline.LEFT :
				extendLeft(drag);
				break;

			case ViewResizeOutline.RIGHT :
				extendRight(drag);
				break;

			case ViewResizeOutline.TOP_RIGHT :
				extendRight(drag);
				extendUpward(drag);
				break;

			case ViewResizeOutline.BOTTOM_RIGHT :
				extendRight(drag);
				extendDownward(drag);
				break;

			case ViewResizeOutline.TOP_LEFT :
				extendLeft(drag);
				extendUpward(drag);
				break;

			case ViewResizeOutline.BOTTOM_LEFT :
				extendLeft(drag);
				extendDownward(drag);
				break;

			case ViewResizeOutline.CENTER :
				moveUpDown(drag);
				break;

			default :
				break;
		}
 	}


    private void extendDownward(InternalDrag drag) {
		markDamaged();
		int height = drag.getTargetLocation().getY();
		int width = getSize().getWidth();
		setSize(new Size(width, height));
		markDamaged();
	}

    private void extendRight(InternalDrag drag) {
		markDamaged();
		int height = getSize().getHeight();
		int width = drag.getTargetLocation().getX();
		setSize(new Size(width, height));
		markDamaged();
	}

	private void extendLeft(InternalDrag drag) {
		markDamaged();
		int difference = drag.getSourceLocation().getX() - drag.getTargetLocation().getX();

		Bounds bounds = new Bounds(origin);
		bounds.extendHeight(difference);
		bounds.translate(-difference, 0);
		setBounds(bounds);

		markDamaged();
	}
	
	private void extendUpward(InternalDrag drag) {
       	Logger.getLogger(getClass()).debug(drag.getSourceLocation() + " " + drag.getTargetLocation());
		
		markDamaged();
		int difference = drag.getSourceLocation().getY() - drag.getTargetLocation().getY();

		Bounds bounds = new Bounds(origin);
		bounds.extendHeight(difference);
		bounds.translate(0, -difference);
		setBounds(bounds);

		markDamaged();
	}
	
	private void moveUpDown(InternalDrag drag) {
		markDamaged();
		int difference = drag.getSourceLocation().getY() - drag.getTargetLocation().getY();
		
		Bounds bounds = new Bounds(origin);
		bounds.translate(0, -difference);
		setBounds(bounds);
		
		markDamaged();
	}


    public void dispose() {
    	getViewManager().showArrowCursor();
		super.dispose();
	}

	public void draw(Canvas canvas) {
        super.draw(canvas);

        Size s = getSize();
        
    	Logger.getLogger(getClass()).debug("drag outline size " + getSize());

        for (int i = 0; i < thickness; i++) {
	        canvas.drawRectangle(i, i, s.getWidth() - i * 2 - 1, s.getHeight() - i * 2 - 1, Style.PRIMARY2);
		}
        canvas.drawText(label, 2, 16, Style.PRIMARY2, Style.NORMAL);
    }
    
	public int getDirection() {
		return direction;
	}

	public void setDisplay(String label) {
		this.label = label == null ? "" : label;
	}
}
