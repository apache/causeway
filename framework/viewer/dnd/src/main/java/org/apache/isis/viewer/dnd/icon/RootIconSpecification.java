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

package org.apache.isis.viewer.dnd.icon;

import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Placement;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.base.AbstractViewDecorator;
import org.apache.isis.viewer.dnd.view.border.ObjectBorder;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

class IconOpenAction extends AbstractViewDecorator {
    protected IconOpenAction(final View wrappedView) {
        super(wrappedView);
    }

    @Override
    public void viewMenuOptions(final UserActionSet menuOptions) {
        super.viewMenuOptions(menuOptions);
        menuOptions.add(new UserActionAbstract("Close") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                getView().dispose();
                // getWorkspace().removeObject((ObjectAdapter)
                // view.getContent().getAdapter());
            }
        });
    }

    private void openIcon() {
        getWorkspace().addWindowFor(getContent().getAdapter(), new Placement(getLocation()));
    }

    @Override
    public void secondClick(final Click click) {
        openIcon();
    }
}

public class RootIconSpecification extends IconSpecification {

    @Override
    public boolean canDisplay(final ViewRequirement requirement) {
        return super.canDisplay(requirement) && requirement.is(ViewRequirement.ROOT);
    }

    @Override
    public View createView(final Content content, final Axes axes, final int sequence) {
        final View icon = super.createView(content, axes, sequence);
        return new ObjectBorder(new IconOpenAction(icon));
    }

    @Override
    public boolean isReplaceable() {
        return false;
    }
}
