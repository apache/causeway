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

import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.composite.AbstractViewBuilder;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

class SplitViewBuilder extends AbstractViewBuilder {

    private final SplitViewSpecification splitViewSpecification;

    public SplitViewBuilder(final SplitViewSpecification splitViewSpecification) {
        this.splitViewSpecification = splitViewSpecification;
    }

    @Override
    public void createAxes(final Axes axes, final Content content) {
        super.createAxes(axes, content);
        axes.add(new SplitViewAccess(splitViewSpecification.determineAvailableFields(content)));
    }

    @Override
    public void build(final View view, final Axes axes) {
        if (view.getSubviews().length == 0) {
            final Content content = view.getContent();
            final Content fieldContent = splitViewSpecification.determineSecondaryContent(content);

            final View form1 = splitViewSpecification.createMainView(axes, content, fieldContent);
            view.addView(form1);

            final View labelledForm = splitViewSpecification.createSecondaryView(axes, fieldContent);
            view.addView(labelledForm);
        }
    }

    @Override
    public void viewMenuOptions(final UserActionSet options, final View view) {
        super.viewMenuOptions(options, view);

        final SplitViewAccess axis = view.getViewAxes().getAxis(SplitViewAccess.class);
        for (final ObjectAssociation field : axis.getFields()) {
            options.add(new UserActionAbstract("Select " + field.getName()) {
                @Override
                public void execute(final Workspace workspace, final View view, final Location at) {
                }
            });
        }
    }
}
