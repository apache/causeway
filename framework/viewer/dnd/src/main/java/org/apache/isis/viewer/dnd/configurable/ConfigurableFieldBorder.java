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

import java.util.Enumeration;

import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.SubviewDecorator;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.UserAction;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAxis;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.ViewState;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.axis.LabelAxis;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;
import org.apache.isis.viewer.dnd.view.base.BlankView;
import org.apache.isis.viewer.dnd.view.border.LabelBorder;
import org.apache.isis.viewer.dnd.view.option.ReplaceViewOption;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

public class ConfigurableFieldBorder extends AbstractBorder {
    public static final class Factory implements SubviewDecorator {
        @Override
        public ViewAxis createAxis(final Content content) {
            return null;
        }

        @Override
        public View decorate(final Axes axes, final View view) {
            return new ConfigurableFieldBorder(view);
        }
    }

    private static final int BORDER = 10;

    protected ConfigurableFieldBorder(final View view) {
        super(view);
        right = BORDER;
    }

    @Override
    public void viewMenuOptions(final UserActionSet menuOptions) {
        super.viewMenuOptions(menuOptions);

        menuOptions.add(new UserActionAbstract("Hide") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                final View parent = wrappedView.getParent();
                wrappedView = new BlankView(getContent());
                wrappedView.setParent(parent);
                wrappedView.setView(ConfigurableFieldBorder.this);
                invalidateLayout();
            }

        });

        menuOptions.add(new UserActionAbstract("Show label") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                if (wrappedView instanceof LabelBorder) {
                    wrappedView = ((LabelBorder) wrappedView).getWrapped();
                } else {
                    wrappedView = LabelBorder.createFieldLabelBorder(view.getParent().getViewAxes().getAxis(LabelAxis.class), wrappedView);
                }
                wrappedView.setView(ConfigurableFieldBorder.this);
                getView().invalidateLayout();
            }
        });

        replaceOptions(Toolkit.getViewFactory().availableViews(new ViewRequirement(getContent(), ViewRequirement.OPEN | ViewRequirement.CLOSED | ViewRequirement.SUBVIEW)), menuOptions); // openSubviews(content,
                                                                                                                                                                                          // this),
                                                                                                                                                                                          // options);

    }

    // TODO copied from AbstractView
    protected void replaceOptions(final Enumeration possibleViews, final UserActionSet options) {
        if (possibleViews.hasMoreElements()) {
            final UserActionSet suboptions = options.addNewActionSet("Replace with");
            while (possibleViews.hasMoreElements()) {
                final ViewSpecification specification = (ViewSpecification) possibleViews.nextElement();

                if (specification != getSpecification()) {
                    final UserAction viewAs = new ReplaceViewOption(specification) {
                        @Override
                        protected void replace(final View view, final View withReplacement) {
                            final View parent = wrappedView.getParent();
                            wrappedView = LabelBorder.createFieldLabelBorder(view.getParent().getViewAxes().getAxis(LabelAxis.class), withReplacement);
                            wrappedView.setParent(parent);
                            wrappedView.setView(ConfigurableFieldBorder.this);
                            invalidateLayout();
                        }
                    };
                    suboptions.add(viewAs);
                }
            }
        }
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);

        final ViewState state = getState();
        if (state.isViewIdentified()) {
            final Size s = getSize();
            final int xExtent = s.getWidth();
            if (state.isViewIdentified()) {
                canvas.drawSolidRectangle(xExtent - BORDER + 1, top, BORDER - 2, s.getHeight() - 2 * top, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
            }
        }
    }

    @Override
    public void entered() {
        getState().setViewIdentified();
        wrappedView.entered();
        markDamaged();
    }

    @Override
    public void exited() {
        getState().clearViewIdentified();
        wrappedView.exited();
        markDamaged();
    }

}
