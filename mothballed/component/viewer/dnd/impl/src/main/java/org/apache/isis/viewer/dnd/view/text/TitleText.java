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

package org.apache.isis.viewer.dnd.view.text;

import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ResolveException;
import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewState;

/**
 * TitleText draws the text derived from the subclass within a view. The text is
 * properly truncated if longer than the specified maximum width.
 */
public abstract class TitleText {
    private static final int NO_MAX_WIDTH = -1;
    private final Color color;
    private final Text style;
    private final View view;
    private boolean resolveFailure;

    public TitleText(final View view, final Text style, final Color color) {
        this.view = view;
        this.style = style;
        this.color = color;
    }

    /**
     * Draw this TitleText's text stating from the specified x coordination and
     * on the specified baseline.
     */
    public void draw(final Canvas canvas, final int x, final int baseline) {
        draw(canvas, x, baseline, NO_MAX_WIDTH);
    }

    /**
     * Draw this TitleText's text stating from the specified x coordination and
     * on the specified baseline. If a maximum width is specified (ie it is
     * positive) then the text drawn will not extend past that width.
     * 
     * @param maxWidth
     *            the maximum width to display the text within; if negative no
     *            limit is imposed
     */
    public void draw(final Canvas canvas, final int x, final int baseline, final int maxWidth) {
        Color color;
        final ViewState state = view.getState();
        if (resolveFailure) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_ERROR);
        } else if (state.canDrop()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_VALID);
        } else if (state.cantDrop()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_INVALID);
        } else if (state.isObjectIdentified()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_IDENTIFIED);
        } else {
            color = this.color;
        }

        final String text = TextUtils.limitText(getTitle(), style, maxWidth);

        final int xt = x;
        final int yt = baseline;

        if (Toolkit.debug) {
            final int x2 = style.stringWidth(text);
            canvas.drawDebugOutline(new Bounds(xt, yt - style.getAscent(), x2, style.getTextHeight()), baseline, Toolkit.getColor(ColorsAndFonts.COLOR_DEBUG_BOUNDS_DRAW));
        }
        canvas.drawText(text, xt, yt, color, style);
    }

    public Size getSize() {
        final int height = style.getTextHeight();
        final int width = style.stringWidth(getTitle());
        return new Size(width, height);
    }

    private String getTitle() {
        if (resolveFailure) {
            return "Resolve Failure!";
        }

        String title;
        try {
            title = title();
        } catch (final ResolveException e) {
            resolveFailure = true;
            title = "Resolve Failure!";
        }
        return title;
    }

    protected abstract String title();

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("style", style);
        str.append("color", color);
        return str.toString();
    }
}
