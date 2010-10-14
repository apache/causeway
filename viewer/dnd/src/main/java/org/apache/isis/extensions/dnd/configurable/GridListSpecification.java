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

import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.GlobalViewFactory;
import org.apache.isis.extensions.dnd.view.Toolkit;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewFactory;
import org.apache.isis.extensions.dnd.view.ViewRequirement;
import org.apache.isis.extensions.dnd.view.ViewSpecification;
import org.apache.isis.extensions.dnd.view.base.Layout;
import org.apache.isis.extensions.dnd.view.composite.CollectionElementBuilder;
import org.apache.isis.extensions.dnd.view.composite.CompositeViewSpecification;
import org.apache.isis.extensions.dnd.view.composite.GridLayout;
import org.apache.isis.extensions.dnd.view.composite.GridLayoutControlBorder;


public class GridListSpecification extends CompositeViewSpecification implements ViewFactory {

    protected static class ElementFactory implements ViewFactory {
        public View createView(final Content content, Axes axes, int sequence) {
            final GlobalViewFactory factory = Toolkit.getViewFactory();

            ViewSpecification elementSpecification = axes.getAxis(ConfigurationAxis.class).getElementSpecification();
            if (elementSpecification == null) {
                int defaultRequirement = ViewRequirement.CLOSED | ViewRequirement.SUBVIEW;
                ViewRequirement viewRequirement = new ViewRequirement(content, defaultRequirement);
                return factory.createView(viewRequirement);
            } else {
                return elementSpecification.createView(content, axes, sequence);
            }
        }
    }

    public GridListSpecification() {
        final ElementFactory factory = new ElementFactory();
        builder = new CollectionElementBuilder(factory);
        // TODO allow to be switched on so that user can change the view for a single element. This type of
        // view used for an element would not be stored.
        if (false) {
            addSubviewDecorator(new ConfigurableFieldBorder.Factory());
        }

        addViewDecorator(new ConfigurableCompositeViewBorder.Factory(factory));
        addViewDecorator(new GridLayoutControlBorder.Factory());
    }

    public boolean canDisplay(ViewRequirement requirement) {
        return requirement.isCollection() && requirement.isOpen() && !requirement.isSubview() && requirement.isDesign();
    }

    public String getName() {
        return "Grid List";
    }

    public Layout createLayout(Content content, Axes axes) {
        return new GridLayout();
    }
}

