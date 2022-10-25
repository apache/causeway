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
package org.apache.causeway.viewer.wicket.viewer.wicketapp.config;

import javax.inject.Inject;

import org.apache.wicket.protocol.http.WebApplication;
import org.springframework.context.annotation.Configuration;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.wicket.model.causeway.WicketApplicationInitializer;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;
import lombok.val;

@Configuration
public class JQueryInitWkt implements WicketApplicationInitializer {

    @Inject CausewayConfiguration configuration;

    /**
     * Upgrading jquery 3.6.0 -> 3.6.1 because of:
     *
     * https://github.com/select2/select2/issues/5993
     */
    @Override
    public void init(final WebApplication webApplication) {
        val settings = webApplication.getJavaScriptLibrarySettings();
        // settings.setJQueryReference(JQueryResourceReference.getV3());
        settings.setJQueryReference(new WebjarsJavaScriptResourceReference("/webjars/jquery/3.6.1/jquery.js"));
    }

}
