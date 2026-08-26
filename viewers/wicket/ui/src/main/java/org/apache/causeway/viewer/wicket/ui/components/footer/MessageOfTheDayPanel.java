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
package org.apache.causeway.viewer.wicket.ui.components.footer;

import java.time.Instant;
import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.motd.MessageOfTheDay;
import org.apache.causeway.applib.services.motd.MessageOfTheDayProvider;
import org.apache.causeway.viewer.wicket.ui.components.widgets.bootstrap.ModalDialog;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;

import lombok.val;

/**
 * Displays the active message of the day and opens its trusted HTML detail.
 */
public class MessageOfTheDayPanel
extends PanelAbstract<String, Model<String>> {

    private static final long serialVersionUID = 1L;

    private static final String ID_TITLE_LINK = "titleLink";
    private static final String ID_TITLE = "title";
    private static final String ID_DETAIL_DIALOG = "detailDialog";

    private static final CssResourceReference CSS_RESOURCE =
            new CssResourceReference(MessageOfTheDayPanel.class, "MessageOfTheDayPanel.css");

    private MessageOfTheDay activeMessage;
    private Label titleLabel;
    private ModalDialog<Void> detailDialog;

    public MessageOfTheDayPanel(final String id) {
        super(id);
        setOutputMarkupPlaceholderTag(true);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        titleLabel = newTitleLabel(ID_TITLE, "");

        final AjaxLink<Void> titleLink = new AjaxLink<Void>(ID_TITLE_LINK) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                showDetail(target);
            }
        };
        titleLink.add(titleLabel);
        add(titleLink);

        detailDialog = new ModalDialog<>(ID_DETAIL_DIALOG);
        detailDialog.setVisible(false);
        add(detailDialog);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        val candidate = candidateFrom(getServiceRegistry()
                .lookupService(MessageOfTheDayProvider.class));
        val now = getServiceRegistry()
                .lookupServiceElseFail(ClockService.class)
                .getClock()
                .nowAsInstant();

        activeMessage = selectActive(candidate, now).orElse(null);
        titleLabel.setDefaultModelObject(activeMessage != null ? activeMessage.getTitle() : "");
        setVisible(activeMessage != null);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(CSS_RESOURCE));
        response.render(OnDomReadyHeaderItem.forScript(
                moveDialogToDocumentBodyScript(detailDialog.getMarkupId())));
    }

    static String moveDialogToDocumentBodyScript(final String markupId) {
        return String.format(
                "document.body.appendChild(document.getElementById('%s'));",
                markupId);
    }

    static Optional<MessageOfTheDay> candidateFrom(
            final Optional<MessageOfTheDayProvider> provider) {
        return provider.flatMap(MessageOfTheDayProvider::getMessageOfTheDay);
    }

    static Optional<MessageOfTheDay> selectActive(
            final Optional<MessageOfTheDay> candidate,
            final Instant now) {
        return candidate.filter(message -> message.isActiveAt(now));
    }

    static Label newTitleLabel(final String id, final String title) {
        return new Label(id, Model.of(title));
    }

    static Label newDetailLabel(final String id, final String detailHtml) {
        final Label detail = new Label(id, Model.of(detailHtml));
        detail.setEscapeModelStrings(false);
        return detail;
    }

    private void showDetail(final AjaxRequestTarget target) {
        if(activeMessage == null) {
            return;
        }

        detailDialog.setTitle(
                newTitleLabel(detailDialog.getTitleId(), activeMessage.getTitle()),
                target);
        detailDialog.setPanel(
                newDetailLabel(detailDialog.getContentId(), activeMessage.getDetailHtml()),
                target);
    }

}
