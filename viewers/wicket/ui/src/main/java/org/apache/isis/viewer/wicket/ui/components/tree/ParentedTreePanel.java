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
package org.apache.isis.viewer.wicket.ui.components.tree;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldTextualAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;

/**
 * Immutable tree, hooks into the ScalarPanelTextField without actually using its text field.
 */
public class ParentedTreePanel
extends ScalarPanelTextFieldTextualAbstract {

    private static final long serialVersionUID = 1L;

    public ParentedTreePanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    @Override
    protected MarkupContainer createScalarIfRegularFormGroup() {

        if(getModel().isEditMode()) {
            // fallback to text editor
            return super.createScalarIfRegularFormGroup();
        }

        final Component treeComponent = createTreeComponent("scalarValueContainer");
        final Behavior treeTheme = getTreeThemeProvider().treeThemeFor(super.getModel());

        getTextField().setLabel(Model.of(getModel().getFriendlyName()));

        final FormGroup formGroup = new FormGroup(ID_SCALAR_IF_REGULAR, getTextField());
        formGroup.add(treeComponent);

        final String labelCaption = getRendering().getLabelCaption(getTextField());
        final Label scalarName = createScalarName(ID_SCALAR_NAME, labelCaption);
        formGroup.add(scalarName);

        // adds the tree-theme behavior to the container, that contains the tree component
        return (MarkupContainer) formGroup.add(treeTheme);
    }

    @Override
    protected Component createComponentForCompact() {
        final Component tree = createTreeComponent(ID_SCALAR_IF_COMPACT);

        // adds the tree-theme behavior to the tree component
        //TODO [2088] not tested yet: if tree renders without applying the theme, behavior needs
        // to go to a container up the hierarchy
        final Behavior treeTheme = getTreeThemeProvider().treeThemeFor(super.getModel());
        return tree.add(treeTheme);
    }

    // -- HELPER

    private Component createTreeComponent(final String id) {
        return IsisToWicketTreeAdapter.adapt(id, getModel());
    }


}
