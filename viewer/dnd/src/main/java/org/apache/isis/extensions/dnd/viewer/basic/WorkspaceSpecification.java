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


package org.apache.isis.extensions.dnd.viewer.basic;

import org.apache.isis.extensions.dnd.service.PerspectiveContent;
import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.CompositeViewSpecification;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewAxis;
import org.apache.isis.extensions.dnd.view.ViewRequirement;
import org.apache.isis.extensions.dnd.view.Workspace;
import org.apache.isis.extensions.dnd.view.base.Layout;
import org.apache.isis.extensions.dnd.view.composite.ViewBuilder;


public class WorkspaceSpecification implements CompositeViewSpecification {
    ApplicationWorkspaceBuilder builder = new ApplicationWorkspaceBuilder();

    public View createView(final Content content, Axes axes, int sequence) {
        Workspace workspace;
        workspace = new ApplicationWorkspace(content, axes, this, createLayout(content, axes), builder);
        // workspace.setFocusManager(new WorkspaceFocusManager());
        return workspace;
    }

    public Layout createLayout(Content content, Axes axes) {
        return  new ApplicationWorkspaceBuilder.ApplicationLayout();
    }
    
    public void createAxes(Content content, Axes axes) {
    }
    
    public ViewAxis axis(Content content) {
        return null;
    }

    public ViewBuilder getSubviewBuilder() {
        return builder;
    }

    public String getName() {
        return "Root Workspace";
    }

    public boolean isAligned() {
        return false;
    }

    public boolean isOpen() {
        return true;
    }

    public boolean isReplaceable() {
        return false;
    }
    
    public boolean isResizeable() {
        return false;
    }

    public boolean isSubView() {
        return false;
    }

    public boolean canDisplay(ViewRequirement requirement) {
        return requirement.isFor(PerspectiveContent.class);
    }

}
