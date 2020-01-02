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
package org.apache.isis.viewer.wicket.ui.components.footer;

import javax.inject.Inject;

import org.apache.isis.commons.internal.environment.IsisSystemEnvironment;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;

import static org.apache.isis.commons.internal.environment.IsisSystemEnvironment.prependContextPathIfRequired;

public class CreditImage extends WebComponent {

    private static final long serialVersionUID = 1L;

    private String imageUrl;
    @Inject
    private IsisSystemEnvironment isisSystemEnvironment;

    public CreditImage(final String id, final String imageUrl) {
        super(id);
        this.imageUrl = imageUrl;
    }

    @Override
    protected void onComponentTag(final ComponentTag tag) {
        super.onComponentTag(tag);
        tag.put("src", imageUrl);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        this.imageUrl = isisSystemEnvironment.prependContextPathIfRequired(imageUrl);
        setVisible(imageUrl != null);
    }

}
