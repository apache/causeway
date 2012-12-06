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

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.ContentDrag;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.AbstractViewDecorator;

public class SimpleIdentifier extends AbstractViewDecorator {

    public SimpleIdentifier(final View wrappedView) {
        super(wrappedView);
    }

    @Override
    public void debugDetails(final DebugBuilder debug) {
        debug.append("SimpleIdentifier");
    }

    @Override
    public void dragIn(final ContentDrag drag) {
        wrappedView.dragIn(drag);
        markDamaged();
    }

    @Override
    public void dragOut(final ContentDrag drag) {
        wrappedView.dragOut(drag);
        markDamaged();
    }

    @Override
    public void draw(final Canvas canvas) {
        Color color = null;
        if (getState().canDrop()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_VALID);
        } else if (getState().cantDrop()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_INVALID);
        } else if (getState().isViewIdentified() || getState().isObjectIdentified()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY1);
        }

        wrappedView.draw(canvas.createSubcanvas());

        if (color != null) {
            final Size s = getSize();
            canvas.drawRectangle(0, 0, s.getWidth() - 1, s.getHeight() - 1, color);
            canvas.drawRectangle(1, 1, s.getWidth() - 3, s.getHeight() - 3, color);
        }
    }

    @Override
    public void entered() {
        getState().setContentIdentified();
        wrappedView.entered();
        markDamaged();
    }

    @Override
    public void exited() {
        getState().clearObjectIdentified();
        wrappedView.exited();
        markDamaged();
    }

    @Override
    public String toString() {
        return wrappedView.toString() + "/SimpleIdentifier";
    }
}
