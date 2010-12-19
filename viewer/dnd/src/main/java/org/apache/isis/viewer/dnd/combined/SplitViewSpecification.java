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

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.composite.CompositeViewSpecification;


public abstract class SplitViewSpecification extends CompositeViewSpecification {

    public SplitViewSpecification() {
        builder = new SplitViewBuilder(this);
    }

    @Override
    public boolean canDisplay(ViewRequirement requirement) {
        if (requirement.isObject() && requirement.is(ViewRequirement.OPEN) && !requirement.isSubview()) {
            Content fieldContent = determineSecondaryContent(requirement.getContent());
            return fieldContent != null && fieldContent.getAdapter() != null;
        } else {
            return false;
        }
    }

    abstract View createMainView(Axes axes, Content mainContent, final Content secondaryContent);
    
    abstract View createSecondaryView(Axes axes, final Content fieldContent);

    abstract Content determineSecondaryContent(Content content);
    
    
    
    
    
    Content field(ObjectAssociation field, Content content) {
        ObjectSpecification spec = content.getSpecification();
        ObjectAdapter target = content.getAdapter();
        return Toolkit.getContentFactory().createFieldContent(field, target);
    }

    List<ObjectAssociation> determineAvailableFields(Content content) {
        ObjectSpecification spec = content.getSpecification();
        ObjectAdapter target = content.getAdapter();
        AuthenticationSession session = IsisContext.getAuthenticationSession();
        List<ObjectAssociation> fields = spec.getAssociations(ObjectAssociationFilters.dynamicallyVisible(session, target));
        List<ObjectAssociation> selectableFields = new ArrayList<ObjectAssociation>();
        for (ObjectAssociation field : fields) {
            if (validField(field)) {
                selectableFields.add(field);
            }
        }
        return selectableFields;
    }

    abstract boolean validField(ObjectAssociation field);


}

