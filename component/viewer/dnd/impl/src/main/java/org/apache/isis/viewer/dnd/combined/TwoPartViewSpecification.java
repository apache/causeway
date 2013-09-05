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

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.dnd.form.FormSpecification;
import org.apache.isis.viewer.dnd.form.InternalFormSpecification;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.axis.LabelAxis;
import org.apache.isis.viewer.dnd.view.base.Layout;
import org.apache.isis.viewer.dnd.view.border.LabelBorder;
import org.apache.isis.viewer.dnd.view.composite.ColumnLayout;

public class TwoPartViewSpecification extends SplitViewSpecification {

    @Override
    public Layout createLayout(final Content content, final Axes axes) {
        return new ColumnLayout();
    }

    @Override
    View createMainView(final Axes axes, final Content mainContent, final Content secondaryContent) {
        final View form1 = new FormSpecification() {
            @Override
            protected boolean include(final Content content, final int sequence) {
                return !secondaryContent.getId().equals(content.getId());
            };
        }.createView(mainContent, axes, -1);
        return form1;
    }

    @Override
    View createSecondaryView(final Axes axes, final Content fieldContent) {
        final View form = new InternalFormSpecification().createView(fieldContent, axes, -1);
        final View labelledForm = LabelBorder.createFieldLabelBorder(new LabelAxis(), form);
        return labelledForm;
    }

    @Override
    @Deprecated
    Content determineSecondaryContent(final Content content) {
        final ObjectSpecification spec = content.getSpecification();
        final ObjectAdapter target = content.getAdapter();
        final AuthenticationSession session = IsisContext.getAuthenticationSession();
        final List<ObjectAssociation> fields = spec.getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.dynamicallyVisible(session, target, where));
        for (final ObjectAssociation field : fields) {
            if (validField(field)) {
                return Toolkit.getContentFactory().createFieldContent(field, target);
            }
        }
        return null;
    }

    @Override
    boolean validField(final ObjectAssociation field) {
        return field.isOneToOneAssociation() && !field.getSpecification().isParseable();
    }

    @Override
    public String getName() {
        return "Two part object (experimental)";
    }

}
