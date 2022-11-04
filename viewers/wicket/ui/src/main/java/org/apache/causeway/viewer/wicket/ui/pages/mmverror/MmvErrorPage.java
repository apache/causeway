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
package org.apache.causeway.viewer.wicket.ui.pages.mmverror;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.viewer.wicket.ui.pages.WebPageBase;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import de.agilecoders.wicket.core.markup.html.references.BootstrapJavaScriptReference;

/**
 * A page being shown when the meta model validation has failed
 */
public class MmvErrorPage extends WebPageBase {

    private static final long serialVersionUID = 1L;

    private static final String ID_PAGE_TITLE = "pageTitle";
    private static final String ID_APPLICATION_NAME = "applicationName";
    private static final String ID_ERROR = "error";
    private static final String ID_ERROR_MESSAGE = "errorMessage";

    public MmvErrorPage(final Collection<String> validationErrors) {
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

    private void addPageTitle() {
        Wkt.labelAdd(this, ID_PAGE_TITLE,
                getApplicationSettings().getName());
    }

    private void addApplicationName() {
        Wkt.labelAdd(this, ID_APPLICATION_NAME,
                getApplicationSettings().getName());
    }

    private void addValidationErrors() {
        Wkt.listViewAdd(this, ID_ERROR, getModel(), item->{
            final String validationError = item.getModelObject();
            Wkt.labelAdd(item, ID_ERROR_MESSAGE, validationError);
        });
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference())));
        response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(BootstrapJavaScriptReference.instance())));

        getWicketViewerSettings().getCss()
        .ifPresent(css -> response.render(CssReferenceHeaderItem.forUrl(css)));

        getWicketViewerSettings().getJs()
        .ifPresent(js -> response.render(JavaScriptReferenceHeaderItem.forUrl(js)));
    }

    @Override
    public boolean isErrorPage() {
        return true;
    }


}
