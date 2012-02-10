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

package org.apache.isis.viewer.dnd.field;

import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.base.AbstractView;

class ColorFieldOverlay extends AbstractView {
    private static final int colors[] = new int[] { 0xffffff, 0x0, 0x666666, 0xcccccc, // white,
                                                                                       // black,
                                                                                       // dark
            // gray, light gray
            0x000099, 0x0066cc, 0x0033ff, 0x99ccff, // blues
            0x990000, 0xff0033, 0xcc0066, 0xff66ff, // reds
            0x003300, 0x00ff33, 0x669933, 0xccff66 // greens
    };
    private static final int COLUMNS = 4;
    private static final int ROWS = 4;
    private static final int ROW_HEIGHT = 18;
    private static final int COLUMN_WIDTH = 23;

    private final ColorField field;

    public ColorFieldOverlay(final ColorField field) {
        super(field.getContent());

        this.field = field;
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        return new Size(COLUMNS * COLUMN_WIDTH, ROWS * ROW_HEIGHT);
    }

    @Override
    public void draw(final Canvas canvas) {
        canvas.drawSolidRectangle(0, 0, COLUMNS * COLUMN_WIDTH - 1, ROWS * ROW_HEIGHT - 1, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
        for (int i = 0; i < colors.length; i++) {
            final Color color = Toolkit.getColor(colors[i]);
            final int y = i / COLUMNS * ROW_HEIGHT;
            final int x = i % COLUMNS * COLUMN_WIDTH;
            canvas.drawSolidRectangle(x, y, COLUMN_WIDTH - 1, ROW_HEIGHT - 1, color);
        }
        canvas.drawRectangle(0, 0, COLUMNS * COLUMN_WIDTH - 1, ROWS * ROW_HEIGHT - 1, Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY2));
    }

    @Override
    public void firstClick(final Click click) {
        final int x = click.getLocation().getX();
        final int y = click.getLocation().getY();
        final int color = colors[y / ROW_HEIGHT * COLUMNS + x / COLUMN_WIDTH];
        field.setColor(color);
        getViewManager().clearOverlayView();
    }
}
