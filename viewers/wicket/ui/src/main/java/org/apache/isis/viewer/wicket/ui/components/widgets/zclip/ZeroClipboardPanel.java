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
package org.apache.isis.viewer.wicket.ui.components.widgets.zclip;

import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ObjectAdapterModel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import de.agilecoders.wicket.jquery.util.Strings2;

public class ZeroClipboardPanel
extends PanelAbstract<ManagedObject, ObjectAdapterModel> {

    private static final long serialVersionUID = 1L;

//    private static final String ID_SUBSCRIBING_LINK = "subscribingLink";
    private static final String ID_COPY_LINK = "copyLink";
    private static final String ID_SIMPLE_CLIPBOARD_MODAL_WINDOW = "simpleClipboardModalWindow";

    private AjaxLink<Void> copyLink;
    private SimpleClipboardModalWindow simpleClipboardModalWindow;

    public ZeroClipboardPanel(final String id, final ObjectAdapterModel entityModel) {
        super(id, entityModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        if(copyLink == null) {
            copyLink = createLink(ID_COPY_LINK);
            addOrReplace(copyLink);
        }
        ObjectAdapterModel model = getModel();
        addSimpleClipboardModalWindow();

        EntityModel.RenderingHint renderingHint = model.getRenderingHint();
        ScalarRepresentation mode = model.getMode();
        setVisible(renderingHint == EntityModel.RenderingHint.REGULAR && mode == ScalarRepresentation.VIEWING);

        // disable, since currently not honoured if used as a URL (think that session hints are taking precedence).
        // (see ISIS-1660 to resurrect)
        setVisibilityAllowed(false);
    }

    private AjaxLink<Void> createLink(final String linkId) {
        return newSimpleClipboardLink(linkId);
    }

    private AjaxLink<Void> newSimpleClipboardLink(final String linkId) {
        return Wkt.link(linkId, target->{
            String contentId = simpleClipboardModalWindow.getContentId();
            SimpleClipboardModalWindowPanel panel = new SimpleClipboardModalWindowPanel(contentId);
            SimpleClipboardModalWindowForm form = new SimpleClipboardModalWindowForm("form");

            final TextField<String> textField = new TextField<String>("textField", new LoadableDetachableModel<String>() {
                private static final long serialVersionUID = 1L;

                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                protected String load() {

                    final Class pageClass = ZeroClipboardPanel.this.getPage().getPageClass();

                    final ObjectAdapterModel entityModel = ZeroClipboardPanel.this.getModel();
                    final PageParameters pageParameters = entityModel.getPageParameters();

                    final CharSequence urlFor = getRequestCycle().urlFor(pageClass, pageParameters);
                    return getRequestCycle().getUrlRenderer().renderFullUrl(Url.parse(urlFor));
                }
            });
            panel.add(form);
            form.add(textField);

            textField.setOutputMarkupId(true);

            CharSequence modalId = Strings2.escapeMarkupId(simpleClipboardModalWindow.getMarkupId());
            CharSequence textFieldId = Strings2.escapeMarkupId(textField.getMarkupId());
            target.appendJavaScript(String.format("$('#%s').one('shown.bs.modal', function(){Wicket.$('%s').select();});", modalId, textFieldId));

            simpleClipboardModalWindow.setPanel(panel, target);
            simpleClipboardModalWindow.showPrompt(target);

            target.focusComponent(textField);
        });
    }

    private void addSimpleClipboardModalWindow() {
        simpleClipboardModalWindow = SimpleClipboardModalWindow.newModalWindow(ID_SIMPLE_CLIPBOARD_MODAL_WINDOW);
        addOrReplace(simpleClipboardModalWindow);
    }

}
