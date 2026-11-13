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

import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeFragmentFactory.FrameFragment;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributePanel;

/**
 * Renders a non-editable tree.
 */
public class TreeAttributePanel
extends AttributePanel {

    private static final long serialVersionUID = 1L;

    public TreeAttributePanel(final String id, final UiAttributeWkt attributeModel) {
        super(id, attributeModel);
    }

    @Override
    protected MarkupContainer createRegularFrame() {
        return FrameFragment.REGULAR
                .createComponent(this::createTreeComponent);
    }

    @Override
    protected Component createCompactFrame() {
        return FrameFragment.COMPACT
                .createComponent(this::createTreeComponent);
    }

    @Override protected void setupInlinePrompt() { }
    @Override protected void onMakeNotEditable(final String disableReason) { }
    @Override protected void onMakeEditable() { }
    @Override protected Component getValidationFeedbackReceiver() { return null; }

    // -- HELPER

    private MarkupContainer createTreeComponent(final String id) {
        var container = getScalarFrameContainer();
        var attributeModel = attributeModel();
        var tree = DomainObjectTree.createComponent(id, attributeModel);
        container.add(tree);
        // adds the tree-theme behavior to the tree's parent
        container.add(getTreeThemeProvider().treeThemeFor(attributeModel));
        return tree;
    }

}
