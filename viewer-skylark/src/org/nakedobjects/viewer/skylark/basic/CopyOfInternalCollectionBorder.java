package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;
import org.nakedobjects.viewer.skylark.util.ImageFactory;

public class CopyOfInternalCollectionBorder extends AbstractBorder {

	protected CopyOfInternalCollectionBorder(View wrappedView) {
		super(wrappedView);
		left = 24;
	}
	
	protected void debugDetails(StringBuffer b) {
		b.append("InternalCollectionBorder ");
    }

	public Size getRequiredSize() {
		Size size = super.getRequiredSize();
		size.ensureWidth(left + 45 + right);
		size.ensureHeight(24);
		return size;
	}
	
	public void draw(Canvas canvas) {
		Image icon = ImageFactory.getInstance().createIcon("InternalCollection", 18, null);
		canvas.drawIcon(icon, 2, 4);
		
		ObjectContent content = (ObjectContent) getContent();
		NakedCollection collection = (NakedCollection) content.getObject();
		if(collection.size() == 0) {
			canvas.drawText("empty", left, 18, Style.SECONDARY1, Style.NORMAL);
		}
		super.draw(canvas);
	}
	
	public String toString() {
		return "InternalCollectionBorder/" + wrappedView ;
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