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
package org.apache.causeway.viewer.commons.applib.services.userprof;

import java.io.Serializable;
import java.net.URL;
import java.util.Optional;

import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.applib.services.user.UserService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the currently logged in user (or, possibly, the user being
 * impersonated).
 *
 * @since 2.0 {@index}
 */
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class UserProfileUiModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Returns an alternate name for the current user, typically for use on the
     * viewer's menu bar to identify the currently logged in user.
     *
     * <p>
     * For example, In the Wicket viewer, used as the menu name of the
     * {@link org.apache.causeway.applib.annotation.DomainServiceLayout.MenuBar#TERTIARY tertiary}
     * &quot;Me&quot; menu bar.
     * </p>
     *
     * <p>
     *     If returns <tt>null</tt>, then the current user name is used instead.
     * </p>
     */
    private String userProfileName;

    /**
     * The {@link UserMemento}, as provided by {@link UserService#currentUser()}.
     *
     * <p>
     *     Will return null if anonymous.
     * </p>
     */
    private UserMemento userMemento;

    public Optional<UserMemento> userMemento() {
        return Optional.ofNullable(getUserMemento());
    }
    public Optional<URL> avatarUrl() {
        return userMemento().map(UserMemento::getAvatarUrl);
    }
}
