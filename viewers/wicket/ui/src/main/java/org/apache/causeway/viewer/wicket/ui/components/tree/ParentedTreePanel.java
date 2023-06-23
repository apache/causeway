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
package org.apache.causeway.viewer.wicket.ui.components.tree;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;

import org.apache.causeway.viewer.wicket.model.models.PopModel;
import org.apache.causeway.viewer.wicket.ui.components.pops.PopPanelAbstract2;
import org.apache.causeway.viewer.wicket.ui.components.pops.PopFragmentFactory.FrameFragment;

import lombok.val;

/**
 * Immutable tree, hooks into the PopPanelTextField without actually using its text field.
 */
public class ParentedTreePanel
extends PopPanelAbstract2 {

    private static final long serialVersionUID = 1L;

    public ParentedTreePanel(final String id, final PopModel popModel) {
        super(id, popModel);
    }

    @Override
    protected MarkupContainer createRegularFrame() {
        return FrameFragment.REGULAR
                .createComponent(this::createTreeComponent);
    }

    @Override
    protected MarkupContainer createCompactFrame() {
        return FrameFragment.COMPACT
                .createComponent(this::createTreeComponent);
    }

    @Override
    protected Component getValidationFeedbackReceiver() {
        return null;
    }

    // -- HELPER

    private MarkupContainer createTreeComponent(final String id) {
        val container = getScalarFrameContainer();
        val popModel = popModel();
        val tree = CausewayToWicketTreeAdapter.adapt(id, popModel);
        container.add(tree);
        // adds the tree-theme behavior to the tree's parent
        container.add(getTreeThemeProvider().treeThemeFor(popModel));
        return (MarkupContainer) tree;
    }


}
