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

package org.apache.isis.viewer.dnd.view.look.swing;

import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.DrawingUtil;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.border.ScrollBarRender;

public class ScrollBar3DRender implements ScrollBarRender {

    @Override
    public void draw(final Canvas canvas, final boolean isHorizontal, final int x, final int y, final int width, final int height, final int scrollPosition, final int visibleAmount) {
        final Color thumbColor = Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY2);

        // canvas.drawSolidRectangle(x + 1, y + 1, width - 2, height - 2,
        // Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
        canvas.drawSolidRectangle(x + 1, y + 1, width - 2, height, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
        int x2 = x;
        int y2 = y;
        int scrollHeight = height;
        int scrollWidth = width;
        if (isHorizontal) {
            x2 += scrollPosition;
            scrollWidth = visibleAmount;
        } else {
            y2 += scrollPosition;
            scrollHeight = visibleAmount;
        }
        canvas.drawSolidRectangle(x2 + 1, y2, scrollWidth - 2, scrollHeight, thumbColor);
        canvas.drawRectangle(x2 + 1, y2, scrollWidth - 2, scrollHeight, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1));
        DrawingUtil.drawHatching(canvas, x2 + 3, y2 + 4, scrollWidth - 6, scrollHeight - 8, Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY1), Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY3));
        canvas.drawRectangle(x, y, width, height, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2));
    }

}
