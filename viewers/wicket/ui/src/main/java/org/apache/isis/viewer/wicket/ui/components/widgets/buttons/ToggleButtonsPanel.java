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

package org.apache.isis.viewer.wicket.ui.components.widgets.buttons;

import java.io.Serializable;

import org.apache.wicket.Component;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * Abstraction of show/hide, ie two buttons only one of which is visible.
 */
public class ToggleButtonsPanel
extends PanelAbstract<ManagedObject, EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_BUTTON_1 = "button1";
    private static final String ID_BUTTON_2 = "button2";

    private boolean flag;
    private Toggler toggler;

    private ContainedButtonPanel button1;

    private ContainedButtonPanel button2;

    public ToggleButtonsPanel(final String id, final String button1Caption, final String button2Caption) {
        super(id, null);
        this.flag = false;
        buildGui(button1Caption, button2Caption);
        onInit();
    }

    private void buildGui(final String button1Caption, final String button2Caption) {
        button1 = new ContainedButtonPanel(ID_BUTTON_1, button1Caption) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
                toggler.toggle();
            }
        };
        addOrReplace(button1);

        button2 = new ContainedButtonPanel(ID_BUTTON_2, button2Caption) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
                toggler.toggle();
            }
        };
        toggler = new Toggler(button1, button2);
        addOrReplace(button2);
    }

    public void addComponentToRerender(final Component... components) {
        for (final Component component : components) {
            button1.addComponentToRerender(component);
            button2.addComponentToRerender(component);
        }
    }

    /**
     * Hook method to override.
     */
    protected void onInit() {
    }

    /**
     * Hook method to override.
     */
    protected void onButton1() {
    }

    /**
     * Hook method to override.
     */
    protected void onButton2() {
    }

    /**
     * For subclasses to use.
     */
    protected final void hideButton1() {
        flag = true;
        toggler.syncButtonVisibility();
    }

    /**
     * For subclasses to use.
     */
    protected final void hideButton2() {
        flag = false;
        toggler.syncButtonVisibility();
    }

    private class Toggler implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Component component1;
        private final Component component2;

        public Toggler(final Component component1, final Component component2) {
            this.component1 = component1;
            this.component2 = component2;
            syncButtonVisibility();
        }

        public void toggle() {
            fireHooks();
            syncButtonVisibility();
        }

        private void fireHooks() {
            flag = !flag;
            if (flag) {
                onButton1();
            } else {
                onButton2();
            }
        }

        private void syncButtonVisibility() {
            component1.setVisible(!flag);
            component2.setVisible(flag);
        }
    }


}
