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

import org.apache.isis.object.[[NAME]];
import org.apache.isis.extensions.dndviewer.ColorsAndFonts;
import org.apache.isis.viewer.dnd.Canvas;
import org.apache.isis.viewer.dnd.Click;
import org.apache.isis.viewer.dnd.Color;
import org.apache.isis.viewer.dnd.Content;
import org.apache.isis.viewer.dnd.InternalDrag;
import org.apache.isis.viewer.dnd.Size;
import org.apache.isis.viewer.dnd.Style;
import org.apache.isis.viewer.dnd.ValueContent;
import org.apache.isis.viewer.dnd.View;
import org.apache.isis.viewer.dnd.ViewAxis;
import org.apache.isis.viewer.dnd.ViewSpecification;
import org.apache.isis.viewer.dnd.basic.SimpleIdentifier;
import org.apache.isis.viewer.dnd.core.AbstractFieldSpecification;


public class PercentageBarField extends AbstractField {

    public static class Specification extends AbstractFieldSpecification {
        public View createView(Content content, ViewAxis axis) {
            return new SimpleIdentifier(new PercentageBarField(content, this, axis));
        }

        public String getName() {
            return "Percentage graph";
        }
        
	    public boolean canDisplay([[NAME]]) {
	    	return object.getObject() instanceof Percentage;
		}
    }
    
    protected PercentageBarField(Content content, ViewSpecification specification, ViewAxis axis) {
        super(content, specification, axis);
    }

    private Percentage entry = new Percentage();
    
    public void drag(InternalDrag drag) {
        float x = drag.getLocation().getX() - 2;
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
 /*       try {
            saveValue(entry);
        } catch(InvalidEntryException e) {
            throw new NotImplementedException();
        }        
   */
        }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        Color color = getState().isObjectIdentified() ? Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY2) : Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1);
        Size size = getSize();
        int width = size.getWidth();
        int height = size.getHeight();
        canvas.drawRectangle(0, 0, width - 1, height - 1, color);

        Percentage p = getPercentage();
        int length = (int) ((width - 4) * p.floatValue());
        canvas.drawSolidRectangle(2, 2, length, height - 5, Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY3));
        canvas.drawRectangle(2, 2, length, height - 5, color);
        canvas.drawText(p.title().toString(), 6, height - 5 - Toolkit.getText(ColorsAndFonts.TEXT_NORMAL).getDescent(), color,
            Toolkit.getText(ColorsAndFonts.TEXT_NORMAL));
    }

    public void firstClick(Click click) {
        float x = click.getLocation().getX() - 2;
        setValue(x);        
    } 
    
    private Percentage getPercentage() {
        ValueContent content = ((ValueContent) getContent());
        Percentage percentage = (Percentage) content.getObject().getObject();

        return percentage;
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
