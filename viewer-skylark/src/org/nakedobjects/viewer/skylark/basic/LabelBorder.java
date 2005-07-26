package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;

public class LabelBorder extends AbstractBorder {
	private final String label;
	private final Text style;
	
	public LabelBorder(String label, View wrappedView) {
		this(label, Style.LABEL, wrappedView);
	}
	
	public LabelBorder(String label, Text style, View wrappedView) {
		super(wrappedView);
		this.label = label + ":";
		this.style = style;
		
        int width = HPADDING + Style.LABEL.stringWidth(this.label) + HPADDING;
		if(getViewAxis() == null) {
		    left = width;
		} else {
		    ((LabelAxis) getViewAxis()).accommodateWidth(width);
		}
	}

	protected int getLeft() {
	    if(getViewAxis() == null) {
		   	return left;
		} else {
		    return ((LabelAxis) getViewAxis()).getWidth();
		}
	}
	
	public void debugDetails(StringBuffer b) {
        b.append("Label '" + label);
    }

	public void draw(Canvas canvas) {
		canvas.drawText(label, HPADDING, wrappedView.getBaseline(), Style.PRIMARY1, style);
		super.draw(canvas);
	}
	
	public String toString() {
		return wrappedView.toString() + "/LabelBorder";
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