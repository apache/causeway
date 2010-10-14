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


package org.apache.isis.extensions.dnd.configurable;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.extensions.dnd.drawing.Size;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.Toolkit;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewRequirement;
import org.apache.isis.extensions.dnd.view.ViewSpecification;
import org.apache.isis.extensions.dnd.view.axis.LabelAxis;
import org.apache.isis.extensions.dnd.view.border.LabelBorder;
import org.apache.isis.extensions.dnd.view.composite.CompositeView;
import org.apache.isis.extensions.dnd.view.composite.StackLayout;
import org.apache.isis.extensions.dnd.view.content.FieldContent;
import org.apache.isis.runtime.userprofile.Options;

public class NewObjectView extends CompositeView {
    StackLayout layout = new StackLayout();
    LabelAxis labelAxis = new LabelAxis();
    List<NewObjectField> fields = new ArrayList<NewObjectField>();

    public NewObjectView(Content content, ViewSpecification specification) {
        super(content, specification);
    }

    void addField(NewObjectField field) {
        fields.add(field);
        addFieldView(field);
        invalidateContent();
    }
    
    protected void buildView() {
        if (getSubviews().length == 0) {
            ObjectAdapter object = getContent().getAdapter();
            ObjectAssociation[] associations = getContent().getSpecification().getAssociations();
           
            
            ObjectAssociation field = associations[0];
            
            addFieldView(object, field);
            addFieldView(object, associations[2]); 
            
            
        }
    }

    private void addFieldView(ObjectAdapter object, ObjectAssociation field) {
        FieldContent fieldContent = (FieldContent) Toolkit.getContentFactory().createFieldContent(field, object);
        ViewRequirement requirement= new ViewRequirement(fieldContent, ViewRequirement.CLOSED | ViewRequirement.SUBVIEW);
        View fieldView = Toolkit.getViewFactory().createView(requirement);
        
        fieldView = LabelBorder.createFieldLabelBorder(labelAxis, fieldView);
        
        addView(fieldView);
    }


    private void addFieldView(NewObjectField field) {
        ObjectAdapter object = getContent().getAdapter();
        FieldContent fieldContent = (FieldContent) Toolkit.getContentFactory().createFieldContent(field.getField(), object);
        ViewRequirement requirement= new ViewRequirement(fieldContent, ViewRequirement.CLOSED | ViewRequirement.SUBVIEW);
        View fieldView = Toolkit.getViewFactory().createView(requirement);
        if (field.includeLabel()) {
            fieldView = LabelBorder.createFieldLabelBorder(labelAxis, fieldView);
        }
        addView(fieldView);
    }

    protected void doLayout(Size maximumSize) {
        layout.layout(this, maximumSize);
    }

    protected Size requiredSize(Size availableSpace) {
        return layout.getRequiredSize(this);
    }

    public void loadOptions(Options viewOptions) {
        Options options = viewOptions.getOptions("fields");
      //  options.options()
       
    }
    
    public void saveOptions(Options viewOptions) {
       for (NewObjectField field : fields) {
           field.saveOptions(viewOptions.getOptions("fields"));
       }
        
    }
}


