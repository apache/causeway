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
package org.apache.isis.viewer.wicket.ui.components.widgets.navbar;

import javax.inject.Inject;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;

import org.apache.isis.core.config.viewer.wicket.WebAppConfiguration;

/**
 * A component used as a brand logo in the top-left corner of the navigation bar
 */
public class BrandLogo extends WebComponent {

    private static final long serialVersionUID = 1L;

    private final Placement placement;

    @Inject private WebAppConfiguration webAppConfigBean;

    /**
     * Constructor.
     *
     * @param id The component id
     * @param placement
     */
    public BrandLogo(final String id, final Placement placement) {
        super(id);
        this.placement = placement;
    }

    @Override
    protected void onComponentTag(final ComponentTag tag) {
        super.onComponentTag(tag);

        tag.put("src", url());
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(url() != null);
    }

    private String url() {
        String logoHeaderUrl = webAppConfigBean.getBrandLogoHeader();
        String logoSigninUrl = webAppConfigBean.getBrandLogoSignin();

        return placement.urlFor(logoHeaderUrl, logoSigninUrl);
    }


}
