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
import org.apache.isis.viewer.dnd.form.InternalFormSpecification;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.border.IconBorder;

public class PanelViewSpecification implements ViewSpecification {
    
    public boolean canDisplay(ViewRequirement requirement) {
        return requirement.isObject() && requirement.isOpen()&& requirement.isExpandable() && requirement.isDesign();
    }

    public String getName() {
        return "Panel (experimental)";
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

    public View createView(Content content, Axes axes, int sequence) {
        PanelView wrappedView = new PanelView(content, this);
        wrappedView.setInitialViewSpecification(new InternalFormSpecification());
        View newView = new IconBorder(wrappedView, Toolkit.getText(ColorsAndFonts.TEXT_TITLE));
        return newView;
    }
}


