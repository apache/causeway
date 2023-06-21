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
package org.apache.causeway.viewer.wicket.ui.components.layout.bs.col;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.causeway.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;

import lombok.Getter;

public class RepeatingViewWithDynamicallyVisibleContent
extends RepeatingView
implements HasDynamicallyVisibleContent {

    private static final long serialVersionUID = 1L;

    @Getter(onMethod_= {@Override})
    private boolean visibleBasedOnContent = false;

    public RepeatingViewWithDynamicallyVisibleContent(final String id) {
        super(id);
    }

    @Override
    public boolean isVisible() {
        return super.isVisible()
            && isVisibleBasedOnContent();
    }

    @Override
    public MarkupContainer add(final Component... children) {
        final MarkupContainer component = super.add(children);
        for (Component child : children) {
            if(child instanceof HasDynamicallyVisibleContent) {
                final HasDynamicallyVisibleContent hasDynamicallyVisibleContent = (HasDynamicallyVisibleContent) child;
                visibleBasedOnContent = visibleBasedOnContent || hasDynamicallyVisibleContent.isVisibleBasedOnContent();
            } else {
                visibleBasedOnContent = true;
            }
        }
        return component;
    }

}
