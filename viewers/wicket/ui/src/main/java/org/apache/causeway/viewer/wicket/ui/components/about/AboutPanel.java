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
package org.apache.causeway.viewer.wicket.ui.components.about;

import java.io.InputStream;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.servlet.ServletContext;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.wicket.model.models.AboutModel;
import org.apache.causeway.viewer.wicket.ui.pages.home.HomePage;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;

/**
 * {@link PanelAbstract Panel} displaying welcome message (as used on
 * {@link HomePage}).
 */
class AboutPanel
extends PanelAbstract<CausewayConfiguration.Viewer.Common.Application, AboutModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_MANIFEST_ATTRIBUTES = "manifestAttributes";
    private static final String ID_APPLICATION_NAME = "applicationName";
    private static final String ID_APPLICATION_VERSION = "applicationVersion";
    private static final String ID_ABOUT_MESSAGE = "aboutMessage";

    public static class LabelVisibleOnlyIfNonEmpty extends Label {

        private static final long serialVersionUID = 1L;
        private final IModel<String> label;

        public LabelVisibleOnlyIfNonEmpty(final String id, final IModel<String> label) {
            super(id, label);
            this.label = label;
        }

        @Override protected void onConfigure() {
            super.onConfigure();
            setVisibilityAllowed(label != null && !_NullSafe.isEmpty(label.getObject()));
        }
    }

    @Inject
    private ServletContext servletContext;

    private JarManifestModel jarManifestModel;

    public AboutPanel(final String id, final AboutModel aboutModel) {
        super(id);

        add(new LabelVisibleOnlyIfNonEmpty(ID_APPLICATION_NAME, LambdaModel.of(()->aboutModel.getObject().getName())));
        add(new LabelVisibleOnlyIfNonEmpty(ID_APPLICATION_VERSION, LambdaModel.of(()->aboutModel.getObject().getVersion())));
        add(new LabelVisibleOnlyIfNonEmpty(ID_ABOUT_MESSAGE, LambdaModel.of(()->aboutModel.getObject().getAbout())));

        if(jarManifestModel == null) {
            Provider<InputStream> metaInfManifestProvider =
                    () -> servletContext.getResourceAsStream("/META-INF/MANIFEST.MF");
            jarManifestModel = JarManifestModel.of(metaInfManifestProvider);
        }

        add(new JarManifestPanel(ID_MANIFEST_ATTRIBUTES, jarManifestModel));
    }

}
