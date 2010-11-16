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


package org.apache.isis.viewer.dnd.view.composite;

import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.icon.Icon;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.base.IconGraphic;
import org.apache.isis.viewer.dnd.view.text.ObjectTitleText;

public class ImageViewSpecification implements ViewSpecification {

    public View createView(Content content, Axes axes, int sequence) {
        Icon icon = new Icon(content, ImageViewSpecification.this);
        Text textStyle = Toolkit.getText(ColorsAndFonts.TEXT_NORMAL);
        icon.setTitle(new ObjectTitleText(icon, textStyle));
        icon.setSelectedGraphic(new IconGraphic(icon, 68));
        icon.setUnselectedGraphic(new IconGraphic(icon, 60));
        icon.setVertical(true);
        
        return icon;
    }
 
    public boolean canDisplay(ViewRequirement requirement) {
        return requirement.isObject() && requirement.isClosed();
    }
    
    public String getName() {
        return "Image";
    }

    public boolean isAligned() {
        return false;
    }

    public boolean isOpen() {
        return false;
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
    
}


