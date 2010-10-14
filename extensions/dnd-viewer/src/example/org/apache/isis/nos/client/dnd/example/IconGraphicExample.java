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


package org.apache.isis.viewer.dnd.example;

import org.apache.isis.nof.core.util.AsString;
import org.apache.isis.viewer.dnd.Canvas;
import org.apache.isis.viewer.dnd.Content;
import org.apache.isis.viewer.dnd.ExampleContent;
import org.apache.isis.viewer.dnd.View;
import org.apache.isis.viewer.dnd.Workspace;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.example.view.TestViews;
import org.apache.isis.viewer.dnd.view.graphic.IconGraphic;
import org.apache.isis.viewer.dnd.viewer.AwtColor;
import org.apache.isis.viewer.dnd.viewer.AwtText;


public class IconGraphicExample extends TestViews {

    public static void main(final String[] args) {
        new IconGraphicExample();
    }

    protected void views(final Workspace workspace) {
        ExampleContent content = new ExampleContent();
        content.setupIconName("icon-a");

        int[] size = new int[] { 12, 20, 40, 60, 85, 100 };
        int x = 160;
        for (int i = 0; i < size.length; i++) {
            View view = new ExampleIconView(content, size[i]);
            view.setLocation(new Location(x, 80));
            x += view.getRequiredSize(new Size()).getWidth() + 15;
            view.setSize(view.getRequiredSize(new Size()));
            workspace.addView(view);
        }

        x = 160;
        for (int i = 0; i < size.length; i++) {
            View view = new ExampleClassIconView(content, size[i]);
            view.setLocation(new Location(x, 230));
            x += view.getRequiredSize(new Size()).getWidth() + 15;
            view.setSize(view.getRequiredSize(new Size()));
            workspace.addView(view);
        }

        size = new int[] { 10, 12, 14, 16, 18, 20, 24, 36, 60 };
        int y = 80;
        for (int i = 0; i < size.length; i++) {
            View view = new ExampleIconViewWithText(content, new ExampleText("Arial-plain-" + size[i]), true);
            view.setLocation(new Location(10, y));
            y += view.getRequiredSize(new Size()).getHeight() + 10;
            view.setSize(view.getRequiredSize(new Size()));
            workspace.addView(view);
        }

        y = 80;
        for (int i = 0; i < size.length; i++) {
            View view = new ExampleIconViewWithText(content, new ExampleText("Arial-plain-" + size[i]), false);
            view.setLocation(new Location(600, y));
            y += view.getRequiredSize(new Size()).getHeight() + 10;
            view.setSize(view.getRequiredSize(new Size()));
            workspace.addView(view);
        }
    }
}

class ExampleText extends AwtText {

    protected ExampleText(final String font) {
        super("dont-find", font);
    }

}

class ExampleIconView extends DrawingView {
    private IconGraphic icon;

    public ExampleIconView(final Content content, final int size) {
        super(content);
        icon = new IconGraphic(this, size, 0);
        setMaximumSize(icon.getSize());
    }

    protected void draw(final Canvas canvas, final int x, final int y) {
        int baseline = icon.getBaseline() + 10;
        icon.draw(canvas, x, baseline);
        // canvas.drawLine(x, baseline, 40, baseline, Color.RED);
    }

    protected void toString(final AsString ts) {
        ts.append("icon", icon.getSize());
        ts.append("baseline", icon.getBaseline());
    }
}

class ExampleIconViewWithText extends DrawingView {
    private final IconGraphic icon;
    private final Text text;
    private final String string = "OpqrST";
    private final boolean showBounds;

    public ExampleIconViewWithText(final Content content, final Text text, final boolean showBounds) {
        super(content);
        this.text = text;
        this.showBounds = showBounds;
        icon = new IconGraphic(this, text);
        Size size = icon.getSize();
        size.extendWidth(text.stringWidth(string));
        setMaximumSize(size);
    }

    protected void draw(final Canvas canvas, final int x, final int y) {
        int baseline = icon.getBaseline() + y;
        // int baseline = - (text.getAscent() - text.getDescent() - icon.getSize().getHeight()) / 2;
        if (showBounds) {
            int right = getRequiredSize(new Size()).getWidth();

            int centre = y + icon.getSize().getHeight() / 2;
            canvas.drawLine(x, centre, right, centre, AwtColor.BLACK);

            int ascendTo = baseline - text.getAscent() + text.getDescent();
            int descendTo = baseline + text.getDescent();
            int midline = baseline - (text.getAscent() - text.getDescent()) / 2;
            canvas.drawLine(0, ascendTo, right, ascendTo, AwtColor.RED);
            canvas.drawLine(0, midline, right, midline, AwtColor.WHITE);
            canvas.drawLine(0, baseline, right, baseline, AwtColor.RED);
            canvas.drawLine(0, descendTo, right, descendTo, AwtColor.RED);
        }
        canvas.drawText(string, x + icon.getSize().getWidth(), baseline, AwtColor.BLACK, text);
        icon.draw(canvas, x, baseline);
    }

    protected void toString(final AsString ts) {
        ts.append("icon", icon.getSize());
        ts.append("baseline", icon.getBaseline());
    }
}

class ExampleClassIconView extends DrawingView {
    private IconGraphic icon;

    public ExampleClassIconView(final Content content, final int size) {
        super(content);
        icon = new IconGraphic(this, size, 0);
        setMaximumSize(icon.getSize());
    }

    protected void draw(final Canvas canvas, final int x, final int y) {
        int baseline = icon.getBaseline() + 10;
        icon.draw(canvas, x, baseline);
    }

    protected void toString(final AsString ts) {
        ts.append("icon", icon.getSize());
        ts.append("baseline", icon.getBaseline());
    }
}
