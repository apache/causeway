/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.viewer.dnd.special;

import org.apache.isis.object.ObjectAdapter;
import org.apache.isis.object.reflect.ObjectField;
import org.apache.isis.extensions.dndviewer.ColorsAndFonts;
import org.apache.isis.viewer.dnd.Canvas;
import org.apache.isis.viewer.dnd.Color;
import org.apache.isis.viewer.dnd.Content;
import org.apache.isis.viewer.dnd.InternalDrag;
import org.apache.isis.viewer.dnd.Location;
import org.apache.isis.viewer.dnd.ObjectContent;
import org.apache.isis.viewer.dnd.Size;
import org.apache.isis.viewer.dnd.Style;
import org.apache.isis.viewer.dnd.ViewAreaType;
import org.apache.isis.viewer.dnd.ViewAxis;
import org.apache.isis.viewer.dnd.ViewSpecification;
import org.apache.isis.viewer.dnd.basic.IconGraphic;
import org.apache.isis.viewer.dnd.basic.ObjectTitleText;
import org.apache.isis.viewer.dnd.basic.TitleText;
import org.apache.isis.viewer.dnd.core.ObjectView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ScheduleBlockView extends ObjectView {
	private static final Logger LOG = LoggerFactory.getLogger(ScheduleBlockView.class);
   	private ObjectField timePeriodField;
	private ObjectField colorField;
	private TitleText text;
	private IconGraphic icon;

	public ScheduleBlockView(Content content, ViewSpecification specification, ViewAxis axis, ObjectField timePeriodField, ObjectField colorField) {
        super(content, specification, axis);
        this.timePeriodField = timePeriodField;
        this.colorField = colorField;

        icon = new IconGraphic(this, Toolkit.getText(ColorsAndFonts.TEXT_NORMAL));
        text = new ObjectTitleText(this, Toolkit.getText(ColorsAndFonts.TEXT_NORMAL));
 	}

    public void draw(Canvas canvas) {
        super.draw(canvas);
        
   		Color color;
   		if(colorField == null) {
   			color = Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY3);
   		} else {
	   		ObjectAdapter object = ((ObjectContent) getContent()).getObject();
   			org.apache.isis.application.value.Color fieldColor =  (org.apache.isis.application.value.Color) object.getField(colorField);
   			color = new Color((fieldColor).intValue());
   		}

		
        Size size = getSize();
        int width = size.getWidth() - 1;
        int height = size.getHeight() - 1;
        canvas.drawSolidRectangle(0, 0, width, height, color);
        canvas.drawRectangle(0, 0, width, height, Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY2));
        
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
    /*
    public View dragFrom(Location location) {
		int direction;
		
		if(location.getY() <= 8) {
			direction = ViewResizeOutline.TOP;
		} else if(location.getY() >= getSize().getHeight() - 8) {
			direction = ViewResizeOutline.BOTTOM;
		} else {
			direction = ViewResizeOutline.CENTER;
		}
		
		// TODO this should be done via static method that creates and displays overlay
	    ViewResizeOutline outlineView =new ViewResizeOutline(this, direction);
	    
//	    outlineView.setLocation(getView().getLocationWithinViewer());
//	    outlineView.setSize(getView().getSize());
	    
    	getViewManager().setOverlayView(outlineView);
		LOG.debug("drag view start " + location);
		return outlineView;
	}
    */
    
    public void dragTo(InternalDrag drag) {
    	ObjectAdapter object = ((ObjectContent) getContent()).getObject();
        TimePeriod tp = calculate(drag);
        TimePeriod timePeriod = (TimePeriod) getObject().getField(timePeriodField);
        ((TimePeriod) timePeriod.getValue()).copyObject(tp);
//        ((Appointment) object).getTime().copyObject(tp);
        invalidateLayout();
	}
    
    public void drag(InternalDrag drag) {
       	ViewResizeOutline outlineView = (ViewResizeOutline) drag.getOverlay();
		outlineView.setDisplay(calculate(drag).title().toString());
  	}

	private TimePeriod calculate(InternalDrag drag) {
		// TODO this fails when the layout decorator is itself decorated (e.g. by a WindowBorder!
		ScheduleLayout layout = (ScheduleLayout) getParent().getSpecification();
       	
		Location location = drag.getLocation();
		location.move(0, -getView().getLocation().getY());
		int top = drag.getOverlay().getLocation().getY() - location.getY();
       	int bottom = top + drag.getOverlay().getSize().getHeight();
       	
       	LOG.debug(top + " " + bottom);
       	
       	TimePeriod tp = new TimePeriod();
       	tp.setValue(layout.getTime(getParent(), top), layout.getTime(getParent(), bottom));
       	
       	return tp;
	}


}
