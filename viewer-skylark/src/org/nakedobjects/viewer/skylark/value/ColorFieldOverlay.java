package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.core.AbstractView;

class ColorFieldOverlay extends AbstractView implements View {
	private static final int colors[] = new int[] {0xffffff, 0x0, 0x666666, 0xcccccc, // white, black, dark gray, light gray
			0x000099, 0x0066cc, 0x0033ff, 0x99ccff, // blues
			0x990000, 0xff0033, 0xcc0066, 0xff66ff, // reds
			0x003300, 0x00ff33, 0x669933, 0xccff66 // greens
	};
	private static final int COLUMNS = 4;
	private static final int ROWS = 4;
	private static final int ROW_HEIGHT = 18;
	private static final int COLUMN_WIDTH = 23;

	private ColorField field;
	
	public ColorFieldOverlay(ColorField field) {
		super(field.getContent(), null, null);
		
		this.field = field;
	}
	
	public Size getRequiredSize() {
		return new Size(COLUMNS * COLUMN_WIDTH, ROWS * ROW_HEIGHT);
	}
	
	public void draw(Canvas canvas) {
		canvas.drawSolidRectangle(0,0, COLUMNS * COLUMN_WIDTH - 1, ROWS * ROW_HEIGHT - 1, Style.SECONDARY3);
		for (int i = 0; i < colors.length; i++) {
			Color color = new Color(colors[i]);
			int y = i / COLUMNS * ROW_HEIGHT;
			int x = i % COLUMNS * COLUMN_WIDTH;
			canvas.drawSolidRectangle(x, y, COLUMN_WIDTH - 1, ROW_HEIGHT - 1, color);
		}
		canvas.drawRectangle(0,0, COLUMNS * COLUMN_WIDTH - 1, ROWS * ROW_HEIGHT - 1, Style.PRIMARY2);
	}
	
	public void firstClick(Click click) {
		int x = click.getMouseLocationRelativeToView().getX();
		int y = click.getMouseLocationRelativeToView().getY();
		int color = colors[y / ROW_HEIGHT * COLUMNS + x / COLUMN_WIDTH];
		field.setColor(color);
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