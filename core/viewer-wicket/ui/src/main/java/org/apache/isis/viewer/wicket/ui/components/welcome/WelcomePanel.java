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

package org.apache.isis.viewer.wicket.ui.components.welcome;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.isis.viewer.wicket.model.models.WelcomeModel;
import org.apache.isis.viewer.wicket.ui.pages.home.HomePage;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * {@link PanelAbstract Panel} displaying welcome message (as used on
 * {@link HomePage}).
 */
public class WelcomePanel extends PanelAbstract<WelcomeModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_MESSAGE = "message";

    @Inject
    @Named("welcomeMessage")
    private String welcomeMessage;

    public WelcomePanel(final String id, final WelcomeModel model) {
        super(id, model);
        model.setObject(welcomeMessage);
        final Label label = new Label(ID_MESSAGE, welcomeMessage);
        // safe to not escape, welcome message is read from file (part of deployed WAR)
        label.setEscapeModelStrings(false);
        add(label);
    }
}
