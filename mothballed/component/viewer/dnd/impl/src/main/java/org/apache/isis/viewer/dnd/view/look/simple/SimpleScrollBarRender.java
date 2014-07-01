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

package org.apache.isis.viewer.dnd.view.look.simple;

import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.border.ScrollBarRender;

public class SimpleScrollBarRender implements ScrollBarRender {

    @Override
    public void draw(final Canvas canvas, final boolean isHorizontal, final int x, final int y, final int width, final int height, final int scrollPosition, final int visibleAmount) {
        // canvas.drawSolidRectangle(x + 1, y + 1, width - 2, height - 2,
        // Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
        if (isHorizontal) {
            // canvas.drawSolidRectangle(x + scrollPosition, y + 2,
            // visibleAmount, height - 4,
            // Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
            // canvas.drawRectangle(x + scrollPosition, y + 1, visibleAmount,
            // height - 2,
            // Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2));

            canvas.drawRectangle(x + scrollPosition + 2, y + 2, visibleAmount - 4, height - 4, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2));
            canvas.drawSolidRectangle(x + scrollPosition + 3, y + 3, visibleAmount - 6, height - 6, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
        } else {
            // canvas.drawSolidRectangle(x + 2, y + scrollPosition, width - 4,
            // visibleAmount,
            // Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
            // canvas.drawRectangle(x + 1, y + scrollPosition, width - 2,
            // visibleAmount,
            // Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2));

            canvas.drawRectangle(x + 2, y + scrollPosition + 2, width - 4, visibleAmount - 4, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2));
            canvas.drawSolidRectangle(x + 3, y + scrollPosition + 3, width - 6, visibleAmount - 6, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
        }
        canvas.drawRectangle(x, y, width, height, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2));
    }
}
