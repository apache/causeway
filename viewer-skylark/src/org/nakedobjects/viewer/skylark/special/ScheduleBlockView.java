package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.value.TimePeriod;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.basic.IconGraphic;
import org.nakedobjects.viewer.skylark.basic.ObjectTitleText;
import org.nakedobjects.viewer.skylark.basic.TitleText;
import org.nakedobjects.viewer.skylark.core.ObjectView;

import org.apache.log4j.Logger;


public class ScheduleBlockView extends ObjectView {
	private static final Logger LOG = Logger.getLogger(ScheduleBlockView.class);
   	private Field timePeriodField;
	private Field colorField;
	private TitleText text;
	private IconGraphic icon;

	public ScheduleBlockView(Content content, ViewSpecification specification, ViewAxis axis, Field timePeriodField, Field colorField) {
        super(content, specification, axis);
        this.timePeriodField = timePeriodField;
        this.colorField = colorField;

        icon = new IconGraphic(this, Style.NORMAL);
        text = new ObjectTitleText(this, Style.NORMAL);
 	}

    public void draw(Canvas canvas) {
        super.draw(canvas);
        
   		Color color;
   		if(colorField == null) {
   			color = Style.PRIMARY3;
   		} else {
	   		NakedObject object = ((ObjectContent) getContent()).getObject();
   			org.nakedobjects.object.value.Color fieldColor = (org.nakedobjects.object.value.Color) colorField.get(object);
   			color = new Color((fieldColor).intValue());
   		}

		
        Size size = getSize();
        int width = size.getWidth() - 1;
        int height = size.getHeight() - 1;
        canvas.drawSolidRectangle(0, 0, width, height, color);
        canvas.drawRectangle(0, 0, width, height, Style.PRIMARY2);
//        canvas.drawText(getObject().title().toString(), 2, 16, Style.IN_FOREGROUND, Style.NORMAL);
        
        int x = 0;
        int baseline = icon.getBaseline();
        icon.draw(canvas, x, baseline);
        x += icon.getSize().getWidth();
        text.draw(canvas, x, baseline);

    }
    
    public ViewAreaType viewAreaType(Location mouseLocation) {
		int  objectBoundary = icon.getSize().getWidth();

    	return mouseLocation.getX() > objectBoundary ? ViewAreaType.INTERNAL : ViewAreaType.CONTENT;
	}
    
    public View dragFrom(InternalDrag drag) {
	    Location location = drag.getSourceLocation();
		int direction;
		
		if(location.getY() <= 8) {
			direction = ViewResizeOutline.TOP;
		} else if(location.getY() >= getSize().getHeight() - 8) {
			direction = ViewResizeOutline.BOTTOM;
		} else {
			direction = ViewResizeOutline.CENTER;
		}
		
		// TODO this should be done via static method that creates and displays overlay
	    ViewResizeOutline outlineView =new ViewResizeOutline(drag, this, direction);
	    
//	    outlineView.setLocation(getView().getLocationWithinViewer());
//	    outlineView.setSize(getView().getSize());
	    
    	getViewManager().setOverlayView(outlineView);
		LOG.debug("drag view start " + location);
		getViewManager().setStatus("Changing " + this);
		return outlineView;
	}
    
    public void dragTo(InternalDrag drag) {
    	NakedObject object = ((ObjectContent) getContent()).getObject();
        TimePeriod tp = calculate(drag);
        Naked timePeriod = timePeriodField.get(object);
        timePeriod.copyObject(tp);
//        ((Appointment) object).getTime().copyObject(tp);
        invalidateLayout();
	}
    
    public void drag(InternalDrag drag) {
       	ViewResizeOutline outlineView = (ViewResizeOutline) drag.getDragOverlay();
    	outlineView.adjust(drag);
		outlineView.setDisplay(calculate(drag).title().toString());
  	}

	private TimePeriod calculate(InternalDrag drag) {
		// TODO this fails when the layout decorator is itself decorated (e.g. by a WindowBorder!
		ScheduleLayout layout = (ScheduleLayout) getParent().getSpecification();
       	
		int top = drag.getDragOverlay().getLocation().getY() - getParent().getLocationWithinViewer().getY();
       	int bottom = top + drag.getDragOverlay().getSize().getHeight();
       	
       	LOG.debug(top + " " + bottom);
       	
       	TimePeriod tp = new TimePeriod();
       	tp.setValue(layout.getTime(getParent(), top), layout.getTime(getParent(), bottom));
       	
       	return tp;
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