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


package org.apache.isis.extensions.dnd.configurable;

import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.extensions.dnd.drawing.ColorsAndFonts;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.drawing.Size;
import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.Toolkit;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewDrag;
import org.apache.isis.extensions.dnd.view.ViewRequirement;
import org.apache.isis.extensions.dnd.view.ViewSpecification;
import org.apache.isis.extensions.dnd.view.border.IconBorder;
import org.apache.isis.extensions.dnd.view.border.LineBorder;
import org.apache.isis.extensions.dnd.view.composite.CompositeView;


public class PanelView extends CompositeView {
    public static enum Position {
        North, South, East, West
    };

    private Panel panel = new Panel();
    private ViewSpecification initialViewSpecification;;
    
    public void debug(DebugString debug) {
        super.debug(debug);
        debug.appendln("Panel");
        debug.indent();
        panel.debug(debug);
        debug.unindent();
    }

    public void setInitialViewSpecification(ViewSpecification initialViewSpecification) {
        this.initialViewSpecification = initialViewSpecification;
    }

    public PanelView(Content content, ViewSpecification specification) {
        super(content, specification);
    }

    protected void buildView() {
//        addView(getContent(), initialViewSpecification, null);
        View newView = initialViewSpecification.createView(getContent(), new Axes(), 0);
        panel.addView(newView, null);
        addView(newView);
    }
    
    

    protected void doLayout(Size maximumSize) {
        panel.layout(maximumSize);
    }

    public Size requiredSize(Size availableSpace) {
        return panel.getRequiredSize(availableSpace);
    }

    @Override
    public void drop(ViewDrag drag) {
        if (drag.getSourceView() == getView() || !contains(drag.getSourceView())) {
            super.drop(drag);
        } else {
            Location dropAt = drag.getLocation();
            dropAt.subtract(getLocation());
            int x = dropAt.getX();
            int y = dropAt.getY();
            int borderWdth = 45;
            int left = getSize().getWidth() - borderWdth;
            int bottom = getSize().getHeight() - borderWdth;
            if (y < borderWdth) {
                addView(drag.getSourceView().getContent(), Position.North);
            } else if (y > bottom) {
                addView(drag.getSourceView().getContent(), Position.South);
            } else if (x < borderWdth) {
                addView(drag.getSourceView().getContent(), Position.West);
            } else if (x > left) {
                addView(drag.getSourceView().getContent(), Position.East);
            }
        }
    }

    public void addView(Content content, Position position) {
        ViewRequirement requirement = new ViewRequirement(content, ViewRequirement.OPEN | ViewRequirement.SUBVIEW);
        ViewSpecification viewSpecification = Toolkit.getViewFactory().availableViews(requirement).nextElement();
        addView(content, viewSpecification, position);
    }

    public void addView(Content content, ViewSpecification specification, Position position) {
        View newView = specification.createView(content, new Axes(), 0);
        //     newView = new LineBorder(newView);
        panel.addView(newView, position);
        addView(newView);
    }
}

