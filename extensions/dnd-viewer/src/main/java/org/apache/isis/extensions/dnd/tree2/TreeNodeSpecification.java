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


package org.apache.isis.extensions.dnd.tree2;

import org.apache.isis.extensions.dnd.form.ExpandableViewBorder;
import org.apache.isis.extensions.dnd.form.ExpandableViewBorder.Factory;
import org.apache.isis.extensions.dnd.icon.IconElementFactory;
import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.SubviewDecorator;
import org.apache.isis.extensions.dnd.view.UserActionSet;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewFactory;
import org.apache.isis.extensions.dnd.view.ViewRequirement;
import org.apache.isis.extensions.dnd.view.base.Layout;
import org.apache.isis.extensions.dnd.view.composite.CollectionElementBuilder;
import org.apache.isis.extensions.dnd.view.composite.CompositeViewSpecification;
import org.apache.isis.extensions.dnd.view.composite.ObjectFieldBuilder;
import org.apache.isis.extensions.dnd.view.composite.StackLayout;
import org.apache.isis.extensions.dnd.view.composite.ViewBuilder;


public class TreeNodeSpecification extends CompositeViewSpecification {

    public TreeNodeSpecification() {
        builder = new ViewBuilder() {
            ViewBuilder objectBuilder = new ObjectFieldBuilder(new ViewFactory() {
                public View createView(Content content, Axes axes, int sequence) {
                    if (content.isTextParseable() || content.getAdapter() == null) {
                        return null;
                    } else if (content.isObject()) {
                        return new IconElementFactory().createView(content, axes, 0); // TreeNodeSpecification.this.createView(content, axes);
                    } else {
                        return TreeNodeSpecification.this.createView(content, axes, -1);
                    }
                }
            });
            
            ViewBuilder collectiontBuilder = new CollectionElementBuilder(new IconElementFactory());

            {
                Factory decorator = new ExpandableViewBorder.Factory(null, TreeNodeSpecification.this, null);
                objectBuilder.addSubviewDecorator(decorator);
                collectiontBuilder.addSubviewDecorator(decorator);

            }

            public void addSubviewDecorator(SubviewDecorator decorator) {}

            public void build(View view, Axes axes) {
                synchronized (view) {
                    (view.getContent().isCollection() ? collectiontBuilder : objectBuilder).build(view, axes);
                }
            }

            public void createAxes(Axes axes, Content content) {}

            public boolean isOpen() {
                return false;
            }

            public boolean isReplaceable() {
                return true;
            }

            public boolean isSubView() {
                return true;
            }

            public boolean canDragView() {
                return true;
            }
            
            public void viewMenuOptions(UserActionSet options, View view) {}
        };
    }

    public Layout createLayout(Content content, Axes axes) {
        return new StackLayout();
    }

    public boolean canDisplay(ViewRequirement requirement) {
        return requirement.isObject() && requirement.isExpandable();
    }

    public String getName() {
        return "Tree Node (not working)";
    }

}

