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

package org.apache.isis.viewer.dnd.viewer.basic;

import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.base.ObjectView;

public class FallbackView extends ObjectView {

    public static class Specification implements ViewSpecification {
        @Override
        public boolean canDisplay(final ViewRequirement requirement) {
            return true;
        }

        @Override
        public View createView(final Content content, final Axes axes, final int sequence) {
            return new FallbackView(content, this);
        }

        @Override
        public String getName() {
            return "Fallback";
        }

        @Override
        public boolean isAligned() {
            return false;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public boolean isReplaceable() {
            return false;
        }

        @Override
        public boolean isResizeable() {
            return false;
        }

        @Override
        public boolean isSubView() {
            return false;
        }
    }

    protected FallbackView(final Content content, final ViewSpecification specification) {
        super(content, specification);
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);

        final Size size = getSize();
        final int width = size.getWidth() - 1;
        final int height = size.getHeight() - 1;
        canvas.drawSolidRectangle(0, 0, width, height, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
        canvas.drawSolidRectangle(0, 0, 10, height, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2));
        canvas.drawLine(10, 0, 10, height - 2, Toolkit.getColor(ColorsAndFonts.COLOR_BLACK));
        canvas.drawRectangle(0, 0, width, height, Toolkit.getColor(ColorsAndFonts.COLOR_BLACK));
        canvas.drawText(getContent().title(), 14, getBaseline(), Toolkit.getColor(ColorsAndFonts.COLOR_BLACK), Toolkit.getText(ColorsAndFonts.TEXT_NORMAL));
    }

    @Override
    public int getBaseline() {
        return 14;
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        return new Size(150, 20);
    }

    @Override
    public ViewAreaType viewAreaType(final Location mouseLocation) {
        return mouseLocation.getX() <= 10 ? ViewAreaType.VIEW : ViewAreaType.CONTENT;
    }
}
