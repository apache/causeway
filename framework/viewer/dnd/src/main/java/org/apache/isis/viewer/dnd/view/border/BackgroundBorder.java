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
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;

/**
 * A background border provides a coloured background to a view of a specified
 * colour.
 */
public class BackgroundBorder extends AbstractBorder {
    private Color background;

    /**
     * Creates a background border with a default colour of white.
     */
    public BackgroundBorder(final View wrappedView) {
        super(wrappedView);
        background = Toolkit.getColor(ColorsAndFonts.COLOR_WHITE);
    }

    public BackgroundBorder(final Color background, final View wrappedView) {
        super(wrappedView);
        this.background = background;
    }

    @Override
    public void draw(final Canvas canvas) {
        clearBackground(canvas, background);
        super.draw(canvas);
    }

    public void setBackground(final Color color) {
        this.background = color;
    }

    @Override
    public String toString() {
        return wrappedView.toString() + "/BackgroundBorder";
    }
}
