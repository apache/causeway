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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.theme.lumo.Lumo;

import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;

import lombok.val;

@CssImport(value = "./css/dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
@CssImport("./css/action-dialog.css")
public class ActionDialog extends Dialog {

    private static final long serialVersionUID = 1L;

    private final transient ManagedAction managedAction;
    
    public String DOCK = "dock";
    public String FULLSCREEN = "fullscreen";

    private boolean isDocked = false;
    private boolean isFullScreen = false;

    private Header header;
    private Button min;
    private Button max;

    private Div content;
    private Footer footer;

    public static ActionDialog forManagedAction(final ManagedAction managedAction) {
        val actionDialog = new ActionDialog(managedAction);
        return actionDialog;
    }

    protected ActionDialog(ManagedAction managedAction) {
        this.managedAction = managedAction;
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

        min = new Button(VaadinIcon.DOWNLOAD_ALT.create());
        min.addClickListener(event -> minimise());

        max = new Button(VaadinIcon.EXPAND_SQUARE.create());
        max.addClickListener(event -> maximise());

        Button close = new Button(VaadinIcon.CLOSE_SMALL.create());
        close.addClickListener(event -> close());

        header = new Header(title, min, max, close);
        header.getElement().getThemeList().add(Lumo.DARK);
        add(header);

        // Content
        val stub = new Label("Under Construction ...");
        
        content = new Div(stub);
        content.addClassName("dialog-content");
        add(content);

        // Footer
        Button send = new Button("Send");
        send.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button attachFiles = new Button(VaadinIcon.PAPERCLIP.create());
        Button discardDraft = new Button(VaadinIcon.TRASH.create());

        footer = new Footer(send, attachFiles, discardDraft);
        add(footer);

        // Button theming
        for (Button button : new Button[] {min, max, close, attachFiles, discardDraft}) {
            button.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_TERTIARY);
        }
        
        
    }

    private void minimise() {
        if (isDocked) {
            initialSize();
        } else {
            if (isFullScreen) {
                initialSize();
            }
            min.setIcon(VaadinIcon.UPLOAD_ALT.create());
            getElement().getThemeList().add(DOCK);
            setWidth("320px");
        }
        isDocked = !isDocked;
        isFullScreen = false;
        content.setVisible(!isDocked);
        footer.setVisible(!isDocked);
    }

    private void initialSize() {
        min.setIcon(VaadinIcon.DOWNLOAD_ALT.create());
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
