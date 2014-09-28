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

package org.apache.isis.viewer.wicket.ui.pages.login;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

import org.apache.wicket.authroles.authentication.panel.SignInPanel;

/**
 * An extension of Wicket's default SignInPanel that provides
 * custom markup, based on Bootstrap, and uses
 * {@link de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel}
 * to Bootstrap styled error messages
 */
public class IsisSignInPanel extends SignInPanel {

    private final boolean clearOriginalDestination;

    /**
     * Constructor
     *
     * @param id
     *            the component id
     * @param rememberMe
     *            True if form should include a remember-me checkbox
     * @param continueToOriginalDestination
     *            A flag indicating whether to continue to the originally requested destination
     */
    public IsisSignInPanel(
            final String id,
            final boolean rememberMe,
            final boolean continueToOriginalDestination) {
        super(id, rememberMe);
        this.clearOriginalDestination = !continueToOriginalDestination;

        addOrReplace(new NotificationPanel("feedback"));
    }

    @Override
    protected void onSignInSucceeded() {

        if(clearOriginalDestination) {
            clearOriginalDestination();
        }
        super.onSignInSucceeded();
    }

    @Override
    protected void onSignInRemembered() {
        if(clearOriginalDestination) {
            clearOriginalDestination();
        }
        super.onSignInRemembered();
    }
}
