package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewState;
import org.nakedobjects.viewer.skylark.core.AbstractBorder;
import org.nakedobjects.viewer.skylark.util.ImageFactory;

public class ObjectBorder extends AbstractBorder {
	public ObjectBorder(View wrappedView) {
		this(1, wrappedView);
	}
	
	public ObjectBorder(int size, View wrappedView) {
		super(wrappedView);
		
		top = size;
		left = size;
		bottom = size;
		right = size;
	}

	protected void debugDetails(StringBuffer b) {
		b.append("ObjectBorder " + top + " pixels\n");
    }
	
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
		Color color = null;
		ViewState state = getState();
        if(state.canDrop()) {
		    color = Style.VALID;
		} else if(state.cantDrop()) {
            color = Style.INVALID;
		} else if(state.isObjectIdentified()) {
            color = Style.SECONDARY2;
		}  else if(getViewManager().hasFocus(getView())) {
            color = Style.IDENTIFIED;
		}
		
		NakedObject object = ((ObjectContent) getContent()).getObject();
        if(object != null && object.getOid() == null) {
		    // canvas.drawRectangle(1, 1, width - 3, s.getHeight() - 3, Style.WHITE);
            int x = getSize().getWidth() - 13;
            int y = 0;
            Image icon = ImageFactory.getInstance().createIcon("transient", 8, null);
            if(icon == null) {
                canvas.drawText("*", x, y, Style.BLACK, Style.NORMAL);
            } else {
                canvas.drawIcon(icon, x, y, 12, 12);
            }
		}
        
		Size s  = getSize();
		int width = s.getWidth();
		if(color != null) {
			for (int i = 0; i < left; i++) {
				canvas.drawRectangle(i, i, width - 2 * i - 1, s.getHeight() - 2 * i - 1, color);
			}
		}
		
		super.draw(canvas);
	}
	
	public String toString() {
		return wrappedView.toString() + "/ObjectBorder [" + getSpecification() + "]";
	}
	
	public Size getRequiredSize() {
        Size size = super.getRequiredSize();
        size.extendWidth(13);
        return size; 
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