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


package org.apache.isis.viewer.dnd.toolbar;

import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.composite.CompositeView;

public class ToolbarView extends CompositeView {

    public ToolbarView(Content content, ViewSpecification specification) {
        super(content, specification);
    }

    protected void buildView() {}

    protected void doLayout(Size maximumSize) {
        int x = HPADDING;
        int y = 0;
        int lineHeight = 0;
        for (View button : getSubviews()) {
            Size buttonSize = button.getRequiredSize(Size.createMax());
            if (x + buttonSize.getWidth() >= maximumSize.getWidth()) {
                x = HPADDING;
                y += lineHeight + VPADDING;
                lineHeight = 0;
            }
            button.setSize(buttonSize);
            button.setLocation(new Location(x, y));
            x += buttonSize.getWidth() + HPADDING;
            lineHeight = Math.max(lineHeight, buttonSize.getHeight());
        }
    }
    
    public Size requiredSize(Size availableSpace) {
        int lineHeight = 0;
        int lineWidth = HPADDING;
        Size requiredSize = new Size();
        for (View button : getSubviews()) {
            Size buttonSize = button.getRequiredSize(availableSpace);
            lineWidth += buttonSize.getWidth() + HPADDING;
            if (lineWidth >= availableSpace.getWidth()) {
                lineWidth = HPADDING;
                requiredSize.extendHeight(lineHeight + VPADDING);
                lineHeight = 0;
            }
            lineHeight = Math.max(lineHeight, buttonSize.getHeight());
            requiredSize.ensureWidth(lineWidth);
        }
        requiredSize.extendHeight(lineHeight + VPADDING);
        return requiredSize;
    }

}


