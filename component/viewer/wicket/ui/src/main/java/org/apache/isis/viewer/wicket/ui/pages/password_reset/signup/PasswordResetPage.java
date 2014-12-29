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

package org.apache.isis.viewer.wicket.ui.pages.password_reset.signup;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.isis.viewer.wicket.ui.pages.AccountManagementPageAbstract;

/**
 * Boilerplate, pick up our HTML and CSS.
 */
public class PasswordResetPage extends AccountManagementPageAbstract {
    
    private static final long serialVersionUID = 1L;

    public PasswordResetPage(final PageParameters parameters) {
        this(parameters, getAndClearExceptionModelIfAny());
    }

    public PasswordResetPage(final PageParameters parameters, ExceptionModel exceptionModel) {
        super(parameters, exceptionModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        addPasswordResetPanel();
    }

    protected PasswordResetPanel addPasswordResetPanel() {
        final PasswordResetPanel passwordResetPanel = new PasswordResetPanel("passwordResetPanel");
        addOrReplace(passwordResetPanel);
        return passwordResetPanel;
    }
}
