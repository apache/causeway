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

import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;

public abstract class AbstractBuilderDecorator implements ViewBuilder {
    protected final ViewBuilder wrappedBuilder;

    public AbstractBuilderDecorator(final ViewBuilder design) {
        this.wrappedBuilder = design;
    }

    @Override
    public void build(final View view, final Axes axes) {
        wrappedBuilder.build(view, axes);
    }

    @Override
    public boolean isOpen() {
        return wrappedBuilder.isOpen();
    }

    @Override
    public boolean isReplaceable() {
        return wrappedBuilder.isReplaceable();
    }

    @Override
    public boolean isSubView() {
        return wrappedBuilder.isSubView();
    }

    @Override
    public String toString() {
        final String name = getClass().getName();
        return wrappedBuilder + "/" + name.substring(name.lastIndexOf('.') + 1);
    }

    @Override
    public void viewMenuOptions(final UserActionSet options, final View view) {
        wrappedBuilder.viewMenuOptions(options, view);
    }
}
