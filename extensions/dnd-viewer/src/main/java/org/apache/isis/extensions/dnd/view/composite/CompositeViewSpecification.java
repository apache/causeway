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


package org.apache.isis.extensions.dnd.view.composite;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.extensions.dnd.util.Properties;
import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.ObjectContent;
import org.apache.isis.extensions.dnd.view.SubviewDecorator;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.ViewAxis;
import org.apache.isis.extensions.dnd.view.ViewSpecification;
import org.apache.isis.extensions.dnd.view.base.Layout;
import org.apache.isis.extensions.dnd.view.window.SubviewFocusManager;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.userprofile.Options;


public abstract class CompositeViewSpecification implements ViewSpecification {
    protected ViewBuilder builder;
    private final List<CompositeViewDecorator> viewDecorators = new ArrayList<CompositeViewDecorator>();

    public final View createView(final Content content, final Axes axes, int sequence) {
        resolveObject(content);
        
        createAxes(content, axes);
        builder.createAxes(axes, content);
        Layout layout = createLayout(content, axes);
        if (layout instanceof ViewAxis) {
            axes.add((ViewAxis) layout);
        }

        final CompositeViewUsingBuilder view = new CompositeViewUsingBuilder(content, this, axes, layout, builder);
        view.setCanDragView(builder.canDragView());
        View decoratedView = decorateView(view, view.getViewAxes());
        Options options = Properties.getViewConfigurationOptions(this);
        decoratedView.loadOptions(options);
        return decoratedView;
    }

    private void resolveObject(final Content content) {
        if (content instanceof ObjectContent) {
            final ObjectAdapter object = ((ObjectContent) content).getObject();
            if (object != null && !object.getResolveState().isResolved()) {
                IsisContext.getPersistenceSession().resolveImmediately(object);
            }
        }
    }
    
    private View decorateView(final View view, final Axes axes) {
        view.setFocusManager(new SubviewFocusManager(view));
        View decorated = view;
        for (CompositeViewDecorator decorator : viewDecorators) {
            decorated = decorator.decorate(decorated, axes);
        }
        return decorated;
    }

    public void addViewDecorator(CompositeViewDecorator decorator) {
        viewDecorators.add(decorator);
    }

    public void addSubviewDecorator(SubviewDecorator decorator) {
        builder.addSubviewDecorator(decorator);
    }
    
    protected abstract Layout createLayout(Content content, Axes axes);

    protected void createAxes(Content content, Axes axes) {}

    public boolean isOpen() {
        return true;
    }

    public boolean isReplaceable() {
        return true;
    }

    public boolean isSubView() {
        return false;
    }

    public boolean isAligned() {
        return false;
    }
    
    public boolean isResizeable() {
        return false;
    }

}
