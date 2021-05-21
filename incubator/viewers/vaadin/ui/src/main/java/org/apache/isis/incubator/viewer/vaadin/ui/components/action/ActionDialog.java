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
package org.apache.isis.incubator.viewer.vaadin.ui.components.action;

import java.util.function.Predicate;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.theme.lumo.Lumo;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentFactoryVaa;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@CssImport(value = "./css/dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
@CssImport("./css/action-dialog.css")
public class ActionDialog extends Dialog {

    private static final long serialVersionUID = 1L;

    public static ActionDialog forManagedAction(
            @NonNull final UiComponentFactoryVaa uiComponentFactory,
            @NonNull final ManagedAction managedAction,
            @NonNull final Predicate<Can<ManagedObject>> submitCallback) {

        val actionDialog = new ActionDialog(uiComponentFactory, managedAction, submitCallback);
        return actionDialog;
    }

    protected ActionDialog(
            final UiComponentFactoryVaa uiComponentFactory,
            final ManagedAction managedAction,
            final Predicate<Can<ManagedObject>> submitCallback) {

        setDraggable(true);
        setModal(false);
        setResizable(true);

        // Dialog Theme

        getElement().getThemeList().add("action-dialog");
        setWidth("600px");
        setHeight("auto");

        // Content

        val actionForm = ActionForm.forManagedAction(uiComponentFactory, managedAction);
        val content = new Div(actionForm);
        content.addClassName("dialog-content");

        // Footer

        val footer = footer(managedAction, actionForm.getPendingArgs(), submitCallback);

        // Header

        val hidableComponents = Can.of(content, footer);
        val header = header(managedAction, hidableComponents);

        // Add to Layout

        add(header, content, footer);
    }

    // -- HELPER

    private Component header(ManagedAction managedAction, Can<Component> hidableComponents) {

        val resizeHandler = DialogResizeHandler.of(this, hidableComponents);

        val title = new H2(managedAction.getName());
        title.addClassName("dialog-title");

        val minButton = new Button(VaadinIcon.ANGLE_DOWN.create());
        val maxButton = new Button(VaadinIcon.EXPAND_SQUARE.create());
        val closeButton = new Button(VaadinIcon.CLOSE_SMALL.create());

        val header = new Header(title, minButton, maxButton, closeButton);
        header.getElement().getThemeList().add(Lumo.DARK);

        // Button Themes
        Stream.of(minButton, maxButton, closeButton)
        .forEach(button->
            button.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_TERTIARY));

        // Button Events
        resizeHandler.bindMinimise(minButton);
        resizeHandler.bindMaximise(maxButton);
        closeButton.addClickListener(event -> close());

        return header;
    }

    private Component footer(
            ManagedAction managedAction,
            ParameterNegotiationModel pendingArgs,
            Predicate<Can<ManagedObject>> submitCallback) {

        val okButton = new Button("Ok");
        val cancelButton = new Button("Cancel");
        val footer = new Footer(okButton, cancelButton);

        // Button Themes
        okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Button Events
        okButton.addClickListener(event -> {
            //invoke the action and route to the result page
            if(submitCallback.test(pendingArgs.getParamValues())) {
                close();
            } else {
                //TODO handle validation feedback (vetos)
            }
        });
        cancelButton.addClickListener(event -> close());

        return footer;
    }

    // -- RESIZING

    @RequiredArgsConstructor(staticName = "of")
    private static class DialogResizeHandler {

        private static final String DOCK = "dock";
        private static final String FULLSCREEN = "fullscreen";

        private boolean isDocked = false;
        private boolean isFullScreen = false;

        private final Dialog dialog;
        private final Can<Component> hidableComponents;

        private Button minButton;
        private Button maxButton;

        public void bindMinimise(Button minButton) {
            this.minButton = minButton;
            minButton.addClickListener(event -> minimise());
        }

        public void bindMaximise(Button maxButton) {
            this.maxButton = maxButton;
            maxButton.addClickListener(event -> maximise());
        }

        private void initialSize() {
            minButton.setIcon(VaadinIcon.ANGLE_DOWN.create());
            dialog.getElement().getThemeList().remove(DOCK);
            maxButton.setIcon(VaadinIcon.EXPAND_SQUARE.create());
            dialog.getElement().getThemeList().remove(FULLSCREEN);
            dialog.setWidth("600px");
            dialog.setHeight("auto");
        }

        private void minimise() {
            if (isDocked) {
                initialSize();
            } else {
                if (isFullScreen) {
                    initialSize();
                }
                minButton.setIcon(VaadinIcon.ANGLE_UP.create());
                dialog.getElement().getThemeList().add(DOCK);
                dialog.setWidth("320px");
            }
            isDocked = !isDocked;
            isFullScreen = false;
            hidableComponents.forEach(comp->comp.setVisible(!isDocked));
        }

        private void maximise() {
            if (isFullScreen) {
                initialSize();
            } else {
                if (isDocked) {
                    initialSize();
                }
                maxButton.setIcon(VaadinIcon.COMPRESS_SQUARE.create());
                dialog.getElement().getThemeList().add(FULLSCREEN);
                dialog.setSizeFull();
                hidableComponents.forEach(comp->comp.setVisible(true));
            }
            isFullScreen = !isFullScreen;
            isDocked = false;
        }

    }

}
