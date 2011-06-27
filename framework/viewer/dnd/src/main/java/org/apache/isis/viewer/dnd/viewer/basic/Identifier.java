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
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.ContentDrag;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.AbstractViewDecorator;

public class Identifier extends AbstractViewDecorator {
    private boolean identified;

    public Identifier(final View wrappedView) {
        super(wrappedView);
    }

    @Override
    public void debugDetails(final DebugBuilder debug) {
        debug.append("Identifier");
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
        final Size s = getSize();
        canvas.drawSolidRectangle(0, 0, s.getWidth(), s.getHeight(), Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
        wrappedView.draw(canvas);
    }

    @Override
    public void entered() {
        getState().setContentIdentified();
        wrappedView.entered();
        identified = true;
        markDamaged();
    }

    @Override
    public void exited() {
        getState().clearObjectIdentified();
        wrappedView.exited();
        identified = false;
        markDamaged();
    }

    @Override
    public String toString() {
        return wrappedView.toString() + "/Identifier [identified=" + identified + "]";
    }
}
