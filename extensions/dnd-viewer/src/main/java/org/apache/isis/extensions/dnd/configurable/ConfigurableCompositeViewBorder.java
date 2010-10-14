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

import java.util.Enumeration;

import org.apache.isis.commons.factory.InstanceFactory;
import org.apache.isis.extensions.dnd.configurable.GridListSpecification.ElementFactory;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.util.Properties;
import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.Toolkit;
import org.apache.isis.extensions.dnd.view.UserActionSet;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewRequirement;
import org.apache.isis.extensions.dnd.view.ViewSpecification;
import org.apache.isis.extensions.dnd.view.Workspace;
import org.apache.isis.extensions.dnd.view.base.AbstractBorder;
import org.apache.isis.extensions.dnd.view.base.UserViewSpecification;
import org.apache.isis.extensions.dnd.view.composite.CompositeViewDecorator;
import org.apache.isis.extensions.dnd.view.option.UserActionAbstract;
import org.apache.isis.runtime.userprofile.Options;


public class ConfigurableCompositeViewBorder extends AbstractBorder {

    public static class Factory implements CompositeViewDecorator {
        private final ElementFactory elementSpecification;

        public Factory(ElementFactory elementSpecification) {
            this.elementSpecification = elementSpecification;
        }

        public View decorate(View view, Axes axes) {
            ConfigurationAxis axis = new ConfigurationAxis();
            // TODO load previously saved settings for the type of elements
            // axis.loadSettings(view.getContent());
            axes.add(axis);
            return new ConfigurableCompositeViewBorder(view, elementSpecification);
        }
    }

    private ViewSpecification elementSpecification;

    protected ConfigurableCompositeViewBorder(View view, ElementFactory elementFactory) {
        super(view);
    }

    public void loadOptions(Options viewOptions) {
        super.loadOptions(viewOptions);
        String elementsClass = viewOptions.getString("elements");
        if (elementsClass != null ) {
                ViewSpecification specification;
                if (elementsClass.startsWith("user:")) {
                    String name = elementsClass.substring("user:".length());
                    String wrappedSpecificationClass = Properties.getUserViewSpecificationOptions(name).getString("wrapped-specification");
                    ViewSpecification wrappedSpectification = (ViewSpecification) InstanceFactory.createInstance(wrappedSpecificationClass);
                    specification = new UserViewSpecification(wrappedSpectification, name);
                } else {
                    specification = (ViewSpecification) InstanceFactory.createInstance(elementsClass);
                }
                if (specification != null) {
                    getViewAxes().getAxis(ConfigurationAxis.class).setElementSpecification(specification);
                }
        }
    }
    
    public void saveOptions(Options viewOptions) {
        super.saveOptions(viewOptions);
        if (elementSpecification != null) {
            boolean isUserSpecification = elementSpecification instanceof UserViewSpecification;
            String name;
            if (isUserSpecification) {
                name = "user:" + elementSpecification.getName();
            } else {
                name = elementSpecification.getClass().getName();
            }
            viewOptions.addOption("elements", name);
        }
    }
    
    
    public void viewMenuOptions(UserActionSet menuOptions) {
        super.viewMenuOptions(menuOptions);
        UserActionSet subOptions = menuOptions.addNewActionSet("Elements as");
        View firstSubview = getSubviews()[0];
        int status = ViewRequirement.OPEN | ViewRequirement.CLOSED | ViewRequirement.SUBVIEW | ViewRequirement.FIXED;
        ViewRequirement viewRequirement = new ViewRequirement(firstSubview.getContent(), status);
        Enumeration<ViewSpecification> possibleViews = Toolkit.getViewFactory().availableViews(viewRequirement);
        while (possibleViews.hasMoreElements()) {
            addElementAsOption(subOptions, possibleViews.nextElement());
        }
    }

    private void addElementAsOption(UserActionSet subOptions, final ViewSpecification specification) {
        if (specification != elementSpecification) {
            subOptions.add(new UserActionAbstract(specification.getName()) {
                public void execute(Workspace workspace, View view, Location at) {
                    replaceElementViews(specification, view);
                }
            });
        }
    }

    private void replaceElementViews(final ViewSpecification specification, View view) {
        elementSpecification = specification;
        removeAllSubviews(view);
        getViewAxes().getAxis(ConfigurationAxis.class).setElementSpecification(specification);
        invalidateContent();
    }

    private void removeAllSubviews(View view) {
        final View[] subviews = view.getSubviews();
        for (int i = 0; i < subviews.length; i++) {
            view.removeView(subviews[i]);
        }
    }
}

