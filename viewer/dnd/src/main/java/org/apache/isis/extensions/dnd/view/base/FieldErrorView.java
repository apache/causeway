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


package org.apache.isis.extensions.dnd.view.base;

import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.extensions.dnd.drawing.Canvas;
import org.apache.isis.extensions.dnd.drawing.ColorsAndFonts;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.drawing.Size;
import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.Toolkit;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewAreaType;
import org.apache.isis.extensions.dnd.view.ViewRequirement;
import org.apache.isis.extensions.dnd.view.ViewSpecification;


/**
 * Displays an error message in place of a normal field when a problem occurs, usually due to a programming
 * error, and the normal field cannot be created. A example of this is where value field is declared in an
 * ObjectAdapter, but the programmer forgot to instantiate the value object, causing null to be returned
 * instead, which is an illegal value.
 */
public class FieldErrorView extends AbstractView {

    private final String error;

    public FieldErrorView(final String errorMessage) {
        super(null);
        this.error = errorMessage;
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);

        final Size size = getSize();
        canvas.drawSolidRectangle(0, 0, size.getWidth() - 1, size.getHeight() - 1, Toolkit.getColor(ColorsAndFonts.COLOR_WHITE));
        canvas.drawRectangle(0, 0, size.getWidth() - 1, size.getHeight() - 1, Toolkit.getColor(ColorsAndFonts.COLOR_BLACK));
        canvas.drawText(error, 14, 20, Toolkit.getColor(ColorsAndFonts.COLOR_INVALID), Toolkit.getText(ColorsAndFonts.TEXT_NORMAL));
    }

    @Override
    public int getBaseline() {
        return 20;
    }

    @Override
    public Size getRequiredSize(Size availableSpace) {
        return new Size(250, 30);
    }

    @Override
    public ViewAreaType viewAreaType(final Location mouseLocation) {
        return mouseLocation.getX() <= 10 ? ViewAreaType.VIEW : ViewAreaType.CONTENT;
    }

    public static class Specification implements ViewSpecification {
        public boolean canDisplay(ViewRequirement requirement) {
            return true;
        }

        public View createView(final Content content, Axes axes, int sequence) {
            throw new NotYetImplementedException();
        }

        public String getName() {
            return "Field Error";
        }

        public boolean isAligned() {
            return false;
        }

        public boolean isSubView() {
            return false;
        }
        
        public boolean isResizeable() {
            return false;
        }

        public boolean isReplaceable() {
            return false;
        }

        public boolean isOpen() {
            return false;
        }
    }
}
