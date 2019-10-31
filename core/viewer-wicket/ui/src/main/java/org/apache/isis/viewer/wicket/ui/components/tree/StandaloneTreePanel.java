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
import org.apache.wicket.behavior.Behavior;

import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

public class StandaloneTreePanel extends PanelAbstract<ValueModel> {

    private static final long serialVersionUID = 1L;
    private static final String ID_TREE = "tree";

    public StandaloneTreePanel(final String id, final ValueModel valueModel) {
        super(id, valueModel);

        final Component tree = IsisToWicketTreeAdapter.adapt(ID_TREE, valueModel);
        final Behavior treeTheme = super.getTreeThemeProvider().treeThemeFor(valueModel); 

        add(tree.add(treeTheme));
    }

}
