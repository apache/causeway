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

package org.apache.isis.viewer.dnd.view.border;

import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.base.AbstractView;
import org.apache.isis.viewer.dnd.view.content.NullContent;

public class ViewResizeOutline extends AbstractView {
    private final int thickness = 1;
    private String label = "";
    private final Size size;

    protected ViewResizeOutline(final Bounds resizeArea) {
        super(new NullContent());
        size = resizeArea.getSize();
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);

        final Size s = getSize();
        // LoggerFactory.getLogger(getClass()).debug("drag outline size " + s);
        final Color color = Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY2);
        for (int i = 0; i < thickness; i++) {
            canvas.drawRectangle(i, i, s.getWidth() - i * 2 - 1, s.getHeight() - i * 2 - 1, color);
        }
        canvas.drawText(label, 2, 16, color, Toolkit.getText(ColorsAndFonts.TEXT_NORMAL));
    }

    public void setDisplay(final String label) {
        this.label = label == null ? "" : label;
    }

    @Override
    public void dispose() {
        getFeedbackManager().showDefaultCursor();
        super.dispose();
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        return new Size(size);
    }
}
