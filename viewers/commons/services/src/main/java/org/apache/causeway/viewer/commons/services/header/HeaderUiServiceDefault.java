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
package org.apache.causeway.viewer.commons.services.header;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.DomainServiceLayout.MenuBar;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.viewer.commons.applib.services.branding.BrandingUiService;
import org.apache.causeway.viewer.commons.applib.services.header.HeaderUiModel;
import org.apache.causeway.viewer.commons.applib.services.header.HeaderUiService;
import org.apache.causeway.viewer.commons.applib.services.menu.MenuUiService;
import org.apache.causeway.viewer.commons.applib.services.userprof.UserProfileUiService;
import org.apache.causeway.viewer.commons.services.CausewayModuleViewerCommonsServices;

import lombok.RequiredArgsConstructor;

@Service
@Named(CausewayModuleViewerCommonsServices.NAMESPACE + ".HeaderUiServiceDefault")
@Priority(PriorityPrecedence.LATE)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class HeaderUiServiceDefault
implements HeaderUiService {

    private final BrandingUiService brandingUiService;
    private final UserProfileUiService userProfileUiService;
    private final MenuUiService menuUiService;

    @Override
    public HeaderUiModel getHeader() {
        return HeaderUiModel.of(
                brandingUiService.getHeaderBranding(),
                userProfileUiService.userProfile(),
                menuUiService.getMenu(MenuBar.PRIMARY),
                menuUiService.getMenu(MenuBar.SECONDARY),
                menuUiService.getMenu(MenuBar.TERTIARY));
    }

}
