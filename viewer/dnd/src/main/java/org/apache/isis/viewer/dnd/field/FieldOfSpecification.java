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

    public boolean canDisplay(ViewRequirement requirement) {
        return requirement.isOpen() && !requirement.isSubview() && requirement.getContent() instanceof FieldContent;
    }

    public String getName() {
        return "Field Of";
    }

    public boolean isAligned() {
        return false;
    }

    public boolean isOpen() {
        return false;
    }

    public boolean isReplaceable() {
        return false;
    }

    public boolean isResizeable() {
        return false;
    }

    public boolean isSubView() {
        return false;
    }

    public View createView(Content content, Axes axes, int sequence) {
        final FieldContent fieldContent = (FieldContent) content;
        ObjectAdapter parent = fieldContent.getParent();
        final Content parentContent = Toolkit.getContentFactory().createRootContent(parent);
        View view = new InternalFieldView(parentContent, fieldContent, axes, this);
        view = addBorder(parentContent, fieldContent, view);
        return view;
    }

    private View addBorder(final Content parentContent, final FieldContent fieldContent, View view) {
        Text textStyle = Toolkit.getText(ColorsAndFonts.TEXT_TITLE);
        Color colorStyle = Toolkit.getColor(ColorsAndFonts.COLOR_BLACK);
        TitleText titleText = new TitleText(view, textStyle, colorStyle) {
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
  //  final View[] subviews = new View[1];
    
    private final Content fieldContent;

    public InternalFieldView(Content content, Content fieldContent, Axes axes, ViewSpecification specification) {
        super(content, specification);
        this.fieldContent = fieldContent;
    }
    
  /*  
    public void draw(Canvas canvas) {
        subviews[0].draw(canvas);
    }
    
    public View[] getSubviews() {
        return subviews;
    }
    */
    public Size requiredSize(Size availableSpace) {
        return getSubviews()[0].getRequiredSize(availableSpace);
    }
    
    protected void doLayout(Size maximumSize) {
        View view = getSubviews()[0];
        view.setSize(view.getRequiredSize(maximumSize));
        view.layout();
    }

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


