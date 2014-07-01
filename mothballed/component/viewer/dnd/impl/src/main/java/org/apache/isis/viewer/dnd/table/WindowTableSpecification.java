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
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.border.ScrollBorder;
import org.apache.isis.viewer.dnd.view.composite.CompositeViewDecorator;
import org.apache.isis.viewer.dnd.viewer.basic.TableFocusManager;

public class WindowTableSpecification extends AbstractTableSpecification {
    public WindowTableSpecification() {
        addViewDecorator(new CompositeViewDecorator() {
            @Override
            public View decorate(final View view, final Axes axes) {
                final ScrollBorder scrollingView = new ScrollBorder(view);
                final View viewWithWindowBorder = scrollingView;
                // note - the next call needs to be after the creation of the
                // window border so
                // that it exists when the header is set up
                scrollingView.setTopHeader(new TableHeader(view.getContent(), axes.getAxis(TableAxis.class)));
                viewWithWindowBorder.setFocusManager(new TableFocusManager(viewWithWindowBorder));
                return viewWithWindowBorder;
            }
        });

    }

    /*
     * @Override public View doCreateView(final View view, final Content
     * content, final ViewAxis axis) { if (true) return view;
     * 
     * final ScrollBorder scrollingView = new ScrollBorder(view); View
     * viewWithWindowBorder = scrollingView; // note - the next call needs to be
     * after the creation of the window border so // that it exists when the
     * header is set up scrollingView.setTopHeader(new TableHeader(content,
     * view.getViewAxisForChildren())); viewWithWindowBorder.setFocusManager(new
     * TableFocusManager(viewWithWindowBorder)); return viewWithWindowBorder; }
     * 
     * protected View decorateView(View view) { super.decorateView(view);
     * 
     * final ScrollBorder scrollingView = new ScrollBorder(view); View
     * viewWithWindowBorder = scrollingView; // note - the next call needs to be
     * after the creation of the window border so // that it exists when the
     * header is set up scrollingView.setTopHeader(new
     * TableHeader(view.getContent(), view.getViewAxisForChildren()));
     * viewWithWindowBorder.setFocusManager(new
     * TableFocusManager(viewWithWindowBorder)); return viewWithWindowBorder; }
     */
    @Override
    public String getName() {
        return "Table";
    }

    @Override
    public boolean isReplaceable() {
        return false;
    }

}
