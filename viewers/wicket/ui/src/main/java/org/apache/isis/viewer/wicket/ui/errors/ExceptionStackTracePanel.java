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
package org.apache.isis.viewer.wicket.ui.errors;

import javax.inject.Inject;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import org.apache.isis.applib.services.error.Ticket;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.components.scalars.markup.MarkupComponent;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModelProvider;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.home.HomePage;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.WktLinks;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

public class ExceptionStackTracePanel extends Panel {

    private static final long serialVersionUID = 1L;

    private static final String ID_MAIN_MESSAGE = "mainMessage";

    private static final String ID_EXCEPTION_DETAIL_DIV = "exceptionDetailDiv";

    private static final String ID_TICKET_MARKUP = "ticketMarkup";

    private static final String ID_STACK_TRACE_ELEMENT = "stackTraceElement";
    private static final String ID_LINE = "stackTraceElementLine";

    private static final JavaScriptResourceReference DIV_TOGGLE_JS = new JavaScriptResourceReference(ExceptionStackTracePanel.class, "div-toggle.js");


    public class ExternalImageUrl extends WebComponent {

        private static final long serialVersionUID = -3556235292216447710L;

        public ExternalImageUrl(final String id, final String imageUrl) {
            super(id);
            add(new AttributeModifier("src", new Model<>(imageUrl)));
            setVisible(!(imageUrl==null || imageUrl.equals("")));
        }

        @Override
        protected void onComponentTag(final ComponentTag tag) {
            super.onComponentTag(tag);
            checkComponentTag(tag, "img");
        }
    }

    public ExceptionStackTracePanel(final String id, final ExceptionModel exceptionModel) {
        super(id, exceptionModel);

        final Ticket ticket = exceptionModel.getTicket();
        final String mainMessage =
                ticket != null && ticket.getUserMessage() != null
                ? ticket.getUserMessage()
                        : exceptionModel.getMainMessage();

                Wkt.labelAdd(this, ID_MAIN_MESSAGE, mainMessage);

                // to avoid potential XSS attacks, no longer escape model strings
                // (risk is low but could just happen: error message being rendered might accidentally or deliberately contain rogue JavaScript)
                // label.setEscapeModelStrings(false);

                final String ticketMarkup = ticket != null ? ticket.getMarkup(): null;
                if(ticketMarkup == null) {
                    Components.permanentlyHide(this, ID_TICKET_MARKUP);
                } else {
                    add(new MarkupComponent(ID_TICKET_MARKUP, Model.of(ticket.getMarkup())));
                }

                final boolean suppressExceptionDetail =
                        exceptionModel.isAuthorizationException() ||
                        exceptionModel.isRecognized() ||
                        (ticket != null && ticket.getStackTracePolicy() == Ticket.StackTracePolicy.HIDE);
                if(suppressExceptionDetail) {
                    Components.permanentlyHide(this, ID_EXCEPTION_DETAIL_DIV);
                } else {
                    MarkupContainer container = new WebMarkupContainer(ID_EXCEPTION_DETAIL_DIV) {
                        private static final long serialVersionUID = 1L;
                        @Override
                        public void renderHead(final IHeaderResponse response) {
                            response.render(JavaScriptReferenceHeaderItem.forReference(DIV_TOGGLE_JS));
                        }
                    };
                    container.add(new StackTraceListView(ID_STACK_TRACE_ELEMENT, ExceptionStackTracePanel.ID_LINE, exceptionModel.getStackTrace()));
                    add(container);
                }

                final BreadcrumbModelProvider session = (BreadcrumbModelProvider) getSession();
                final BreadcrumbModel breadcrumbModel = session.getBreadcrumbModel();
                final EntityModel entityModel = breadcrumbModel.getMostRecentlyVisited();

                final Class<? extends Page> pageClass;
                final PageParameters pageParameters;
                if (entityModel != null) {
                    pageClass = pageClassRegistry.getPageClass(PageType.ENTITY);
                    pageParameters = entityModel.getPageParameters();
                } else {
                    pageParameters = null;
                    pageClass = HomePage.class;
                }
                final AbstractLink link = WktLinks.newBookmarkablePageLink("continueButton", pageParameters, pageClass);
                add(link);

    }

    @Inject PageClassRegistry pageClassRegistry;

}
