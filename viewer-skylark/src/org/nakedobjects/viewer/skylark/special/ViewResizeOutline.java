package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.core.AbstractView;

import org.apache.log4j.Logger;



public class ViewResizeOutline extends AbstractView {
    private final int thickness = 1;
	private String label = "";
 
    protected ViewResizeOutline() {
        super(null, null, null);
    }

	public void draw(Canvas canvas) {
        super.draw(canvas);

        Size s = getSize();
    	Logger.getLogger(getClass()).debug("drag outline size " + s);
        for (int i = 0; i < thickness; i++) {
	        canvas.drawRectangle(i, i, s.getWidth() - i * 2 - 1, s.getHeight() - i * 2 - 1, Style.PRIMARY2);
		}
        canvas.drawText(label, 2, 16, Style.PRIMARY2, Style.NORMAL);
    }

	public void setDisplay(String label) {
		this.label = label == null ? "" : label;
	}
	
    public void dispose() {
    	getViewManager().showArrowCursor();
		super.dispose();
	}

}

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
