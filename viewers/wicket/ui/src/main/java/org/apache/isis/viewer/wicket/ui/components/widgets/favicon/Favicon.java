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
package org.apache.isis.viewer.wicket.ui.components.widgets.favicon;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.util.string.Strings;

import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.viewer.wicket.WebAppContextPath;

/**
 * A component for application favorite icon
 */
public class Favicon extends WebComponent {

    private static final long serialVersionUID = 1L;

    @Inject private transient IsisConfiguration isisConfiguration;
    @Inject private transient WebAppContextPath webAppContextPath;

    private String url = null;
    private String contentType = null;

    public Favicon(String id) {
        super(id);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        if(webAppContextPath != null && isisConfiguration != null) {

            url = isisConfiguration.getViewer().getWicket().getApplication().getFaviconUrl()
                    .filter(x -> !Strings.isEmpty(x))
                    .map(webAppContextPath::prependContextPathIfLocal)
                    .orElse(null);

            contentType = isisConfiguration.getViewer().getWicket().getApplication().getFaviconContentType()
                    .filter(x -> !Strings.isEmpty(x))
                    .orElse(null);;
        }

        setVisible(url != null);
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);

        if(url != null) {
            tag.put("href", url);
        }
        if(contentType != null) {
            tag.put("type", contentType);
        }

    }
}
