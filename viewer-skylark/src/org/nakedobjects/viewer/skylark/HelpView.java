package org.nakedobjects.viewer.skylark;

import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.text.TextBlockTarget;
import org.nakedobjects.viewer.skylark.text.TextContent;


public class HelpView extends AbstractView implements View, TextBlockTarget {
    private static final int HEIGHT = 350;
    private static final int WIDTH = 400;
    private TextContent content;

    protected HelpView(View forView) {
        super(null, null, null);

        String description = null;
        String name = null;

        if (forView != null) {
	        Content content = forView.getContent();
	        description = content.getDescription();
            name = content.getName();
	        name =  name == null ? content.title() : name;
        }
        
        String text = (name == null ? "" : (name  + "\n\n")) + (description == null ? "" : description);
        content = new TextContent(this, 10, TextContent.WRAPPING);
        content.setText(text);
    }

    public void draw(Canvas canvas) {
        int x = 0;
        int y = 0;
        int xEntent = getSize().getWidth() - 1;
        int yExtent = getSize().getHeight() - 1;

       
        int arc = 9;
        canvas.drawSolidRectangle(x + 2, y + 2, xEntent - 4, yExtent - 4, Style.WHITE);
        canvas.drawRoundedRectangle(x, y, xEntent, yExtent, arc, arc, Style.BLACK);
        canvas.drawRoundedRectangle(x + 1, y + 1, xEntent - 2, yExtent - 2, arc, arc, Style.SECONDARY2);
        canvas.drawRoundedRectangle(x + 2, y + 2, xEntent - 4, yExtent - 4, arc, arc, Style.BLACK);

        canvas.drawText("Help", x + 10, y + 20, Style.BLACK, Style.TITLE);
     //   canvas.drawText(content.getText(), x + 10, y + 40, Style.BLACK, Style.NORMAL);
        
        y += 65;
        String[] lines = content.getDisplayLines();
        for (int i = 0; i < lines.length; i++) {
            canvas.drawText(lines[i], x + 10, y, Style.BLACK, Style.NORMAL);
            y += 20;
        }
    }

    public Size getRequiredSize() {
        return new Size(WIDTH, HEIGHT);
    }

    /**
     * Removes the help view when clicked on.
     */
    public void firstClick(Click click) {
        getViewManager().clearOverlayView(this);
    }

    
    
    public int getMaxWidth() {
        return WIDTH - 20;
    }
    
    public Text getText() {
        return Style.NORMAL;
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