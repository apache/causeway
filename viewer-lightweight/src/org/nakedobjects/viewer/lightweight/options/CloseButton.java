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

package org.nakedobjects.viewer.lightweight.options;

import org.nakedobjects.viewer.lightweight.Canvas;
import org.nakedobjects.viewer.lightweight.Color;
import org.nakedobjects.viewer.lightweight.Size;
import org.nakedobjects.viewer.lightweight.Style;


public class CloseButton extends WindowButton {
	
	public CloseButton() {
		super(new CloseViewOption());
	}
	
	public void paint(Canvas canvas, Color color) {
		Size size = getBounds().getSize();
		int x = size.getWidth() -1;
		int y = size.getHeight() - 1;
		
		canvas.drawLine(0, 0, x, y, Style.OTHER);
		canvas.drawLine(x, 0, 0, y, Style.OTHER);
	}
}
