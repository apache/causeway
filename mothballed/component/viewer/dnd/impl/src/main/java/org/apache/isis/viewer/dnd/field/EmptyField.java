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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.icon.IconSpecification;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.ContentDrag;
import org.apache.isis.viewer.dnd.view.ObjectContent;
import org.apache.isis.viewer.dnd.view.Placement;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.action.ObjectParameter;
import org.apache.isis.viewer.dnd.view.base.AbstractView;
import org.apache.isis.viewer.dnd.view.base.IconGraphic;
import org.apache.isis.viewer.dnd.view.border.ObjectBorder;
import org.apache.isis.viewer.dnd.view.field.OneToOneField;
import org.apache.isis.viewer.dnd.view.lookup.OpenObjectDropDownBorder;
import org.apache.isis.viewer.dnd.view.text.TitleText;

public class EmptyField extends AbstractView {

    public static class Specification implements ViewSpecification {
        @Override
        public boolean canDisplay(final ViewRequirement requirement) {
            return requirement.isObject() && requirement.isOpen() && !requirement.isTextParseable() && !requirement.hasReference();
        }

        @Override
        public View createView(final Content content, final Axes axes, final int sequence) {
            final EmptyField emptyField = new EmptyField(content, this, Toolkit.getText(ColorsAndFonts.TEXT_NORMAL));
            if ((content instanceof OneToOneField && ((OneToOneField) content).isEditable().isAllowed()) || content instanceof ObjectParameter) {
                if (content.isOptionEnabled()) {
                    return new ObjectBorder(new OpenObjectDropDownBorder(emptyField, new IconSpecification()));
                } else {
                    return new ObjectBorder(emptyField);
                }
            } else {
                return emptyField;
            }
        }

        @Override
        public String getName() {
            return "empty field";
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
            return true;
        }

        @Override
        public boolean isResizeable() {
            return false;
        }

        @Override
        public boolean isSubView() {
            return true;
        }
    }

    private final IconGraphic icon;
    private final TitleText text;

    public EmptyField(final Content content, final ViewSpecification specification, final Text style) {
        super(content, specification);
        if (((ObjectContent) content).getObject() != null) {
            throw new IllegalArgumentException("Content for EmptyField must be null: " + content);
        }
        final ObjectAdapter object = ((ObjectContent) getContent()).getObject();
        if (object != null) {
            throw new IllegalArgumentException("Content for EmptyField must be null: " + object);
        }
        icon = new IconGraphic(this, style);
        text = new EmptyFieldTitleText(this, style);
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);
        int x = 0;
        final int y = icon.getBaseline();
        icon.draw(canvas, x, y);
        x += icon.getSize().getWidth();
        x += ViewConstants.HPADDING;

        text.draw(canvas, x, y);
    }

    @Override
    public int getBaseline() {
        return icon.getBaseline();
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        final Size size = icon.getSize();
        size.extendWidth(ViewConstants.HPADDING);
        size.extendWidth(text.getSize().getWidth());
        return size;
    }

    private Consent canDrop(final ObjectAdapter dragSource) {
        final ObjectContent content = (ObjectContent) getContent();
        return content.canSet(dragSource);
    }

    @Override
    public void dragIn(final ContentDrag drag) {
        final Content sourceContent = drag.getSourceContent();
        if (sourceContent instanceof ObjectContent) {
            final ObjectAdapter source = ((ObjectContent) sourceContent).getObject();
            final Consent canDrop = canDrop(source);
            if (canDrop.isAllowed()) {
                getState().setCanDrop();
            } else {
                getState().setCantDrop();
            }
            final String actionText = canDrop.isVetoed() ? canDrop.getReason() : "Set to " + sourceContent.title();
            getFeedbackManager().setAction(actionText);
        } else {
            getState().setCantDrop();
        }

        markDamaged();
    }

    @Override
    public void dragOut(final ContentDrag drag) {
        getState().clearObjectIdentified();
        markDamaged();
    }

    @Override
    public void drop(final ContentDrag drag) {
        getState().clearViewIdentified();
        markDamaged();
        final ObjectAdapter target = ((ObjectContent) getParent().getContent()).getObject();
        final Content sourceContent = drag.getSourceContent();
        if (sourceContent instanceof ObjectContent) {
            final ObjectAdapter source = ((ObjectContent) sourceContent).getObject();
            setField(target, source);
        }
    }

    /**
     * Objects returned by menus are used to set this field before passing the
     * call on to the parent.
     */
    @Override
    public void objectActionResult(final ObjectAdapter result, final Placement placement) {
        final ObjectAdapter target = ((ObjectContent) getParent().getContent()).getObject();
        if (result instanceof ObjectAdapter) {
            setField(target, result);
        }
        super.objectActionResult(result, placement);
    }

    private void setField(final ObjectAdapter parent, final ObjectAdapter object) {
        if (canDrop(object).isAllowed()) {
            ((ObjectContent) getContent()).setObject(object);
            getParent().invalidateContent();
        }
    }

    @Override
    public String toString() {
        return "EmptyField" + getId();
    }
}
