package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.ContentDrag;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.core.AbstractViewDecorator;

public class Identifier extends AbstractViewDecorator {
	private boolean identified;

	public Identifier(View wrappedView) {
		super(wrappedView);
	}

	public void debugDetails(StringBuffer b) {
        b.append("Identifier");
    }

	public void dragIn(ContentDrag drag) {
		wrappedView.dragIn(drag);
	    markDamaged();
	}
	
	public void dragOut(ContentDrag drag) {
		wrappedView.dragOut(drag);
	    markDamaged();
	}
	
	public void draw(Canvas canvas) {
		Size s  = getSize();
/*		if(identified) {
			canvas.drawSolidRectangle(0, 0, s.getWidth(), s.getHeight(), Style.SECONDARY2);
		} else if(dragover) {
				canvas.drawSolidRectangle(0, 0, s.getWidth(), s.getHeight(), Style.PRIMARY3);
		} else {
*/			canvas.drawSolidRectangle(0, 0, s.getWidth(), s.getHeight(), Style.SECONDARY3);
//		}
		wrappedView.draw(canvas);
	}

	public void entered() {
		getState().setObjectIdentified();
		wrappedView.entered();
		identified = true;
		markDamaged();
	}
	
	public void exited() {
		getState().clearObjectIdentified();
		wrappedView.exited();
		identified = false;
		markDamaged();
	}
	
	public String toString() {
		return wrappedView.toString() + "/Identifier [identified=" + identified + "]";
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