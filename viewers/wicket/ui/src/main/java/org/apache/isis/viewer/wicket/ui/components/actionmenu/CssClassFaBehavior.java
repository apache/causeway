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

import org.apache.isis.applib.layout.component.CssClassFaPosition;
import org.apache.isis.viewer.common.model.decorator.icon.FontAwesomeUiModel;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * A behavior that prepends or appends the markup needed to show a Font Awesome icon
 * for a LinkAndLabel
 */
@RequiredArgsConstructor
public class CssClassFaBehavior extends Behavior {

    private static final long serialVersionUID = 1L;
    
    @NonNull private final FontAwesomeUiModel fontAwesomeUiModel;

    @Override
    public void beforeRender(final Component component) {
        super.beforeRender(component);
        val position = fontAwesomeUiModel.getPosition();
        if (position == null || CssClassFaPosition.LEFT == position) {
            val cssClassFa = fontAwesomeUiModel.getCssClassesSpaceSeparated();
            component.getResponse().write("<span class=\""+cssClassFa+" fontAwesomeIcon\"></span>");
        }
    }

    @Override
    public void afterRender(final Component component) {
        val position = fontAwesomeUiModel.getPosition();
        if (CssClassFaPosition.RIGHT == position) {
            val cssClassFa = fontAwesomeUiModel.getCssClassesSpaceSeparated();
            component.getResponse().write("<span class=\""+cssClassFa+" fontAwesomeIcon\"></span>");
        }
        super.afterRender(component);
    }

}
