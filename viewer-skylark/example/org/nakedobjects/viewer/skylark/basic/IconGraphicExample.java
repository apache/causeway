package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.utility.ToString;
import org.nakedobjects.viewer.skylark.AwtText;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ExampleContent;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.example.TestViews;
import org.nakedobjects.viewer.skylark.metal.ClassIconGraphic;


public class IconGraphicExample extends TestViews {

    public static void main(String[] args) {
        new IconGraphicExample();
    }

    protected void views(Workspace workspace) {
        ExampleContent content = new ExampleContent();
   //     content.setupIconName("icon-a");

        int[] size = new int[] { 12, 20, 40, 60, 85, 100 };
        int x = 160;
        for (int i = 0; i < size.length; i++) {
            View view = new ExampleIconView(content, size[i]);
            view.setLocation(new Location(x, 80));
            x += view.getRequiredSize().getWidth() + 15;
            view.setSize(view.getRequiredSize());
            workspace.addView(view);
        }

        x = 160;
        for (int i = 0; i < size.length; i++) {
            View view = new ExampleClassIconView(content, size[i]);
            view.setLocation(new Location(x, 230));
            x += view.getRequiredSize().getWidth() + 15;
            view.setSize(view.getRequiredSize());
            workspace.addView(view);
        }

        size = new int[] { 10, 12, 14, 16, 18, 20, 24, 36, 60 };
        int y = 80;
        for (int i = 0; i < size.length; i++) {
            View view = new ExampleIconViewWithText(content, new ExampleText("Arial-plain-" + size[i]), true);
            view.setLocation(new Location(10, y));
            y += view.getRequiredSize().getHeight() + 10;
            view.setSize(view.getRequiredSize());
            workspace.addView(view);
        }
        
       	y = 80;
        for (int i = 0; i < size.length; i++) {
            View view = new ExampleIconViewWithText(content, new ExampleText("Arial-plain-" + size[i]), false);
            view.setLocation(new Location(600, y));
            y += view.getRequiredSize().getHeight() + 10;
            view.setSize(view.getRequiredSize());
            workspace.addView(view);
        }
    }
}

class ExampleText extends AwtText {

    protected ExampleText(String font) {
        super("dont-find", font);
    }

}

class ExampleIconView extends DrawingView {
    private IconGraphic icon;

    public ExampleIconView(Content content, int size) {
        super(content);
        icon = new IconGraphic(this, size, 0);
        setRequiredSize(icon.getSize());
    }

    protected void draw(Canvas canvas, int x, int y) {
        int baseline = icon.getBaseline() + 10;
        icon.draw(canvas, x, baseline);
        //     canvas.drawLine(x, baseline, 40, baseline, Color.RED);
    }

    protected void toString(ToString ts) {
        ts.append("icon", icon.getSize());
        ts.append("baseline", icon.getBaseline());
    }
}

class ExampleIconViewWithText extends DrawingView {
    private final IconGraphic icon;
    private final Text text;
    private final String string = "OpqrST";
    private final boolean showBounds;

    public ExampleIconViewWithText(Content content, Text text, boolean showBounds) {
        super(content);
        this.text = text;
        this.showBounds = showBounds;
        icon = new IconGraphic(this, text);
        Size size = icon.getSize();
        size.extendWidth(text.stringWidth(string));
        setRequiredSize(size);
    }

    protected void draw(Canvas canvas, int x, int y) {
        int baseline = icon.getBaseline() + y;
    //    int baseline = - (text.getAscent() - text.getDescent() - icon.getSize().getHeight()) / 2;
        if (showBounds) {
            int right = getRequiredSize().getWidth();

            int centre = y + icon.getSize().getHeight() / 2;
	        canvas.drawLine(x, centre, right, centre, Color.BLACK);
	        
            int ascendTo = baseline - text.getAscent() + text.getDescent();
            int descendTo = baseline + text.getDescent();
            int midline = baseline - (text.getAscent() - text.getDescent()) / 2;
            canvas.drawLine(0, ascendTo, right, ascendTo, Color.RED);
            canvas.drawLine(0, midline, right, midline, Color.WHITE);
            canvas.drawLine(0, baseline, right, baseline, Color.RED);
            canvas.drawLine(0, descendTo, right, descendTo, Color.RED);
        }
        canvas.drawText(string, x + icon.getSize().getWidth(), baseline, Color.BLACK, text);
        icon.draw(canvas, x, baseline);
    }

    protected void toString(ToString ts) {
        ts.append("icon", icon.getSize());
        ts.append("baseline", icon.getBaseline());
    }
}

class ExampleClassIconView extends DrawingView {
    private IconGraphic icon;

    public ExampleClassIconView(Content content, int size) {
        super(content);
        icon = new ClassIconGraphic(this, size, 0);
        setRequiredSize(icon.getSize());
    }

    protected void draw(Canvas canvas, int x, int y) {
        int baseline = icon.getBaseline() + 10;
        icon.draw(canvas, x, baseline);
    }

    protected void toString(ToString ts) {
        ts.append("icon", icon.getSize());
        ts.append("baseline", icon.getBaseline());
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