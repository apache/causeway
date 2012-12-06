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

package org.apache.isis.viewer.dnd.table;

import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.Layout;
import org.apache.isis.viewer.dnd.view.border.ScrollBorder;
import org.apache.isis.viewer.dnd.view.composite.CompositeViewDecorator;
import org.apache.isis.viewer.dnd.view.composite.StackLayout;
import org.apache.isis.viewer.dnd.viewer.basic.TableFocusManager;

public class InternalTableSpecification extends AbstractTableSpecification {
    public InternalTableSpecification() {
        addViewDecorator(new CompositeViewDecorator() {
            @Override
            public View decorate(final View view, final Axes axes) {
                final ScrollBorder scrollingView = new ScrollBorder(view);
                // note - the next call needs to be after the creation of the
                // window border
                // so that it exists when the header is set up
                scrollingView.setTopHeader(new TableHeader(view.getContent(), axes.getAxis(TableAxis.class)));
                scrollingView.setFocusManager(new TableFocusManager(scrollingView));
                return scrollingView;

            }
        });

    }

    @Override
    public Layout createLayout(final Content content, final Axes axes) {
        return new StackLayout();
    }

    // TODO remove
    /*
     * @Override public View doCreateView(final View view, final Content
     * content, final ViewAxis axis) { final ScrollBorder scrollingView = new
     * ScrollBorder(view); // note - the next call needs to be after the
     * creation of the window border // so that it exists when the header is set
     * up scrollingView.setTopHeader(new TableHeader(content));
     * scrollingView.setFocusManager(new TableFocusManager(scrollingView));
     * return scrollingView; }
     * 
     * protected View decorateView(View view) { super.decorateView(view);
     * 
     * final ScrollBorder scrollingView = new ScrollBorder(view); // note - the
     * next call needs to be after the creation of the window border // so that
     * it exists when the header is set up scrollingView.setTopHeader(new
     * TableHeader(view.getContent())); scrollingView.setFocusManager(new
     * TableFocusManager(scrollingView)); return scrollingView; }
     */
    @Override
    public String getName() {
        return "Table";
    }
}
