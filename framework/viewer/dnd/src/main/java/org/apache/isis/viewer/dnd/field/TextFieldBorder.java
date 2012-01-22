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
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;

/**
 * Border decorator to draw a white background and 3D style border around a text
 * field.
 */
public class TextFieldBorder extends AbstractBorder {

    public TextFieldBorder(final View view) {
        super(view);
        top = bottom = left = right = 2;
    }

    @Override
    public void draw(final Canvas canvas) {
        final int height = getSize().getHeight() - 2;
        final int width = getSize().getWidth();
        canvas.drawSolidRectangle(0, 1, width - 1, height - 2, Toolkit.getColor(ColorsAndFonts.COLOR_WHITE));
        canvas.drawRectangle(0, 1, width - 3, height - 2, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1));
        canvas.drawRectangle(1, 2, width - 1, height - 2, Toolkit.getColor(ColorsAndFonts.COLOR_WHITE));

        super.draw(canvas);
    }
}
