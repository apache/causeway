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


package org.apache.isis.viewer.dnd.value;

import org.apache.isis.object.InvalidEntryException;
import org.apache.isis.object.[[NAME]];
import org.apache.isis.extensions.dndviewer.ColorsAndFonts;
import org.apache.isis.utility.NotImplementedException;
import org.apache.isis.viewer.dnd.Canvas;
import org.apache.isis.viewer.dnd.Color;
import org.apache.isis.viewer.dnd.Content;
import org.apache.isis.viewer.dnd.InternalDrag;
import org.apache.isis.viewer.dnd.ObjectContent;
import org.apache.isis.viewer.dnd.Size;
import org.apache.isis.viewer.dnd.Style;
import org.apache.isis.viewer.dnd.View;
import org.apache.isis.viewer.dnd.ViewAxis;
import org.apache.isis.viewer.dnd.ViewSpecification;
import org.apache.isis.viewer.dnd.core.AbstractFieldSpecification;

import java.sql.Time;

import org.apache.log4j.Logger;


public class TimePeriodBarField extends AbstractField {

    public static class Specification extends AbstractFieldSpecification {
        public View createView(Content content, ViewAxis axis) {
            return new TimePeriodBarField(content, this, axis);
        }

        public String getName() {
            return "Period graph";
        }
	    
	    public boolean canDisplay([[NAME]]) {
	    	return object instanceof TimePeriod;
		}
   }
    private static final Logger LOG = Logger.getLogger(TimePeriodBarField.class);
    private int endTime;
  //  private int startTime;

    protected TimePeriodBarField(Content content, ViewSpecification specification, ViewAxis axis) {
        super(content, specification, axis);
    }

    public void drag(InternalDrag drag) {
        float x = drag.getLocation().getX() - 2;
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

        Color color = getState().isObjectIdentified() ? Toolkit.getColor("primary2") : Toolkit.getColor("secondary1");
        Size size = getSize();
        int width = size.getWidth();
        int height = size.getHeight();
        canvas.drawRectangle(0, 0, width - 1, height - 1, color);

        TimePeriod p = getPeriod();

        int max = width - 4;
        int start = (int) ((p.isEmpty() ? 0 : (p.getStart().longValue() * max)) / (3600 * 24)) + 2;
        int end = (int) ((p.isEmpty() ? max : (p.getEnd().longValue() * max)) / (3600 * 24)) + 2;
        canvas.drawSolidRectangle(start, 2, end - start, height - 5, Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY3));
        canvas.drawRectangle(start, 2, end - start, height - 5, color);
        canvas.drawText(p.title().toString(), start + 3, height - 5 - Toolkit.getText(ColorsAndFonts.TEXT_NORMAL).getDescent(),
            color, Toolkit.getText(ColorsAndFonts.TEXT_NORMAL));
    }

     private TimePeriod getPeriod() {
        ObjectContent content = ((ObjectContent) getContent());
        TimePeriod period = (TimePeriod) content.getObject().getObject();

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
[[NAME]] - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  [[NAME]] Group Ltd

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

The authors can be contacted via isis.apache.org (the
registered address of [[NAME]] Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/
