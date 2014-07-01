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

import java.util.List;

import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

public class ViewDesignBorder extends AbstractBorder {
    private final NewObjectView viewUnderControl;

    protected ViewDesignBorder(final View view, final NewObjectView view2) {
        super(view);
        viewUnderControl = view2;
    }

    private NewObjectView getNewObjectView() {
        return viewUnderControl;
    }

    @Override
    public void viewMenuOptions(final UserActionSet menuOptions) {
        super.viewMenuOptions(menuOptions);

        // ObjectAdapter object = getContent().getAdapter();
        final List<ObjectAssociation> associations = getContent().getSpecification().getAssociations(Contributed.EXCLUDED);

        for (final ObjectAssociation objectAssociation : associations) {
            final ObjectAssociation f = objectAssociation;
            final UserActionAbstract action = new UserActionAbstract("Add field " + objectAssociation.getName()) {

                @Override
                public void execute(final Workspace workspace, final View view, final Location at) {
                    final NewObjectField field = new NewObjectField(f);
                    getNewObjectView().addField(field);
                }
            };
            menuOptions.add(action);
        }
    }
}
