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

package org.apache.isis.viewer.dnd.view.debug;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;

public class DebugBorder extends AbstractBorder {
    public DebugBorder(final View wrappedView) {
        super(wrappedView);

        bottom = Toolkit.getText(ColorsAndFonts.TEXT_DEBUG).getTextHeight();
    }

    @Override
    protected void debugDetails(final DebugBuilder debug) {
        debug.append("DebugBorder");
    }

    @Override
    public void draw(final Canvas canvas) {
        final String debug = getView() + " " + getState();
        final Text text = Toolkit.getText(ColorsAndFonts.TEXT_DEBUG);
        final int baseline = wrappedView.getSize().getHeight() + text.getAscent();
        final Color color = Toolkit.getColor(ColorsAndFonts.COLOR_DEBUG_BASELINE);
        canvas.drawText(debug, 0, baseline, color, text);

        super.draw(canvas);
    }

    @Override
    public String toString() {
        return wrappedView.toString() + "/DebugBorder";
    }

    @Override
    public void firstClick(final Click click) {
        new DebugOption().execute(getWorkspace(), getView(), click.getLocation());
    }
}
