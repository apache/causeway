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

import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.base.Layout;
import org.apache.isis.viewer.dnd.view.border.IconBorder;
import org.apache.isis.viewer.dnd.view.composite.StackLayout;

public class NewViewSpecification implements ViewSpecification {

    protected Layout createLayout(final Content content, final Axes axes) {
        return new StackLayout();
    }

    @Override
    public boolean canDisplay(final ViewRequirement requirement) {
        return requirement.isObject() && requirement.isOpen();
    }

    @Override
    public String getName() {
        return "Object View";
    }

    @Override
    public boolean isAligned() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isReplaceable() {
        return false;
    }

    @Override
    public boolean isResizeable() {
        return false;
    }

    @Override
    public boolean isSubView() {
        return false;
    }

    @Override
    public View createView(final Content content, final Axes axes, final int sequence) {
        final NewObjectView view = new NewObjectView(content, this);
        View view2 = new IconBorder(view, Toolkit.getText(ColorsAndFonts.TEXT_TITLE));
        view2 = new ViewDesignBorder(view2, view);
        return view2;
    }

}
