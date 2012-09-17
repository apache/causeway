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

package org.apache.isis.viewer.wicket.ui.tabs;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;

import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * Adapter class for tabbed panels; does very little in itself, but does ensure
 * that the CSS needed is rendered.
 */
public abstract class TabbedPanelAbstract<T extends IModel<?>> extends PanelAbstract<T> {

    private static final long serialVersionUID = 1L;

    public TabbedPanelAbstract(final String id, final T model) {
        super(id, model);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        renderHead(response, TabbedPanelAbstract.class);
    }

}
