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

package org.apache.isis.viewer.wicket.ui.pages.mmverror;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.beans.WebAppConfigBean;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.session.IsisSessionFactory;

import de.agilecoders.wicket.core.markup.html.references.BootstrapJavaScriptReference;

/**
 * A page being shown when the meta model validation has failed
 */
public class MmvErrorPage extends WebPage {

    private static final long serialVersionUID = 1L;

    private static final String ID_PAGE_TITLE = "pageTitle";
    private static final String ID_APPLICATION_NAME = "applicationName";
    private static final String ID_ERROR = "error";
    private static final String ID_ERROR_MESSAGE = "errorMessage";

    public MmvErrorPage(Set<String> validationErrors) {
        this(Model.ofList(_Lists.newArrayList(validationErrors)));
    }

    public MmvErrorPage(final IModel<List<String>> model) {
        super(model);
        addPageTitle();
        addApplicationName();
        addValidationErrors();
    }

    @SuppressWarnings("unchecked")
    private IModel<List<String>> getModel() {
        return (IModel<List<String>>) getDefaultModel();
    }

    private MarkupContainer addPageTitle() {
        return add(new Label(ID_PAGE_TITLE, webAppConfigBean.getApplicationName()));
    }

    private void addApplicationName() {
        add(new Label(ID_APPLICATION_NAME, webAppConfigBean.getApplicationName()));
    }

    private void addValidationErrors() {
        ListView<String> validationErrorsView = new ListView<String>(ID_ERROR, getModel()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<String> item) {
                final String validationError = item.getModelObject();
                item.add(new Label(ID_ERROR_MESSAGE, validationError));
            }
        };
        add(validationErrorsView);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference())));
        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(BootstrapJavaScriptReference.instance())));

        String applicationCss = webAppConfigBean.getApplicationCss();
        if(applicationCss != null) {
            response.render(CssReferenceHeaderItem.forUrl(applicationCss));
        }
        String applicationJs = webAppConfigBean.getApplicationJs();
        if(applicationJs != null) {
            response.render(JavaScriptReferenceHeaderItem.forUrl(applicationJs));
        }
    }

    @Override
    public boolean isErrorPage() {
        return true;
    }

    // ///////////////////////////////////////////////////
    // System components
    // ///////////////////////////////////////////////////

    protected IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

    IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

    @Inject private WebAppConfigBean webAppConfigBean;

}
