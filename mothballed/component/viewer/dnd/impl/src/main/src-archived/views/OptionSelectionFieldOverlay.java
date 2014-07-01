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

import org.apache.isis.extensions.dndviewer.ColorsAndFonts;
import org.apache.isis.viewer.dnd.Canvas;
import org.apache.isis.viewer.dnd.Click;
import org.apache.isis.viewer.dnd.Size;
import org.apache.isis.viewer.dnd.Style;
import org.apache.isis.viewer.dnd.Text;
import org.apache.isis.viewer.dnd.View;
import org.apache.isis.viewer.dnd.core.AbstractView;

public class OptionSelectionFieldOverlay extends AbstractView implements View {
	private String options[];
	private int rowHeight;

	private OptionSelectionField field;
	private static final Text STYLE = Toolkit.getText(ColorsAndFonts.TEXT_NORMAL);
	
	public OptionSelectionFieldOverlay(OptionSelectionField field) {
		super(field.getContent(), null, null);
		this.field = field;
		
		options = field.getOption().getOptions();
		rowHeight = STYLE.getHeight();
	}
	
	public Size getRequiredSize() {
		Size size = new Size();
		for (int i = 0; i < options.length; i++) {
			size.extendHeight(rowHeight);
			size.ensureWidth(STYLE.stringWidth(options[i]));
		}
		size.extendHeight(2 * VPADDING);
		size.extendWidth(2 * HPADDING);
		return size;
	}
	
	public int getBaseline() {
        return STYLE.getAscent();
    }
	
	public void draw(Canvas canvas) {
		Size size = getSize();
		canvas.drawSolidRectangle(0,0, size.getWidth() - 1, size.getHeight() - 1, Toolkit.getColor(ColorsAndFonts.COLOR_WHITE));
		canvas.drawRectangle(0,0, size.getWidth() - 1, size.getHeight() - 1, Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY2));
		int x = HPADDING;
		int y = VPADDING;
		for (int i = 0; i < options.length; i++) {
			canvas.drawText(options[i], x, y + getBaseline(), Toolkit.getColor(ColorsAndFonts.COLOR_BLACK), STYLE);
			y += rowHeight;
		}
	}
	
	public void firstClick(Click click) {
		int y = click.getLocation().getY() - VPADDING;
		int row = y / rowHeight;
		field.set(options[row]);
		dispose();
	}
}

