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
package org.apache.isis.viewer.wicket.ui.components.layout.bs3.col;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;

public class RepeatingViewWithDynamicallyVisibleContent 
extends RepeatingView 
implements HasDynamicallyVisibleContent {

    private static final long serialVersionUID = 1L;

    public RepeatingViewWithDynamicallyVisibleContent(final String id) {
        super(id);
    }

    @Override
    public MarkupContainer add(final Component... children) {
        final MarkupContainer component = super.add(children);
        for (Component child : children) {
            if(child instanceof HasDynamicallyVisibleContent) {
                final HasDynamicallyVisibleContent hasDynamicallyVisibleContent = (HasDynamicallyVisibleContent) child;
                visible = visible || hasDynamicallyVisibleContent.isVisible();
            } else {
                visible = true;
            }
        }
        return component;
    }

    private boolean visible = false;

    @Override
    public boolean isVisible() {
        return visible;
    }

}
