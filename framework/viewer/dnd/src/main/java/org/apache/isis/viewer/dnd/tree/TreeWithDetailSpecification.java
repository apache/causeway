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

package org.apache.isis.viewer.dnd.tree;

import org.apache.isis.viewer.dnd.tree2.CollectionTreeNodeSpecification;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.composite.MasterDetailPanel;

public class TreeWithDetailSpecification implements ViewSpecification {
    private final ViewSpecification treeSpecification;

    public TreeWithDetailSpecification() {
        // treeSpecification = new TreeSpecification();
        treeSpecification = CollectionTreeNodeSpecification.create()[0];
    }

    @Override
    public boolean canDisplay(final ViewRequirement requirement) {
        return treeSpecification.canDisplay(requirement);
    }

    @Override
    public View createView(final Content content, final Axes axes, final int sequence) {
        return new MasterDetailPanel(content, this, treeSpecification);
    }

    @Override
    public String getName() {
        return "Tree and details (experimental)";
    }

    @Override
    public boolean isAligned() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReplaceable() {
        return true;
    }

    @Override
    public boolean isResizeable() {
        return true;
    }

    @Override
    public boolean isSubView() {
        return false;
    }

}
