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

import org.apache.wicket.markup.ComponentTag;

import org.apache.isis.viewer.common.model.branding.BrandingUiModel;
import org.apache.isis.viewer.wicket.ui.components.WebComponentBase;

/**
 * A component used as a brand logo in the top-left corner of the navigation bar
 */
public class BrandLogo extends WebComponentBase {

    private static final long serialVersionUID = 1L;

    private final BrandingUiModel branding;

    /**
     * Constructor.
     *
     * @param id - The component id
     * @param branding
     */
    public BrandLogo(final String id, final BrandingUiModel branding) {
        super(id);
        this.branding = branding;
    }

    @Override
    protected void onComponentTag(final ComponentTag tag) {
        super.onComponentTag(tag);

        branding.getLogoHref()
            .map(super.getWebAppContextPath()::prependContextPathIfLocal)
            .ifPresent(url -> tag.put("src", url));
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(branding.getLogoHref().isPresent());
    }


}
