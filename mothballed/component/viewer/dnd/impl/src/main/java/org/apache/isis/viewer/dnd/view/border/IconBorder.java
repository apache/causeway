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

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.DragStart;
import org.apache.isis.viewer.dnd.view.Placement;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;
import org.apache.isis.viewer.dnd.view.base.IconGraphic;
import org.apache.isis.viewer.dnd.view.composite.CompositeViewDecorator;
import org.apache.isis.viewer.dnd.view.text.ObjectTitleText;
import org.apache.isis.viewer.dnd.view.text.TitleText;

public class IconBorder extends AbstractBorder {

    public static class Factory implements CompositeViewDecorator {
        private final Text textStyle;

        public Factory() {
            this(Toolkit.getText(ColorsAndFonts.TEXT_TITLE));
        }

        public Factory(final Text textStyle) {
            this.textStyle = textStyle;
        }

        @Override
        public View decorate(final View child, final Axes axes) {
            return new IconBorder(child, textStyle);
        }
    }

    private final int baseline;
    private final int titlebarHeight;
    private final IconGraphic icon;
    private final TitleText text;

    public IconBorder(final View wrappedView, final Text style) {
        this(wrappedView, null, null, style);
    }

    public IconBorder(final View wrappedView, final TitleText titleText, final IconGraphic iconGraphic, final Text style) {
        super(wrappedView);

        icon = iconGraphic == null ? new IconGraphic(this, style) : iconGraphic;
        text = titleText == null ? new ObjectTitleText(this, style) : titleText;
        titlebarHeight = ViewConstants.VPADDING + icon.getSize().getHeight() + 1;

        top = titlebarHeight + ViewConstants.VPADDING;
        left = right = ViewConstants.HPADDING;
        bottom = ViewConstants.VPADDING;

        baseline = ViewConstants.VPADDING + icon.getBaseline() + 1;
    }

    @Override
    public void debugDetails(final DebugBuilder debug) {
        super.debugDetails(debug);
        debug.appendln("titlebar", top - titlebarHeight);
    }

    @Override
    public DragEvent dragStart(final DragStart drag) {
        if (overBorder(drag.getLocation())) {
            return Toolkit.getViewFactory().createDragContentOutline(this, drag.getLocation());
        } else {
            return super.dragStart(drag);
        }
    }

    @Override
    public void draw(final Canvas canvas) {
        int x = left - 2;

        if (Toolkit.debug) {
            canvas.drawDebugOutline(new Bounds(getSize()), baseline, Toolkit.getColor(ColorsAndFonts.COLOR_DEBUG_BOUNDS_DRAW));
        }

        // icon & title
        icon.draw(canvas, x, baseline);
        x += icon.getSize().getWidth();
        x += ViewConstants.HPADDING;
        final int maxWidth = getSize().getWidth() - x - right;
        text.draw(canvas, x, baseline, maxWidth);

        // components
        super.draw(canvas);
    }

    @Override
    public int getBaseline() {
        return baseline; // wrappedView.getBaseline() + baseline +
                         // titlebarHeight;
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        final Size size = super.getRequiredSize(availableSpace);
        size.ensureWidth(left + icon.getSize().getWidth() + ViewConstants.HPADDING + text.getSize().getWidth() + right);
        return size;
    }

    @Override
    public void firstClick(final Click click) {
        final int y = click.getLocation().getY();
        if (y < top && click.button2()) {
            final Location location = new Location(click.getLocationWithinViewer());
            getViewManager().showInOverlay(getContent(), location);
        } else {
            super.firstClick(click);
        }
    }

    @Override
    public void secondClick(final Click click) {
        final int y = click.getLocation().getY();
        if (y < top) {
            getWorkspace().addWindowFor(getContent().getAdapter(), new Placement(this));
        } else {
            super.secondClick(click);
        }
    }

    @Override
    public ViewAreaType viewAreaType(final Location mouseLocation) {
        final Bounds title = new Bounds(new Location(), icon.getSize());
        title.extendWidth(left);
        title.extendWidth(text.getSize().getWidth());
        if (title.contains(mouseLocation)) {
            return ViewAreaType.CONTENT;
        } else {
            return super.viewAreaType(mouseLocation);
        }
    }

    @Override
    public String toString() {
        return wrappedView.toString() + "/IconBorder [" + getSpecification() + "]";
    }
}
