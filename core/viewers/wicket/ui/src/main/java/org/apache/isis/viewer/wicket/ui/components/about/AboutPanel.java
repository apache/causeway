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

package org.apache.isis.viewer.wicket.ui.components.about;

import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.wicket.markup.html.basic.Label;

import org.apache.isis.viewer.wicket.model.models.AboutModel;
import org.apache.isis.viewer.wicket.ui.pages.home.HomePage;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import lombok.val;

/**
 * {@link PanelAbstract Panel} displaying welcome message (as used on
 * {@link HomePage}).
 */
public class AboutPanel extends PanelAbstract<AboutModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_MANIFEST_ATTRIBUTES = "manifestAttributes";
    private static final String ID_APPLICATION_NAME = "applicationName";
    private static final String ID_APPLICATION_VERSION = "applicationVersion";
    private static final String ID_ABOUT_MESSAGE = "aboutMessage";

    public static class LabelVisibleOnlyIfNonEmpty extends Label {

        private static final long serialVersionUID = 1L;
        private final String label;

        public LabelVisibleOnlyIfNonEmpty(final String id, final String label) {
            super(id, label);
            this.label = label;
        }

        @Override protected void onConfigure() {
            super.onConfigure();
            setVisibilityAllowed(label != null && !label.isEmpty());
        }
    }

    /**
     * We take care to read this only once.
     *
     * <p>
     *     Is <code>transient</code> because
     * </p>
     */
    @Inject
    @Named("metaInfManifest")
    private transient InputStream metaInfManifestIs;

    private JarManifestModel jarManifestModel;

    public AboutPanel(final String id) {
        super(id);

        val webAppConfigBean = super.getWebAppConfigBean();
        
        add(new LabelVisibleOnlyIfNonEmpty(ID_APPLICATION_NAME, webAppConfigBean.getApplicationName()));
        add(new LabelVisibleOnlyIfNonEmpty(ID_APPLICATION_VERSION, webAppConfigBean.getApplicationVersion()));
        add(new LabelVisibleOnlyIfNonEmpty(ID_ABOUT_MESSAGE, webAppConfigBean.getAboutMessage()));

        if(jarManifestModel == null) {
            jarManifestModel = new JarManifestModel(super.getCommonContext(),  metaInfManifestIs);
        }

        add(new JarManifestPanel(ID_MANIFEST_ATTRIBUTES, jarManifestModel));
    }


}
