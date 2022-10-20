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
package org.apache.causeway.viewer.wicket.ui.components.tree.themes.bootstrap;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

public class WktBootstrapTreeTheme extends Behavior {
    private static final long serialVersionUID = 1L;

    private static final ResourceReference CSS =
            new CssResourceReference(WktBootstrapTreeTheme.class, "wkt-tree-theme.css");

    @Override
    public void onComponentTag(final Component component, final ComponentTag tag) {
        tag.append("class", "tree-theme-bootstrap", " ");
    }

    @Override
    public void renderHead(final Component component, final IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(CSS));
    }
}
