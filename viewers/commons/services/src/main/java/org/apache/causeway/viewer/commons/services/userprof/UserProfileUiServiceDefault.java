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
package org.apache.causeway.viewer.commons.services.userprof;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.viewer.commons.applib.services.userprof.UserProfileUiModel;
import org.apache.causeway.viewer.commons.applib.services.userprof.UserProfileUiService;
import org.apache.causeway.viewer.commons.services.CausewayModuleViewerCommonsServices;

import lombok.RequiredArgsConstructor;

/**
 * Default implementation of {@link UserProfileUiService}
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleViewerCommonsServices.NAMESPACE + ".UserProfileUiServiceDefault")
@Priority(PriorityPrecedence.LATE)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class UserProfileUiServiceDefault implements UserProfileUiService {

    private final UserService userService;
    private final TranslationService translationService;

    @Override
    public UserProfileUiModel userProfile() {
        return UserProfileUiModel.of(userProfileName(), userService.currentUser().orElse(null));
    }

    private String userProfileName() {
        return userService.currentUser()
                .map(this::userNameFor)
                .orElse(String.format("<%s>", translated("Anonymous")));
    }

    private String userNameFor(final UserMemento user) {
        if (user.isImpersonating()) {
            return user.nameFormatted();
        }
        var realName = user.getRealName();
        return _Strings.isNullOrEmpty(realName)
                ? user.getName()
                : realName;
    }

    private String translated(final String str) {
        return translationService.translate(TranslationContext.forClassName(getClass()), str);
    }

}
