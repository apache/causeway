package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.defaults.value.Time;
import org.nakedobjects.object.defaults.value.TimePeriod;
import org.nakedobjects.utility.NotImplementedException;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.ValueContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.core.AbstractFieldSpecification;

import org.apache.log4j.Logger;


public class TimePeriodBarField extends AbstractField {

    public static class Specification extends AbstractFieldSpecification {
        public View createView(Content content, ViewAxis axis) {
            return new TimePeriodBarField(content, this, axis);
        }

        public String getName() {
            return "Period graph";
        }
	    
	    public boolean canDisplay(Naked object) {
	    	return object instanceof TimePeriod;
		}
   }
    private static final Logger LOG = Logger.getLogger(TimePeriodBarField.class);
    private int endTime;
    private int startTime;

    protected TimePeriodBarField(Content content, ViewSpecification specification, ViewAxis axis) {
        super(content, specification, axis);
    }

    public void drag(InternalDrag drag) {
        float x = drag.getRelativeLocation().getX() - 2;
        float max = getSize().getWidth() - 4;

        if ((x >= 0) && (x <= max)) {
            int time = (int) (x / max * 3600 * 24);
            endTime = time;
            initiateSave();
        }
    }
    
    protected void save() {
        Time end = getPeriod().getEnd();
        end.setValue(endTime);

        Time start = getPeriod().getStart();

        TimePeriod tp = new TimePeriod();
        tp.setValue(start, end);
        try {
            parseEntry(tp.title().toString());
        } catch (InvalidEntryException e) {
            throw new NotImplementedException();
        }
        LOG.debug("adjust time " + endTime + " " + getPeriod());
        markDamaged();
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        Color color = getState().isObjectIdentified() ? Style.PRIMARY2 : Style.SECONDARY1;
        Size size = getSize();
        int width = size.getWidth();
        int height = size.getHeight();
        canvas.drawRectangle(0, 0, width - 1, height - 1, color);

        TimePeriod p = getPeriod();

        int max = width - 4;
        int start = (int) ((p.isEmpty() ? 0 : (p.getStart().longValue() * max)) / (3600 * 24)) + 2;
        int end = (int) ((p.isEmpty() ? max : (p.getEnd().longValue() * max)) / (3600 * 24)) + 2;
        canvas.drawSolidRectangle(start, 2, end - start, height - 5, Style.PRIMARY3);
        canvas.drawRectangle(start, 2, end - start, height - 5, color);
        canvas.drawText(p.title().toString(), start + 3, height - 5 - Style.NORMAL.getDescent(),
            color, Style.NORMAL);
    }

     private TimePeriod getPeriod() {
        ValueContent content = ((ValueContent) getContent());
        TimePeriod period = (TimePeriod) content.getValue();

        return period;
    }

    public Size getRequiredSize() {
		Size size = super.getRequiredSize();
		size.extendWidth(304);
        return size; 
    }
/*
    public void refresh() {
    }
    */
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
