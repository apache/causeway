package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;

public class LineBorder extends AbstractBorder {
    private final Color color;
    
	public LineBorder(View wrappedView) {
		this(1, wrappedView);
	}
	
	public LineBorder(int size, View wrappedView) {
	    this(size, Style.PRIMARY2, wrappedView);
	}
	
	public LineBorder(int size, Color color, View wrappedView) {
		super(wrappedView);
		
		top = size;
		left = size;
		bottom = size;
		right = size;
		
		this.color = color;
	}

	protected void debugDetails(StringBuffer b) {
		b.append("LineBorder " + top + " pixels\n");
    }
   
	public void draw(Canvas canvas) {
		
		Size s  = getSize();
		int width = s.getWidth();
		for (int i = 0; i < left; i++) {
			canvas.drawRectangle(i, i, width - 2 * i - 1, s.getHeight() - 2 * i - 1, color);
		}
		super.draw(canvas);
	}
	
	public String toString() {
		return wrappedView.toString() + "/LineBorder";
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