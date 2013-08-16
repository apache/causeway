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

package org.apache.isis.viewer.dnd.configurable;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.userprofile.Options;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.axis.LabelAxis;
import org.apache.isis.viewer.dnd.view.border.LabelBorder;
import org.apache.isis.viewer.dnd.view.composite.CompositeView;
import org.apache.isis.viewer.dnd.view.composite.StackLayout;
import org.apache.isis.viewer.dnd.view.content.FieldContent;

public class NewObjectView extends CompositeView {
    StackLayout layout = new StackLayout();
    LabelAxis labelAxis = new LabelAxis();
    List<NewObjectField> fields = new ArrayList<NewObjectField>();

    public NewObjectView(final Content content, final ViewSpecification specification) {
        super(content, specification);
    }

    void addField(final NewObjectField field) {
        fields.add(field);
        addFieldView(field);
        invalidateContent();
    }

    @Override
    protected void buildView() {
        if (getSubviews().length == 0) {
            final ObjectAdapter object = getContent().getAdapter();
            final List<ObjectAssociation> associations = getContent().getSpecification().getAssociations(Contributed.EXCLUDED);

            final ObjectAssociation field = associations.get(0);

            addFieldView(object, field);
            addFieldView(object, associations.get(2));
        }
    }

    private void addFieldView(final ObjectAdapter object, final ObjectAssociation field) {
        final FieldContent fieldContent = (FieldContent) Toolkit.getContentFactory().createFieldContent(field, object);
        final ViewRequirement requirement = new ViewRequirement(fieldContent, ViewRequirement.CLOSED | ViewRequirement.SUBVIEW);
        View fieldView = Toolkit.getViewFactory().createView(requirement);

        fieldView = LabelBorder.createFieldLabelBorder(labelAxis, fieldView);

        addView(fieldView);
    }

    private void addFieldView(final NewObjectField field) {
        final ObjectAdapter object = getContent().getAdapter();
        final FieldContent fieldContent = (FieldContent) Toolkit.getContentFactory().createFieldContent(field.getField(), object);
        final ViewRequirement requirement = new ViewRequirement(fieldContent, ViewRequirement.CLOSED | ViewRequirement.SUBVIEW);
        View fieldView = Toolkit.getViewFactory().createView(requirement);
        if (field.includeLabel()) {
            fieldView = LabelBorder.createFieldLabelBorder(labelAxis, fieldView);
        }
        addView(fieldView);
    }

    @Override
    protected void doLayout(final Size maximumSize) {
        layout.layout(this, maximumSize);
    }

    @Override
    protected Size requiredSize(final Size availableSpace) {
        return layout.getRequiredSize(this);
    }

    @Override
    public void loadOptions(final Options viewOptions) {
        final Options options = viewOptions.getOptions("fields");
        // options.options()

    }

    @Override
    public void saveOptions(final Options viewOptions) {
        for (final NewObjectField field : fields) {
            field.saveOptions(viewOptions.getOptions("fields"));
        }

    }
}
