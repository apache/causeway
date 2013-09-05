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

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.composite.CompositeViewSpecification;

public abstract class SplitViewSpecification extends CompositeViewSpecification {

    // REVIEW: should provide this rendering context, rather than hardcoding.
    // the net effect currently is that class members annotated with 
    // @Hidden(where=Where.ANYWHERE) or @Disabled(where=Where.ANYWHERE) will indeed
    // be hidden/disabled, but will be visible/enabled (perhaps incorrectly) 
    // for any other value for Where
    final Where where = Where.ANYWHERE;

    public SplitViewSpecification() {
        builder = new SplitViewBuilder(this);
    }

    @Override
    public boolean canDisplay(final ViewRequirement requirement) {
        if (requirement.isObject() && requirement.is(ViewRequirement.OPEN) && !requirement.isSubview()) {
            final Content fieldContent = determineSecondaryContent(requirement.getContent());
            return fieldContent != null && fieldContent.getAdapter() != null;
        } else {
            return false;
        }
    }

    abstract View createMainView(Axes axes, Content mainContent, final Content secondaryContent);

    abstract View createSecondaryView(Axes axes, final Content fieldContent);

    abstract Content determineSecondaryContent(Content content);

    Content field(final ObjectAssociation field, final Content content) {
        final ObjectSpecification spec = content.getSpecification();
        final ObjectAdapter target = content.getAdapter();
        return Toolkit.getContentFactory().createFieldContent(field, target);
    }

    List<ObjectAssociation> determineAvailableFields(final Content content) {
        final ObjectSpecification spec = content.getSpecification();
        final ObjectAdapter target = content.getAdapter();
        final AuthenticationSession session = IsisContext.getAuthenticationSession();
        final List<ObjectAssociation> fields = spec.getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.dynamicallyVisible(session, target, where));
        final List<ObjectAssociation> selectableFields = new ArrayList<ObjectAssociation>();
        for (final ObjectAssociation field : fields) {
            if (validField(field)) {
                selectableFields.add(field);
            }
        }
        return selectableFields;
    }

    abstract boolean validField(ObjectAssociation field);

}
