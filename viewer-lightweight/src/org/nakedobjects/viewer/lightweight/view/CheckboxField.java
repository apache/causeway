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

package org.nakedobjects.viewer.lightweight.view;




import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.value.Logical;
import org.nakedobjects.viewer.lightweight.AbstractValueView;
import org.nakedobjects.viewer.lightweight.Canvas;
import org.nakedobjects.viewer.lightweight.Click;
import org.nakedobjects.viewer.lightweight.Color;
import org.nakedobjects.viewer.lightweight.ObjectView;
import org.nakedobjects.viewer.lightweight.Size;
import org.nakedobjects.viewer.lightweight.Style;


public class CheckboxField extends AbstractValueView {
    private Logical value;

	public int getBaseline() {
        return 9;
    }

    public Size getRequiredSize() {
        return new Size(HPADDING + 10 + HPADDING, defaultFieldHeight());
    }

	public NakedValue getValue() {
		return value;
	}
	
    public void firstClick(Click click) {
    	if(canChangeValue()) {
			set(isSet() ? Logical.FALSE : Logical.TRUE);
	        redraw();
    	}
    }

    protected void init(NakedValue value) {
    	this.value = (Logical) value;
	}

	public void draw(Canvas canvas) {
        int x = HPADDING;

        Color color;
        if (hasFocus()) {
			color = Style.ACTIVE;
		} else if(((ObjectView)getParent()).getState().isObjectIdentified()) {
			color = Style.IDENTIFIED;
		} else  if(((ObjectView)getParent()).getState().isRootViewIdentified()) {
			color = Style.IN_FOREGROUND;
		} else {
			color = Style.IN_BACKGROUND;
		}

        canvas.drawRoundedRectangle(x, 0, 9, 9, 3, 3, color);

        if (isSet()) {
            canvas.drawLine(x + 2, 2, x + 7, 7, color);
            canvas.drawLine(x + 7, 2, x + 2, 7,  color);
        }
    }
    
    public void refresh() {
		if(objectField.isDerived()) {
			ObjectView p = (ObjectView) getParent();
			value = (Logical) objectField.get(p.getObject());
		}
	}
	
    private boolean isSet() {
		return value.isSet();
	}
}
