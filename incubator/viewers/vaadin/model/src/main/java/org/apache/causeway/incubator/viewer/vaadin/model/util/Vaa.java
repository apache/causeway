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
package org.apache.causeway.incubator.viewer.vaadin.model.util;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Label;

import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * Vaadin common idioms, in alphabetical order.
 */
@UtilityClass
public class Vaa {

    // -- COMPONENT FACTORIES

    public <T extends Component> T add(final HasComponents container, final T component) {
        container.add(component);
        return component;
    }

    public Button newButton(final String label) {
        val component = new Button(label);
        component.getStyle().set("margin-left", "0.5em");
        component.addThemeVariants(ButtonVariant.LUMO_SMALL);
        return component;
    }

    public Button newButton(final HasComponents container, final String label, final ComponentEventListener<ClickEvent<Button>> eventHandler) {
        val component = newButton(label);
        container.add(component);
        component.addClickListener(eventHandler);
        return component;
    }

    public Label newLabel(final HasComponents container, final String label) {
        val component = new Label(label);
        container.add(component);
        return component;
    }

    // -- COMPONENT EVENTS

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T extends Component> T setOnClick(
            final T component,
            final Runnable onClick) {
        ComponentUtil.addListener(component, ClickEvent.class,
                (ComponentEventListener) e->onClick.run());
        return component;
    }

}
