package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;

public class PanelBorder extends LineBorder {
	private Color background;

    public PanelBorder(View wrappedView) {
		super(wrappedView);	
		background = Style.WHITE;
	}

    public PanelBorder(int size, Color border, Color background, View wrappedView) {
        super(size, border, wrappedView);
        this.background = background;
    }
    
    public PanelBorder(int size, View wrappedView) {
        super(size, Style.SECONDARY2, wrappedView);
		background = Style.WHITE;
    }
    
    public PanelBorder(Color border, Color background, View wrappedView) {
        super(border, wrappedView);
		this.background = background;
	}

    
	public void draw(Canvas canvas) {
		canvas.clearBackground(this, background);
		super.draw(canvas);
	}
	
	public void setBackground(Color color) {
        this.background = color;
    }
    

    public String toString() {
        return wrappedView.toString() + "/PanelBorder";
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