package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.ContentDrag;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;

public class SimpleBorder extends AbstractBorder {
	private int handleWidth = 14;

	public SimpleBorder(View wrappedView) {
		this(1, wrappedView);
	}
	
	public SimpleBorder(int size, View wrappedView) {
		super(wrappedView);
		
		top = size;
		left = size;
		bottom = size;
		right = size + handleWidth;
	}

	protected void debugDetails(StringBuffer b) {
		b.append("SimpleBorder " + top + " pixels\n");
    	b.append("           handle " + handleWidth + " pixels");
    }
   
	public void dragIn(ContentDrag drag) {
		getState().setCanDrop();
	wrappedView.dragIn(drag);
	    markDamaged();
	}
/*	
	public void dragOut(ContentDrag drag) {
		getState().
		delegate.dragOut(drag);
	    markDamaged();
	}
	*/
/*	public void entered() {
		delegate.entered();
	    markDamaged();
	}
	
	public void exited() {
		delegate.exited();
	    markDamaged();
	}
*/	
	public void entered() {
		getState().setObjectIdentified();
		getState().setViewIdentified();
		wrappedView.entered();
		markDamaged();
	}
	
	public void exited() {
		getState().clearObjectIdentified();
		getState().clearViewIdentified();
		wrappedView.exited();
		markDamaged();
	}
	

	public void draw(Canvas canvas) {
		if(getState().isViewIdentified()) {
			Color color = Style.SECONDARY2;
			Size s  = getSize();
			int width = s.getWidth();
			for (int i = 0; i < left; i++) {
				canvas.drawRectangle(i, i, width - 2 * i - 1, s.getHeight() - 2 * i - 1, color);
			}
			int w2 = width - left - 2; 
			int w3 = w2 - handleWidth;
			for (int x = w2; x > w3; x -= 2) {
				canvas.drawLine(x, top, x, s.getHeight() - top, color);
			}
		}
		super.draw(canvas);
	}
	
	public String toString() {
		return wrappedView.toString() + "/SimpleBorder";
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