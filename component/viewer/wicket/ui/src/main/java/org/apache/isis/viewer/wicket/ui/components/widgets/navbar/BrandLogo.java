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

import com.google.inject.name.Named;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;

/**
 * A component used as a brand logo in the top-left corner of the navigation bar
 */
public class BrandLogo extends WebComponent {

    private final Placement placement;

    @com.google.inject.Inject(optional = true)
    @Named("brandLogoHeader")
    private String logoHeaderUrl;

    @com.google.inject.Inject(optional = true)
    @Named("brandLogoSignin")
    private String logoSigninUrl;

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
        return placement.urlFor(logoHeaderUrl, logoSigninUrl);
    }


}
