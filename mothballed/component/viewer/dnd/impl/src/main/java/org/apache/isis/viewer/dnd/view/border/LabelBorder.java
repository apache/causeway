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
import org.apache.isis.core.commons.lang.ObjectExtensions;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.action.ParameterContent;
import org.apache.isis.viewer.dnd.view.axis.LabelAxis;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;
import org.apache.isis.viewer.dnd.view.content.FieldContent;

public class LabelBorder extends AbstractBorder {
    public static final int NORMAL = 0;
    public static final int DISABLED = 1;
    public static final int MANDATORY = 2;

    public static View createFieldLabelBorder(final LabelAxis axis, final View wrappedView) {
        final FieldContent fieldContent = (FieldContent) wrappedView.getContent();
        return new LabelBorder(fieldContent, axis, wrappedView);
    }

    public static View createValueParameterLabelBorder(final LabelAxis axis, final View wrappedView) {
        final ParameterContent fieldContent = (ParameterContent) wrappedView.getContent();
        return new LabelBorder(fieldContent, axis, wrappedView);
    }

    private final String label;
    private Text style;
    private Color color;
    private final LabelAxis axis;

    protected LabelBorder(final FieldContent fieldContent, final LabelAxis axis, final View wrappedView) {
        super(wrappedView);
        this.axis = axis;
        if (fieldContent.isEditable().isVetoed()) {
            setDisabledStyling();
        } else if (fieldContent.isMandatory()) {
            setMandatoryStyling();
        } else {
            setOptionalStyling();
        }

        final String name = fieldContent.getFieldName();
        this.label = name + ":";

        final int width = ViewConstants.HPADDING + style.stringWidth(this.label) + ViewConstants.HPADDING;
        if (axis == null) {
            left = width;
        } else {
            axis.accommodateWidth(width);
        }
    }

    protected LabelBorder(final ParameterContent fieldContent, final LabelAxis axis, final View wrappedView) {
        super(wrappedView);
        this.axis = axis;
        if (fieldContent.isRequired()) {
            setMandatoryStyling();
        } else {
            setOptionalStyling();
        }

        final String name = fieldContent.getParameterName();
        this.label = name + ":";

        final int width = ViewConstants.HPADDING + style.stringWidth(this.label) + ViewConstants.HPADDING;
        if (axis == null) {
            left = width;
        } else {
            axis.accommodateWidth(width);
        }
    }

    private void setOptionalStyling() {
        style = Toolkit.getText(ColorsAndFonts.TEXT_LABEL);
        color = Toolkit.getColor(ColorsAndFonts.COLOR_LABEL);
    }

    private void setMandatoryStyling() {
        style = Toolkit.getText(ColorsAndFonts.TEXT_LABEL_MANDATORY);
        color = Toolkit.getColor(ColorsAndFonts.COLOR_LABEL_MANDATORY);
    }

    private void setDisabledStyling() {
        style = Toolkit.getText(ColorsAndFonts.TEXT_LABEL_DISABLED);
        color = Toolkit.getColor(ColorsAndFonts.COLOR_LABEL_DISABLED);
    }

    @Override
    protected int getLeft() {
        if (axis == null) {
            return left;
        } else {
            return axis.getWidth();
        }
    }

    @Override
    public void debugDetails(final DebugBuilder debug) {
        super.debugDetails(debug);
        debug.appendln("label", "'" + label + "'");
        debug.appendln("axis", axis);
    }

    @Override
    public void draw(final Canvas canvas) {
        final Color color = textColor();
        canvas.drawText(label, left, wrappedView.getBaseline(), color, style);
        super.draw(canvas);
    }

    protected Color textColor() {
        return color;
    }

    @Override
    public String toString() {
        return wrappedView.toString() + "/" + ObjectExtensions.classBaseName(this);
    }

    public View getWrapped() {
        return wrappedView;
    }
}
