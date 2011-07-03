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

package org.apache.isis.viewer.dnd.view.border;

import java.awt.event.KeyEvent;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.KeyboardAction;
import org.apache.isis.viewer.dnd.view.SubviewDecorator;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAxis;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

public class SelectObjectBorder extends AbstractBorder {
    private final SelectableViewAxis axis;

    public static class Factory implements SubviewDecorator {
        @Override
        public ViewAxis createAxis(final Content content) {
            return null;
        }

        @Override
        public View decorate(final Axes axes, final View view) {
            if (axes.contains(SelectableViewAxis.class)) {
                final SelectableViewAxis axis = axes.getAxis(SelectableViewAxis.class);
                return new SelectObjectBorder(view, axis);
            } else {
                return view;
            }
        }
    }

    protected SelectObjectBorder(final View view, final SelectableViewAxis axis) {
        super(view);
        this.axis = axis;
    }

    @Override
    public Axes getViewAxes() {
        final Axes viewAxes = super.getViewAxes();
        viewAxes.add(axis);
        return viewAxes;
    }

    @Override
    protected void debugDetails(final DebugBuilder debug) {
        super.debugDetails(debug);
        debug.appendln("axis", axis);
    }

    @Override
    public void keyPressed(final KeyboardAction key) {
        if (key.getKeyCode() == KeyEvent.VK_SPACE) {
            selectNode();
        } else {
            super.keyPressed(key);
        }
    }

    @Override
    public void firstClick(final Click click) {
        final int x = click.getLocation().getX();
        final int y = click.getLocation().getY();
        if (withinSelectorBounds(x, y) && click.button1()) {
            selectNode();
        } else {
            super.firstClick(click);
        }
    }

    private void selectNode() {
        axis.selected(getView());
    }

    private boolean withinSelectorBounds(final int x, final int y) {
        return true;
    }

    @Override
    public void viewMenuOptions(final UserActionSet options) {
        super.viewMenuOptions(options);
        options.add(new UserActionAbstract("Select node") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                selectNode();
            }

            @Override
            public String getDescription(final View view) {
                return "Show this node in the right-hand pane";
            }
        });
    }

    @Override
    public void draw(final Canvas canvas) {
        if (axis.isSelected(getView())) {
            clearBackground(canvas, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
        }
        super.draw(canvas);
    }
}
