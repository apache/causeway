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

import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;

public class SelectedBorder extends AbstractBorder {
    private final SelectableViewAxis axis;

    public SelectedBorder(final View view, final SelectableViewAxis axis) {
        super(view);
        this.axis = axis;
    }

    @Override
    public void firstClick(final Click click) {
        axis.selected(getView());
        super.firstClick(click);
    }

    @Override
    public void draw(final Canvas canvas) {
        if (axis.isSelected(getView())) {
            final Size size = getSize();
            canvas.drawSolidRectangle(left, right, size.getWidth() - left - right, size.getHeight() - top - bottom, Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY3));
        }
        super.draw(canvas);
    }

}
