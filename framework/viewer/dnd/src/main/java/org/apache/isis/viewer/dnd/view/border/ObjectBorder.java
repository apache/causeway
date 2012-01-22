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
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Image;
import org.apache.isis.viewer.dnd.drawing.ImageFactory;
import org.apache.isis.viewer.dnd.drawing.Offset;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.interaction.ViewDragImpl;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.DragStart;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewState;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;
import org.apache.isis.viewer.dnd.view.base.DragViewOutline;

/**
 * A border for objects providing
 * <ol>
 * <li>Ability to drag out a new view of the object.</li>
 * <li>State change when moving over object.
 * <li>Feedback of the state of the view, eg drop valid, identified etc.
 * </ol>
 */
public class ObjectBorder extends AbstractBorder {
    private static final int BORDER = 13;

    public ObjectBorder(final int size, final View wrappedView) {
        super(wrappedView);

        top = size;
        left = size;
        bottom = size;
        right = size + BORDER;
    }

    public ObjectBorder(final View wrappedView) {
        this(1, wrappedView);
    }

    @Override
    protected void debugDetails(final DebugBuilder debug) {
        super.debugDetails(debug);
        debug.appendln("line thickness", left);
    }

    @Override
    public DragEvent dragStart(final DragStart drag) {
        if (drag.getLocation().getX() > getSize().getWidth() - right) {
            if (getContent().getAdapter() == null) {
                return null;
            }
            final View dragOverlay = new DragViewOutline(getView());
            return new ViewDragImpl(this, new Offset(drag.getLocation()), dragOverlay);
        } else {
            return super.dragStart(drag);
        }
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);

        Color color = null;
        final ViewState state = getState();
        final boolean hasFocus = getViewManager().hasFocus(getView());
        if (state.canDrop()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_VALID);
        } else if (state.cantDrop()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_INVALID);
        } else if (hasFocus) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_IDENTIFIED);
        } else if (state.isObjectIdentified()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2);
        }
        final Size s = getSize();

        if (getContent().isPersistable() && getContent().isTransient()) {
            final int x = s.getWidth() - 13;
            final int y = 0;
            final Image icon = ImageFactory.getInstance().loadIcon("transient", 8, null);
            if (icon == null) {
                canvas.drawText("*", x, y + Toolkit.getText(ColorsAndFonts.TEXT_NORMAL).getAscent(), Toolkit.getColor(ColorsAndFonts.COLOR_BLACK), Toolkit.getText(ColorsAndFonts.TEXT_NORMAL));
            } else {
                canvas.drawImage(icon, x, y, 12, 12);
            }
        }

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
                canvas.drawLine(xExtent - BORDER, top, xExtent - BORDER, top + s.getHeight(), color);
                canvas.drawSolidRectangle(xExtent - BORDER + 1, top, BORDER - 2, s.getHeight() - 2 * top, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
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

    /*
     * @Override public void viewMenuOptions(final UserActionSet options) {
     * super.viewMenuOptions(options); Content content = getContent();
     * UserActionSet suboptions = options.addNewActionSet("Replace with");
     * replaceOptions(Toolkit.getViewFactory().availableViews(new
     * ViewRequirement(content, ViewRequirement.OPEN |
     * ViewRequirement.REPLACEABLE | ViewRequirement.SUBVIEW)), suboptions);
     * replaceOptions(Toolkit.getViewFactory().availableViews(new
     * ViewRequirement(content, ViewRequirement.CLOSED |
     * ViewRequirement.REPLACEABLE | ViewRequirement.SUBVIEW)), suboptions); }
     * 
     * protected void replaceOptions(final Enumeration possibleViews, final
     * UserActionSet options) { if (possibleViews.hasMoreElements()) { while
     * (possibleViews.hasMoreElements()) { final ViewSpecification specification
     * = (ViewSpecification) possibleViews.nextElement(); if (specification !=
     * getSpecification()) { options.add(new ReplaceViewOption(specification) {
     * protected void replace(View view, View withReplacement) {
     * replaceWrappedView(withReplacement); } }); } } } }
     */
    @Override
    public String toString() {
        return wrappedView.toString() + "/ObjectBorder [" + getSpecification() + "]";
    }
}
