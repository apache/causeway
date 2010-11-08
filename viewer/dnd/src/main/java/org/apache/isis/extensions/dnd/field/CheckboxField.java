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


package org.apache.isis.extensions.dnd.field;

import org.apache.isis.metamodel.facets.value.BooleanValueFacet;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.extensions.dnd.drawing.Canvas;
import org.apache.isis.extensions.dnd.drawing.Color;
import org.apache.isis.extensions.dnd.drawing.ColorsAndFonts;
import org.apache.isis.extensions.dnd.drawing.Image;
import org.apache.isis.extensions.dnd.drawing.ImageFactory;
import org.apache.isis.extensions.dnd.drawing.Size;
import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.Click;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.KeyboardAction;
import org.apache.isis.extensions.dnd.view.Toolkit;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewRequirement;
import org.apache.isis.extensions.dnd.view.ViewSpecification;
import org.apache.isis.extensions.dnd.view.base.AbstractFieldSpecification;
import org.apache.isis.extensions.dnd.view.content.TextParseableContent;


/*
 * TODO this class does not set the underlying business object  via its boolean adapter.  Need
 * to create an content type for flags.
 */
public class CheckboxField extends AbstractField {
    private static final int size = Toolkit.getText(ColorsAndFonts.TEXT_NORMAL).getTextHeight();

    public static class Specification extends AbstractFieldSpecification {
        @Override
        public boolean canDisplay(ViewRequirement requirement) {
            return requirement.isTextParseable() && requirement.isForValueType(BooleanValueFacet.class);
        }

        public View createView(final Content content, Axes axes, int sequence) {
            return new CheckboxField(content, this);
        }

        public String getName() {
            return "Checkbox";
        }
    }

    public CheckboxField(final Content content, final ViewSpecification specification) {
        super(content, specification);
    }

    @Override
    public void draw(final Canvas canvas) {
        Color color;
        color = getIdentified() ? Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2) : null;
        color = hasFocus() ? Toolkit.getColor(ColorsAndFonts.COLOR_IDENTIFIED) : color;

        final int top = VPADDING;
        final int left = HPADDING;
        if (color != null) {
            canvas.drawRectangle(left - 2, top - 2, size + 4, size + 4, color);
        }

        color = Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1);
        canvas.drawRectangle(left, top, size, size, color);
        if (isSet()) {
            Image image = ImageFactory.getInstance().loadImage("check-mark");
            canvas.drawImage(image, 3, 3, size, size);
        }
    }

    @Override
    public void firstClick(final Click click) {
        toggle();
    }

    public void secondClick(Click click) {
    // ignore
    }

    public void thirdClick(Click click) {
    // ignore
    }

    @Override
    public void keyTyped(KeyboardAction action) {
        if (action.getKeyCode() == ' ') {
            toggle();
        } else {
            super.keyTyped(action);
        }
    }

    private void toggle() {
        if (canChangeValue().isAllowed()) {
            initiateSave(false);
        }
    }

    @Override
    public Consent canChangeValue() {
        final TextParseableContent cont = (TextParseableContent) getContent();
        return cont.isEditable();
    }

    @Override
    public int getBaseline() {
        return VPADDING + Toolkit.getText(ColorsAndFonts.TEXT_NORMAL).getAscent();
    }

    @Override
    public Size getRequiredSize(Size availableSpace) {
        return new Size(HPADDING + size + HPADDING, VPADDING + size + VPADDING);
    }

    private boolean isSet() {
        final BooleanValueFacet booleanValueFacet = getContent().getSpecification().getFacet(BooleanValueFacet.class);
        return booleanValueFacet.isSet(getContent().getAdapter());
    }

    @Override
    protected void save() {
        final BooleanValueFacet booleanValueFacet = getContent().getSpecification().getFacet(BooleanValueFacet.class);
        final ObjectAdapter adapter = getContent().getAdapter();
        if (adapter == null) {
            ((TextParseableContent) getContent()).parseTextEntry("true");
        } else {
            booleanValueFacet.toggle(adapter);
        }

        // return parsed != null ? PersistorUtil.createAdapter(parsed) : null;

        markDamaged();
        ((TextParseableContent) getContent()).entryComplete();
        getParent().invalidateContent();
    }
}
