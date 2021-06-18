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
package org.apache.isis.viewer.common.model.userprofile;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.viewer.common.applib.services.userprof.UserProfileUiModelProvider;
import org.apache.isis.viewer.common.applib.services.userprof.UserProfileUiModel;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named("isis.viewer.common.UserProfileServiceDefault")
@javax.annotation.Priority(PriorityPrecedence.LATE)
@Primary
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class UserProfileUiModelProviderDefault implements UserProfileUiModelProvider {

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

    private String userNameFor(UserMemento x) {
        final String username = x.getName();
        if (x.isImpersonating()) {
            return String.format("%s (%s)", username, translated("impersonating"));
        }
        val realName = x.getRealName();
        return !isNullOrEmpty(realName)
                ? realName
                : username;
    }

    private String translated(String str) {
        return translationService.translate(TranslationContext.forClassName(getClass()), str);
    }

    private static boolean isNullOrEmpty(String realName) {
        return realName == null || realName.equals("");
    }

}
