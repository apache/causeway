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
package org.apache.isis.viewer.wicket.viewer.wicketapp.config;

import org.apache.wicket.protocol.http.WebApplication;
import org.springframework.context.annotation.Configuration;

import org.apache.isis.viewer.wicket.model.isis.WicketApplicationInitializer;
import org.apache.isis.viewer.wicket.viewer.IsisModuleViewerWicketViewer;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WicketViewerXray {

    /**
     * Activates visual debugging mode for the Wicket Viewer.
     * Use for troubleshooting and bug hunting.
     * <p>
     * Not imported by {@link IsisModuleViewerWicketViewer}.
     */
    @Configuration
    public static class Enable
        implements WicketApplicationInitializer {

        @Override
        public void init(final WebApplication webApplication) {
            WicketViewerXray.enabled = true;
            webApplication.getDebugSettings()
                .setOutputMarkupContainerClassName(true);
        }
    }

    @Getter
    private boolean enabled = false;

}
