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
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.form.InternalFormSpecification;
import org.apache.isis.viewer.dnd.list.SimpleListSpecification;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.border.IconBorder;
import org.apache.isis.viewer.dnd.view.composite.CompositeView;
import org.apache.isis.viewer.dnd.view.content.FieldContent;
import org.apache.isis.viewer.dnd.view.text.TitleText;

public class FieldOfSpecification implements ViewSpecification {

    @Override
    public boolean canDisplay(final ViewRequirement requirement) {
        return requirement.isOpen() && !requirement.isSubview() && requirement.getContent() instanceof FieldContent;
    }

    @Override
    public String getName() {
        return "Field Of";
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

    @Override
    public View createView(final Content content, final Axes axes, final int sequence) {
        final FieldContent fieldContent = (FieldContent) content;
        final ObjectAdapter parent = fieldContent.getParent();
        final Content parentContent = Toolkit.getContentFactory().createRootContent(parent);
        View view = new InternalFieldView(parentContent, fieldContent, axes, this);
        view = addBorder(parentContent, fieldContent, view);
        return view;
    }

    private View addBorder(final Content parentContent, final FieldContent fieldContent, View view) {
        final Text textStyle = Toolkit.getText(ColorsAndFonts.TEXT_TITLE);
        final Color colorStyle = Toolkit.getColor(ColorsAndFonts.COLOR_BLACK);
        final TitleText titleText = new TitleText(view, textStyle, colorStyle) {
            @Override
            protected String title() {
                return parentContent.title() + "/" + fieldContent.getFieldName();
            }
        };
        view = new IconBorder(view, titleText, null, textStyle);
        return view;
    }

}

class InternalFieldView extends CompositeView {
    // final View[] subviews = new View[1];

    private final Content fieldContent;

    public InternalFieldView(final Content content, final Content fieldContent, final Axes axes, final ViewSpecification specification) {
        super(content, specification);
        this.fieldContent = fieldContent;
    }

    /*
     * public void draw(Canvas canvas) { subviews[0].draw(canvas); }
     * 
     * public View[] getSubviews() { return subviews; }
     */
    @Override
    public Size requiredSize(final Size availableSpace) {
        return getSubviews()[0].getRequiredSize(availableSpace);
    }

    @Override
    protected void doLayout(final Size maximumSize) {
        final View view = getSubviews()[0];
        view.setSize(view.getRequiredSize(maximumSize));
        view.layout();
    }

    @Override
    protected void buildView() {
        ViewSpecification internalSpecification;
        if (fieldContent.isCollection()) {
            internalSpecification = new SimpleListSpecification();
        } else {
            internalSpecification = new InternalFormSpecification();
        }
        addView(internalSpecification.createView(fieldContent, new Axes(), 0));
    }
}
