package org.nakedobjects.viewer.skylark.util;

import org.nakedobjects.object.Naked;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractView;

class FallbackView extends AbstractView  {

	protected FallbackView(Content content, ViewSpecification specification, ViewAxis axis) {
		super(content, specification, axis);
	}

	public void draw(Canvas canvas) {
		super.draw(canvas);
		
		Size size = getSize();
		canvas.drawSolidRectangle(0, 0, size.getWidth() - 1, size.getHeight() - 1, Style.SECONDARY3);
		canvas.drawSolidRectangle(0, 0, 10, size.getHeight() - 1, Style.SECONDARY2);
		canvas.drawLine(10, 0, 10, 50, Style.BLACK);
		canvas.drawRectangle(0, 0, size.getWidth() - 1, size.getHeight() - 1, Style.BLACK);
		canvas.drawText("Fallback View", 14, 20, Style.BLACK, Style.NORMAL);
		canvas.drawText(getContent().toString(), 14, 40, Style.BLACK, Style.NORMAL);
	}
	
	public int getBaseline() {
		return 20;
	}
	
	public Size getRequiredSize() {
		return new Size(200, 50);
	}
	
	public ViewAreaType viewAreaType(Location mouseLocation) {
		return mouseLocation.getX() <= 10 ? ViewAreaType.VIEW : ViewAreaType.CONTENT;
	}
	
	public static class Specification implements ViewSpecification {
		public boolean canDisplay(Naked object) {
			return true;
		}
		
		public View createView(Content content, ViewAxis axis) {
			return new FallbackView(content, this, axis);
	    }

		public String getName() {
	        return "Fallback";
	    }

		public boolean isSubView() {
			return false;
		}
		
		public boolean isReplaceable() {
			return false;
		}

		public boolean isOpen() {
			return false;
		}
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