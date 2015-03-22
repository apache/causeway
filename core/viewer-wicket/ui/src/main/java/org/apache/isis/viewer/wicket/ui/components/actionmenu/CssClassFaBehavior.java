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
package org.apache.isis.viewer.wicket.ui.components.actionmenu;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.isis.applib.annotation.ActionLayout;

/**
 * A behavior that prepends or appends the markup needed to show a Font Awesome icon
 * for a LinkAndLabel
 */
public class CssClassFaBehavior extends Behavior {

    private final String cssClassFa;
    private final ActionLayout.CssClassFaPosition position;

    public CssClassFaBehavior(final String cssClassFa, final ActionLayout.CssClassFaPosition position) {
        this.cssClassFa = cssClassFa;
        this.position = position;
    }

    @Override
    public void beforeRender(final Component component) {
        super.beforeRender(component);
        if (position == null || ActionLayout.CssClassFaPosition.LEFT == position) {
            component.getResponse().write("<span class=\""+cssClassFa+" fontAwesomeIcon\"></span>");
        }
    }

    @Override
    public void afterRender(final Component component) {
        if (ActionLayout.CssClassFaPosition.RIGHT == position) {
            component.getResponse().write("<span class=\""+cssClassFa+" fontAwesomeIcon\"></span>");
        }
        super.afterRender(component);
    }
}
