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
package org.apache.causeway.viewer.commons.services.branding;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Common.Application;
import org.apache.causeway.viewer.commons.applib.services.branding.BrandingUiModel;
import org.apache.causeway.viewer.commons.applib.services.branding.BrandingUiService;
import org.apache.causeway.viewer.commons.services.CausewayModuleViewerCommonsServices;

@Service
@Named(CausewayModuleViewerCommonsServices.NAMESPACE + ".BrandingUiServiceDefault")
@Priority(PriorityPrecedence.LATE)
@Qualifier("Default")
public class BrandingUiServiceDefault
implements BrandingUiService {

    private final Application appConfig;

    @Inject
    public BrandingUiServiceDefault(final CausewayConfiguration causewayConfiguration) {
        this.appConfig = causewayConfiguration.viewer().common().application();
    }

    @Override
    public BrandingUiModel getHeaderBranding() {
        return BrandingUiModel.of(
                appConfig.name(),
                appConfig.brandLogoHeader().orElse(null));
    }

    @Override
    public BrandingUiModel getSignInBranding() {
        return BrandingUiModel.of(
                appConfig.name(),
                appConfig.brandLogoSignin().orElse(null));
    }

}
