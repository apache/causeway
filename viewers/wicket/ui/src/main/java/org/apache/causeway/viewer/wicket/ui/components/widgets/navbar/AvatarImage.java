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
package org.apache.causeway.viewer.wicket.ui.components.widgets.navbar;

import org.apache.wicket.markup.ComponentTag;

import org.apache.causeway.viewer.commons.applib.services.userprof.UserProfileUiModel;
import org.apache.causeway.viewer.wicket.ui.components.WebComponentBase;

/**
 * A component used as a brand logo in the top-left corner of the navigation bar
 */
public class AvatarImage extends WebComponentBase {

    private static final long serialVersionUID = 1L;

    private final UserProfileUiModel userProfile;

    /**
     * Constructor.
     *
     * @param id - The component id
     * @param userProfile
     */
    public AvatarImage(final String id, final UserProfileUiModel userProfile) {
        super(id);
        this.userProfile = userProfile;
    }

    @Override
    protected void onComponentTag(final ComponentTag tag) {
        super.onComponentTag(tag);

        userProfile.avatarUrl()
                .ifPresent(url -> tag.put("src", url.toExternalForm()));
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(userProfile.avatarUrl().isPresent());
    }

}
