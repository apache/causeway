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


package org.apache.isis.viewer.dnd.combined;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.viewer.dnd.form.FormSpecification;
import org.apache.isis.viewer.dnd.table.InternalTableSpecification;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.Layout;
import org.apache.isis.viewer.dnd.view.composite.GridLayout;
import org.apache.isis.viewer.dnd.view.composite.StackLayout;


public class FormWithTableSpecification extends SplitViewSpecification {

    @Override
    public Layout createLayout(Content content, Axes axes) {
        return new StackLayout();
    }

    @Override
    View createMainView(Axes axes, Content mainContent, final Content secondaryContent) {
        View form1 = new FormSpecification() {
            @Override
            protected boolean include(Content content, int sequence) {
                return !secondaryContent.getId().equals(content.getId());
            };
            
            @Override
            public Layout createLayout(Content content, Axes axes) {
                GridLayout gridLayout = new GridLayout();
                gridLayout.setSize(2);
                return gridLayout;
            }
        }.createView(mainContent, axes, -1);
        return form1;
    }
    
    @Override
    View createSecondaryView(Axes axes, final Content fieldContent) {
        return new InternalTableSpecification().createView(fieldContent, axes, -1);
    }

    @Override
    Content determineSecondaryContent(Content content) {
        ObjectSpecification spec = content.getSpecification();
        ObjectAdapter target = content.getAdapter();
        AuthenticationSession session = IsisContext.getAuthenticationSession();
        List<ObjectAssociation> fields = spec.getAssociations(ObjectAssociationFilters.dynamicallyVisible(session, target));
        for (ObjectAssociation field : fields) {
            if (field.isOneToManyAssociation()) {
                return Toolkit.getContentFactory().createFieldContent(field, target);
            }
        }
        return null;
    }

    
    
    
    /*
    
    @Override
    protected void init() {
        addSubviewDecorator(new FieldLabelsDecorator() {
            public View decorate(Axes axes, View view) {
                if (view.getContent().isCollection()) {
                    return view;
                } else {
                    return super.decorate(axes, view);
                }
            }
        });
        addViewDecorator(new IconBorder.Factory());
    }

    @Override
    protected SubviewSpec createFieldFactory() {
        return new SubviewSpec() {
            public View createView(final Content content, Axes axes, int sequence) {
                if (content.isCollection()) {
                    return new InternalTableSpecification().createView(content, axes, sequence);
                } else {
                    final ViewFactory factory = Toolkit.getViewFactory();
                    int requirement = ViewRequirement.CLOSED | ViewRequirement.SUBVIEW;
                    ViewRequirement viewRequirement = new ViewRequirement(content, requirement);
                    return factory.createView(viewRequirement);
                }
            }
        };
    }
*/
    @Override
    public String getName() {
        return "Form with table (experimental)";
    }

    @Override
    boolean validField(ObjectAssociation field) {
        return false;
    }
}
