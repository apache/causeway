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

package org.apache.isis.viewer.dnd.view.composite;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.base.Layout;

public class CompositeViewUsingBuilder extends CompositeView {
    private final ViewBuilder builder;
    private final Layout layout;
    private final Axes axes = new Axes();

    public CompositeViewUsingBuilder(final Content content, final ViewSpecification specification, final Axes axes, final Layout layout, final ViewBuilder builder) {
        super(content, specification);
        this.layout = layout;
        this.builder = builder;
        this.axes.add(axes);
    }

    @Override
    public void debugStructure(final DebugBuilder debug) {
        debug.appendln("Builder", builder);
        debug.appendln("Axes", axes);
        super.debugStructure(debug);
    }

    @Override
    public Size requiredSize(final Size availableSpace) {
        final Size size = layout.getRequiredSize(this);
        size.extend(getPadding());
        size.ensureHeight(1);
        return size;
    }

    @Override
    protected void buildView() {
        builder.build(getView(), axes);
    }

    @Override
    protected void doLayout(final Size maximumSize) {
        layout.layout(getView(), new Size(maximumSize));
    }

    @Override
    public Axes getViewAxes() {
        return axes;
    }

    @Override
    protected void appendDebug(final DebugBuilder debug) {
        super.appendDebug(debug);
        debug.appendln("Layout", layout);
    }

    public Layout getLayout() {
        return layout;
    }

    @Override
    public void viewMenuOptions(final UserActionSet options) {
        super.viewMenuOptions(options);
        builder.viewMenuOptions(options, this);
    }
}
