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
package org.apache.isis.viewer.dnd.view.look.linux;

import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.window.ResizeWindowRender;

public class ResizeWindow3DRender implements ResizeWindowRender {

    @Override
    public void draw(final Canvas canvas, final int width, final int height, final boolean isDisabled, final boolean isOver, final boolean isPressed) {
        final int x = 0;
        final int y = 0;

        canvas.drawRectangle(x - 1, y, width + 2, height + 1, Toolkit.getColor(ColorsAndFonts.COLOR_WHITE));
        canvas.drawRectangle(x, y - 1, width, height + 1, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1));
        canvas.drawSolidRectangle(width / 2 - 1, y + 2, 3, height - 4, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2));
        canvas.drawSolidRectangle(x + 2, height / 2 - 1, width - 4, 3, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2));

    }
}
