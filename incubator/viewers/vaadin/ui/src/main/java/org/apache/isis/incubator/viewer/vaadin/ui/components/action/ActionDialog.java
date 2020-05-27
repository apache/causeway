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

import java.util.stream.Stream;

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

import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentFactoryVaa;

import lombok.NonNull;
import lombok.val;

@CssImport(value = "./css/dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
@CssImport("./css/action-dialog.css")
public class ActionDialog extends Dialog {

    private static final long serialVersionUID = 1L;
    private static final String DOCK = "dock";
    private static final String FULLSCREEN = "fullscreen";

    private boolean isDocked = false;
    private boolean isFullScreen = false;

    private Header header;
    private Button min;
    private Button max;

    private Div content;
    private Footer footer;

    public static ActionDialog forManagedAction(
            @NonNull final UiComponentFactoryVaa uiComponentFactory, 
            @NonNull final ManagedAction managedAction) {
        
        val actionDialog = new ActionDialog(uiComponentFactory, managedAction);
        return actionDialog;
    }

    protected ActionDialog(
            final UiComponentFactoryVaa uiComponentFactory,
            final ManagedAction managedAction) {
        
        setDraggable(true);
        setModal(false);
        setResizable(true);
        
        // Dialog theming
        getElement().getThemeList().add("action-dialog");
        setWidth("600px");
        setHeight("auto");
        
        // Header
        val title = new H2(managedAction.getName());
        title.addClassName("dialog-title");

        min = new Button(VaadinIcon.ANGLE_DOWN.create());
        min.addClickListener(event -> minimise());

        max = new Button(VaadinIcon.EXPAND_SQUARE.create());
        max.addClickListener(event -> maximise());

        val closeButton = new Button(VaadinIcon.CLOSE_SMALL.create());

        header = new Header(title, min, max, closeButton);
        header.getElement().getThemeList().add(Lumo.DARK);
        add(header);

        // Content
        val actionForm = ActionForm.forManagedAction(uiComponentFactory, managedAction);
        
        content = new Div(actionForm);
        content.addClassName("dialog-content");
        add(content);

        // Footer
        val okButton = new Button("Ok");
        val cancelButton = new Button("Cancel");
        footer = new Footer(okButton, cancelButton);
        add(footer);

        // Button Themes
        
        okButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        Stream.of(min, max, closeButton)
        .forEach(button->
            button.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_TERTIARY));
        
        // Button Events
        
        okButton.addClickListener(event -> close()); //TODO should invoke the action
        cancelButton.addClickListener(event -> close());
        closeButton.addClickListener(event -> close());
        
    }

    // -- HELPER
    
    private void minimise() {
        if (isDocked) {
            initialSize();
        } else {
            if (isFullScreen) {
                initialSize();
            }
            min.setIcon(VaadinIcon.ANGLE_UP.create());
            getElement().getThemeList().add(DOCK);
            setWidth("320px");
        }
        isDocked = !isDocked;
        isFullScreen = false;
        content.setVisible(!isDocked);
        footer.setVisible(!isDocked);
    }

    private void initialSize() {
        min.setIcon(VaadinIcon.ANGLE_DOWN.create());
        getElement().getThemeList().remove(DOCK);
        max.setIcon(VaadinIcon.EXPAND_SQUARE.create());
        getElement().getThemeList().remove(FULLSCREEN);
        setHeight("auto");
        setWidth("600px");
    }

    private void maximise() {
        if (isFullScreen) {
            initialSize();
        } else {
            if (isDocked) {
                initialSize();
            }
            max.setIcon(VaadinIcon.COMPRESS_SQUARE.create());
            getElement().getThemeList().add(FULLSCREEN);
            setSizeFull();
            content.setVisible(true);
            footer.setVisible(true);
        }
        isFullScreen = !isFullScreen;
        isDocked = false;
    }

}
