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

package org.apache.isis.viewer.dnd.service;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewState;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;

public class ServiceBorder extends AbstractBorder {
    private static final int BORDER = 13;

    public ServiceBorder(final int size, final View wrappedView) {
        super(wrappedView);

        top = size;
        left = size;
        bottom = size;
        right = size + BORDER;
    }

    public ServiceBorder(final View wrappedView) {
        this(1, wrappedView);
    }

    @Override
    protected void debugDetails(final DebugBuilder debug) {
        debug.append("ServiceBorder " + top + " pixels");
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);

        Color color = null;
        final ViewState state = getState();
        final boolean hasFocus = getViewManager().hasFocus(getView());
        if (hasFocus) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_IDENTIFIED);
        } else if (state.isObjectIdentified()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2);
        }

        final Size s = getSize();
        if (color != null) {
            if (hasFocus) {
                final int xExtent = s.getWidth() - left;
                for (int i = 0; i < left; i++) {
                    canvas.drawRectangle(i, i, xExtent - 2 * i, s.getHeight() - 2 * i, color);
                }
            } else {
                final int xExtent = s.getWidth();
                for (int i = 0; i < left; i++) {
                    canvas.drawRectangle(i, i, xExtent - 2 * i, s.getHeight() - 2 * i, color);
                }
                canvas.drawLine(xExtent - BORDER, left, xExtent - BORDER, left + s.getHeight(), color);
                canvas.drawSolidRectangle(xExtent - BORDER + 1, left, BORDER - 2, s.getHeight() - 2 * left, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
            }
        }
    }

    @Override
    public void entered() {
        getState().setContentIdentified();
        getState().setViewIdentified();
        wrappedView.entered();
        markDamaged();
    }

    @Override
    public void exited() {
        getState().clearObjectIdentified();
        getState().clearViewIdentified();
        wrappedView.exited();
        markDamaged();
    }

    @Override
    public void secondClick(final Click click) {
        // ignore - prevents the super class opening a view
    }

    @Override
    public String toString() {
        return wrappedView.toString() + "/ServiceBorder [" + getSpecification() + "]";
    }
}
