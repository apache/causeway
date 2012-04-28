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
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.composite.CompositeView;

public class ToolbarView extends CompositeView {

    public ToolbarView(final Content content, final ViewSpecification specification) {
        super(content, specification);
    }

    @Override
    protected void buildView() {
    }

    @Override
    protected void doLayout(final Size maximumSize) {
        int x = ViewConstants.HPADDING;
        int y = 0;
        int lineHeight = 0;
        for (final View button : getSubviews()) {
            final Size buttonSize = button.getRequiredSize(Size.createMax());
            if (x + buttonSize.getWidth() >= maximumSize.getWidth()) {
                x = ViewConstants.HPADDING;
                y += lineHeight + ViewConstants.VPADDING;
                lineHeight = 0;
            }
            button.setSize(buttonSize);
            button.setLocation(new Location(x, y));
            x += buttonSize.getWidth() + ViewConstants.HPADDING;
            lineHeight = Math.max(lineHeight, buttonSize.getHeight());
        }
    }

    @Override
    public Size requiredSize(final Size availableSpace) {
        int lineHeight = 0;
        int lineWidth = ViewConstants.HPADDING;
        final Size requiredSize = new Size();
        for (final View button : getSubviews()) {
            final Size buttonSize = button.getRequiredSize(availableSpace);
            lineWidth += buttonSize.getWidth() + ViewConstants.HPADDING;
            if (lineWidth >= availableSpace.getWidth()) {
                lineWidth = ViewConstants.HPADDING;
                requiredSize.extendHeight(lineHeight + ViewConstants.VPADDING);
                lineHeight = 0;
            }
            lineHeight = Math.max(lineHeight, buttonSize.getHeight());
            requiredSize.ensureWidth(lineWidth);
        }
        requiredSize.extendHeight(lineHeight + ViewConstants.VPADDING);
        return requiredSize;
    }

}
