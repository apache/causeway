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
package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.reflect.Value;
import org.nakedobjects.object.value.Logical;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.ValueContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractFieldSpecification;


public class CheckboxField extends AbstractField {
	private int size;

	public static class Specification extends AbstractFieldSpecification {
	    public String getName() {
	        return "Checkbox";
	    }

	    public View createView(Content content, ViewAxis axis) {
			return new CheckboxField(content, this, axis);
	    }
	    
	    public boolean canDisplay(Naked object) {
	    	return object instanceof Logical;
		}
	}
	
    public CheckboxField(Content content, ViewSpecification specification, ViewAxis axis) {
        super(content, specification, axis);
        
        size = Style.NORMAL.getHeight();
    }

    public void draw(Canvas canvas) {
        Color color;

        if (hasFocus()) {
            color = Style.PRIMARY2;
        } else if (getParent().getState().isObjectIdentified()) {
            color = Style.IDENTIFIED;
        } else if (getParent().getState().isRootViewIdentified()) {
            color = Style.PRIMARY3;
        } else {
            color = Style.SECONDARY1;
        }

        color = getState().isObjectIdentified() ? Style.PRIMARY2 : Style.SECONDARY1;

    	int top = VPADDING;
    	int left = HPADDING;

    	canvas.drawRectangle(left, top, size - 1, size - 1, color);
/*    	left++;
    	top++;
        canvas.drawRectangle(left, top, size - 3, size - 3, color);
*/
        if (isSet()) {
        	left += 2;
        	top += 2;
	    	int bottom = size - 1;
	    	int right = size - 2;
	    	color = Style.BLACK;
            canvas.drawLine(left, top, right, bottom, color);
            canvas.drawLine(left + 1, top, right + 1, bottom, color);
            canvas.drawLine(right, top, left, bottom, color);
            canvas.drawLine(right + 1, top, left + 1, bottom, color);
        }
    }

    public void firstClick(Click click) {
        if (canChangeValue()) {
            try {
                set(isSet() ? Logical.FALSE : Logical.TRUE);
	            markDamaged();
            } catch (InvalidEntryException e) {
                throw new NakedObjectRuntimeException(e);
            }
        }
    }

    public int getBaseline() {
        return VPADDING + Style.NORMAL.getAscent();
    }

    public Size getRequiredSize() {
        return new Size(HPADDING + size + HPADDING, VPADDING + size + VPADDING);
    }

    private boolean isSet() {
        ValueContent content = ((ValueContent) getContent());
        Logical value = (Logical) content.getValue();

        return value.isSet();
    }

    public void refresh() {
        ValueContent content = ((ValueContent) getContent());
        Value field = content.getValueField();

        if (field.isDerived()) {
            content.getValue().copyObject((Logical) field.get(
                    ((ObjectContent) getParent().getContent()).getObject()));
        }
    }
}
