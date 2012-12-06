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

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewDrag;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.composite.CompositeView;

public class PanelView extends CompositeView {
    public static enum Position {
        North, South, East, West
    };

    private final Panel panel = new Panel();
    private ViewSpecification initialViewSpecification;;

    @Override
    public void debug(final DebugBuilder debug) {
        super.debug(debug);
        debug.appendln("Panel");
        debug.indent();
        panel.debug(debug);
        debug.unindent();
    }

    public void setInitialViewSpecification(final ViewSpecification initialViewSpecification) {
        this.initialViewSpecification = initialViewSpecification;
    }

    public PanelView(final Content content, final ViewSpecification specification) {
        super(content, specification);
    }

    @Override
    protected void buildView() {
        // addView(getContent(), initialViewSpecification, null);
        final View newView = initialViewSpecification.createView(getContent(), new Axes(), 0);
        panel.addView(newView, null);
        addView(newView);
    }

    @Override
    protected void doLayout(final Size maximumSize) {
        panel.layout(maximumSize);
    }

    @Override
    public Size requiredSize(final Size availableSpace) {
        return panel.getRequiredSize(availableSpace);
    }

    @Override
    public void drop(final ViewDrag drag) {
        if (drag.getSourceView() == getView() || !contains(drag.getSourceView())) {
            super.drop(drag);
        } else {
            final Location dropAt = drag.getLocation();
            dropAt.subtract(getLocation());
            final int x = dropAt.getX();
            final int y = dropAt.getY();
            final int borderWdth = 45;
            final int left = getSize().getWidth() - borderWdth;
            final int bottom = getSize().getHeight() - borderWdth;
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

    public void addView(final Content content, final Position position) {
        final ViewRequirement requirement = new ViewRequirement(content, ViewRequirement.OPEN | ViewRequirement.SUBVIEW);
        final ViewSpecification viewSpecification = Toolkit.getViewFactory().availableViews(requirement).nextElement();
        addView(content, viewSpecification, position);
    }

    public void addView(final Content content, final ViewSpecification specification, final Position position) {
        final View newView = specification.createView(content, new Axes(), 0);
        // newView = new LineBorder(newView);
        panel.addView(newView, position);
        addView(newView);
    }
}
