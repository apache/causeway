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

package org.apache.isis.viewer.dnd.view.lookup;

import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewFactory;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.base.Layout;
import org.apache.isis.viewer.dnd.view.border.BackgroundBorder;
import org.apache.isis.viewer.dnd.view.border.LineBorder;
import org.apache.isis.viewer.dnd.view.border.ScrollBorder;
import org.apache.isis.viewer.dnd.view.composite.CompositeViewDecorator;
import org.apache.isis.viewer.dnd.view.composite.CompositeViewSpecification;
import org.apache.isis.viewer.dnd.view.composite.StackLayout;

public abstract class SelectionListSpecification extends CompositeViewSpecification {

    public SelectionListSpecification() {
        builder = new SelectionListBuilder(new ViewFactory() {
            @Override
            public View createView(final Content content, final Axes axes, final int fieldNumber) {
                final View elementView = createElementView(content);
                final SelectionListAxis axis = axes.getAxis(SelectionListAxis.class);
                axes.add(axis);
                return new SelectionItemSelector(elementView, axis);
            }

        });
        addViewDecorator(new CompositeViewDecorator() {
            @Override
            public View decorate(final View view, final Axes axes) {
                final SelectionListAxis axis = axes.getAxis(SelectionListAxis.class);
                final View list = new SelectionListFocusBorder(view, axis);
                return new DisposeOverlay(new BackgroundBorder(new LineBorder(new ScrollBorder(list))), axis);
            }
        });
    }

    protected abstract View createElementView(Content content);

    @Override
    public Layout createLayout(final Content content, final Axes axes) {
        return new StackLayout(true);
    }

    @Override
    public boolean canDisplay(final ViewRequirement requirement) {
        throw new UnexpectedCallException();
    }

    @Override
    public String getName() {
        return "Object Drop Down Overlay";
    }

}
