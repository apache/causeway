package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.core.AbstractView;

public class OptionSelectionFieldOverlay extends AbstractView implements View {
	private String options[];
	private int rowHeight;

	private OptionSelectionField field;
	private static final Text STYLE = Style.NORMAL;
	
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
		canvas.drawSolidRectangle(0,0, size.getWidth() - 1, size.getHeight() - 1, Style.WHITE);
		canvas.drawRectangle(0,0, size.getWidth() - 1, size.getHeight() - 1, Style.PRIMARY2);
		int x = HPADDING;
		int y = VPADDING;
		for (int i = 0; i < options.length; i++) {
			//canvas.drawSolidRectangle(x, y, size.getWidth() - HPADDING, rowHeight, Style.PRIMARY2);
			canvas.drawText(options[i], x, y + getBaseline(), Style.BLACK, STYLE);
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