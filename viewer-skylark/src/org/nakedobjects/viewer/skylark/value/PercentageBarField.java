package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.value.Percentage;
import org.nakedobjects.utility.NotImplementedException;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.ValueField;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.basic.SimpleIdentifier;
import org.nakedobjects.viewer.skylark.core.AbstractFieldSpecification;


public class PercentageBarField extends AbstractField {

    public static class Specification extends AbstractFieldSpecification {
        public View createView(Content content, ViewAxis axis) {
            return new SimpleIdentifier(new PercentageBarField(content, this, axis));
        }

        public String getName() {
            return "Percentage graph";
        }
        
	    public boolean canDisplay(Naked object) {
	    	return object instanceof Percentage;
		}
    }
    
    protected PercentageBarField(Content content, ViewSpecification specification, ViewAxis axis) {
        super(content, specification, axis);
    }

    private Percentage entry = new Percentage();
    
    public void drag(InternalDrag drag) {
        float x = drag.getTargetLocation().getX() - 2;
        setValue(x);
    }

    private void setValue(float x) {
        float max = getSize().getWidth() - 4;
        
        if ((x >= 0) && (x <= max)) {
            entry.setValue(x / max);
            initiateSave();
        }
    }
    
    protected void save() {
        try {
                saveValue(entry);
        } catch(InvalidEntryException e) {
            throw new NotImplementedException();
        }        
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        Color color = getState().isObjectIdentified() ? Style.PRIMARY2 : Style.SECONDARY1;
        Size size = getSize();
        int width = size.getWidth();
        int height = size.getHeight();
        canvas.drawRectangle(0, 0, width - 1, height - 1, color);

        Percentage p = getPercentage();
        int length = (int) ((width - 4) * p.floatValue());
        canvas.drawSolidRectangle(2, 2, length, height - 5, Style.PRIMARY3);
        canvas.drawRectangle(2, 2, length, height - 5, color);
        canvas.drawText(p.title().toString(), 6, height - 5 - Style.NORMAL.getDescent(), color,
            Style.NORMAL);
    }

    public void firstClick(Click click) {
        float x = click.getLocation().getX() - 2;
        setValue(x);        
    } 
    
    private Percentage getPercentage() {
        ValueField content = ((ValueField) getContent());
        Percentage percentage = (Percentage) content.getValue();

        return percentage;
    }

    public Size getRequiredSize() {
		Size size = super.getRequiredSize();
		size.extendWidth(304);
        return size; 

    }

    public void refresh() {
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
