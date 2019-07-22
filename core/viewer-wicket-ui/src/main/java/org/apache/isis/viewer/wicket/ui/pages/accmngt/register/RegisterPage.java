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

package org.apache.isis.viewer.wicket.ui.pages.accmngt.register;

import javax.inject.Inject;

import org.apache.isis.applib.services.userreg.UserDetails;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.isis.viewer.wicket.ui.pages.PageNavigationService;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.AccountConfirmationMap;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.AccountManagementPageAbstract;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

/**
 * Web page representing the about page.
 */
public class RegisterPage extends AccountManagementPageAbstract {

    private static final long serialVersionUID = 1L;

    public RegisterPage(final PageParameters parameters) {
        this(parameters, getAndClearExceptionModelIfAny());
    }

    private RegisterPage(final PageParameters parameters, final ExceptionModel exceptionModel) {
        super(parameters, exceptionModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        add(new NotificationPanel("feedback"));

        final StringValue uuidValue = getPageParameters().get(0);
        if (uuidValue.isEmpty()) {
            pageNavigationService.navigateTo(PageType.SIGN_IN);
        } else {
            String uuid = uuidValue.toString();

            AccountConfirmationMap accountConfirmationMap = getApplication().getMetaData(AccountConfirmationMap.KEY);
            final String email = accountConfirmationMap.get(uuid);
            if (Strings.isEmpty(email)) {
                pageNavigationService.navigateTo(PageType.SIGN_IN);
            } else {
                UserDetails userDetails = newUserDetails();
                addOrReplace(new RegisterPanel("content", userDetails, uuidValue.toString()) {
                    @Override
                    protected MarkupContainer newExtraFieldsContainer(String id) {
                        return RegisterPage.this.newExtraFieldsContainer(id);
                    }
                });
            }
        }
    }

    protected UserDetails newUserDetails() {
        return new UserDetails();
    }

    protected MarkupContainer newExtraFieldsContainer(final String id) {
        return new WebMarkupContainer(id);
    }

    @Inject private PageNavigationService pageNavigationService;
}
