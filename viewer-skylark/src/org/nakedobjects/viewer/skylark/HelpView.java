package org.nakedobjects.viewer.skylark;

import org.nakedobjects.viewer.skylark.core.AbstractView;


public class HelpView extends AbstractView implements View {
    private String description;
    private String name;

    protected HelpView(View forView) {
        super(null, null, null);

        if (forView != null) {
	        Content content = forView.getContent();
	        if(content.getHint() != null) {
	            name = content.getHint().getName();
	            description = content.getHint().getDescription();
	        } else {
	            name = content.getNaked().titleString();
	        }
        }
    }

    public void draw(Canvas canvas) {
        int x = 0;
        int y = 0;
        int width = getSize().getWidth();
        int height = getSize().getHeight();

        canvas.drawSolidRectangle(x, y, width, height, Style.WHITE);
        canvas.drawRoundedRectangle(x, y, width - 1, height - 1, 5, 5, Style.BLACK);
        canvas.drawRoundedRectangle(x + 1, y + 1, width - 3, height - 3, 5, 5, Style.BLACK);

        canvas.drawText("Help", x + 10, y + 20, Style.BLACK, Style.TITLE);
        canvas.drawText(name == null ? "no name" : name, x + 10, y + 40, Style.BLACK, Style.NORMAL);
        canvas.drawText(description == null ? "no description" : description, x + 10, y + 55, Style.BLACK, Style.NORMAL);
    }

    public Size getRequiredSize() {
        return new Size(200, 150);
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */