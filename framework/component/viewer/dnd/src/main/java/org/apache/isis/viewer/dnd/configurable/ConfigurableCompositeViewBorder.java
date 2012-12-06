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

import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.runtime.userprofile.Options;
import org.apache.isis.viewer.dnd.configurable.GridListSpecification.ElementFactory;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.util.Properties;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;
import org.apache.isis.viewer.dnd.view.base.UserViewSpecification;
import org.apache.isis.viewer.dnd.view.composite.CompositeViewDecorator;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

public class ConfigurableCompositeViewBorder extends AbstractBorder {

    public static class Factory implements CompositeViewDecorator {
        private final ElementFactory elementSpecification;

        public Factory(final ElementFactory elementSpecification) {
            this.elementSpecification = elementSpecification;
        }

        @Override
        public View decorate(final View view, final Axes axes) {
            final ConfigurationAxis axis = new ConfigurationAxis();
            // TODO load previously saved settings for the type of elements
            // axis.loadSettings(view.getContent());
            axes.add(axis);
            return new ConfigurableCompositeViewBorder(view, elementSpecification);
        }
    }

    private ViewSpecification elementSpecification;

    protected ConfigurableCompositeViewBorder(final View view, final ElementFactory elementFactory) {
        super(view);
    }

    @Override
    public void loadOptions(final Options viewOptions) {
        super.loadOptions(viewOptions);
        final String elementsClass = viewOptions.getString("elements");
        if (elementsClass != null) {
            ViewSpecification specification;
            if (elementsClass.startsWith("user:")) {
                final String name = elementsClass.substring("user:".length());
                final String wrappedSpecificationClass = Properties.getUserViewSpecificationOptions(name).getString("wrapped-specification");
                final ViewSpecification wrappedSpectification = (ViewSpecification) InstanceUtil.createInstance(wrappedSpecificationClass);
                specification = new UserViewSpecification(wrappedSpectification, name);
            } else {
                specification = (ViewSpecification) InstanceUtil.createInstance(elementsClass);
            }
            if (specification != null) {
                getViewAxes().getAxis(ConfigurationAxis.class).setElementSpecification(specification);
            }
        }
    }

    @Override
    public void saveOptions(final Options viewOptions) {
        super.saveOptions(viewOptions);
        if (elementSpecification != null) {
            final boolean isUserSpecification = elementSpecification instanceof UserViewSpecification;
            String name;
            if (isUserSpecification) {
                name = "user:" + elementSpecification.getName();
            } else {
                name = elementSpecification.getClass().getName();
            }
            viewOptions.addOption("elements", name);
        }
    }

    @Override
    public void viewMenuOptions(final UserActionSet menuOptions) {
        super.viewMenuOptions(menuOptions);
        final UserActionSet subOptions = menuOptions.addNewActionSet("Elements as");
        final View firstSubview = getSubviews()[0];
        final int status = ViewRequirement.OPEN | ViewRequirement.CLOSED | ViewRequirement.SUBVIEW | ViewRequirement.FIXED;
        final ViewRequirement viewRequirement = new ViewRequirement(firstSubview.getContent(), status);
        final Enumeration<ViewSpecification> possibleViews = Toolkit.getViewFactory().availableViews(viewRequirement);
        while (possibleViews.hasMoreElements()) {
            addElementAsOption(subOptions, possibleViews.nextElement());
        }
    }

    private void addElementAsOption(final UserActionSet subOptions, final ViewSpecification specification) {
        if (specification != elementSpecification) {
            subOptions.add(new UserActionAbstract(specification.getName()) {
                @Override
                public void execute(final Workspace workspace, final View view, final Location at) {
                    replaceElementViews(specification, view);
                }
            });
        }
    }

    private void replaceElementViews(final ViewSpecification specification, final View view) {
        elementSpecification = specification;
        removeAllSubviews(view);
        getViewAxes().getAxis(ConfigurationAxis.class).setElementSpecification(specification);
        invalidateContent();
    }

    private void removeAllSubviews(final View view) {
        final View[] subviews = view.getSubviews();
        for (final View subview : subviews) {
            view.removeView(subview);
        }
    }
}
