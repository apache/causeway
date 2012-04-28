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

package org.apache.isis.viewer.dnd.field;

import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.facets.object.parseable.InvalidEntryException;
import org.apache.isis.core.progmodel.facets.value.color.ColorValueFacet;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.base.AbstractFieldSpecification;
import org.apache.isis.viewer.dnd.view.content.TextParseableContent;
import org.apache.isis.viewer.dnd.view.lookup.OpenDropDownBorder;
import org.apache.isis.viewer.dnd.view.lookup.OptionContent;

public class ColorField extends TextParseableFieldAbstract {
    public static class Specification extends AbstractFieldSpecification {

        @Override
        public boolean canDisplay(final ViewRequirement requirement) {
            return requirement.isTextParseable() && requirement.isForValueType(ColorValueFacet.class);
        }

        @Override
        public View createView(final Content content, final Axes axes, final int sequence) {
            final ColorField field = new ColorField(content, this);
            return new OpenDropDownBorder(field) {
                @Override
                protected View createDropDownView() {
                    return new ColorFieldOverlay(field);
                }

                @Override
                protected void setSelection(final OptionContent selectedContent) {
                }
            };
        }

        @Override
        public String getName() {
            return "Color";
        }
    }

    private int color;

    public ColorField(final Content content, final ViewSpecification specification) {
        super(content, specification);
    }

    @Override
    public void draw(final Canvas canvas) {
        Color color;

        if (hasFocus()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY1);
        } else if (getParent().getState().isObjectIdentified()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_IDENTIFIED);
        } else if (getParent().getState().isRootViewIdentified()) {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY2);
        } else {
            color = Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1);
        }

        int top = 0;
        int left = 0;

        final Size size = getSize();
        int w = size.getWidth() - 1;
        int h = size.getHeight() - 1;
        canvas.drawRectangle(left, top, w, h, color);
        left++;
        top++;
        w -= 1;
        h -= 1;
        canvas.drawSolidRectangle(left, top, w, h, Toolkit.getColor(getColor()));
    }

    /*
     * @Override public void firstClick(final Click click) { if
     * (((TextParseableContent) getContent()).isEditable().isAllowed()) { final
     * View overlay = new DisposeOverlay(new ColorFieldOverlay(this), new
     * ValueDropDownAxis((TextParseableContent) getContent(), getView())); final
     * Location location = this.getAbsoluteLocation(); // Location location =
     * click.getLocationWithinViewer(); // TODO offset by constant amount //
     * location.move(10, 10); overlay.setLocation(location); //
     * overlay.setSize(overlay.getRequiredSize(new Size())); //
     * overlay.markDamaged(); getViewManager().setOverlayView(overlay); } }
     */
    @Override
    public int getBaseline() {
        return ViewConstants.VPADDING + Toolkit.getText(ColorsAndFonts.TEXT_NORMAL).getAscent();
    }

    int getColor() {
        final TextParseableContent content = ((TextParseableContent) getContent());
        final ColorValueFacet col = content.getSpecification().getFacet(ColorValueFacet.class);
        return col.colorValue(content.getAdapter());
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        return new Size(45, 15);
    }

    @Override
    protected void save() {
        try {
            parseEntry("" + color);
        } catch (final InvalidEntryException e) {
            throw new NotYetImplementedException();
        }
    }

    void setColor(final int color) {
        this.color = color;
        initiateSave(false);
    }
}
