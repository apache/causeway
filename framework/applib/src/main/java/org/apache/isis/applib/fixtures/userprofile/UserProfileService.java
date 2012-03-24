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

package org.apache.isis.applib.fixtures.userprofile;

import org.apache.isis.applib.fixtures.UserProfileFixture;
import org.apache.isis.applib.fixtures.switchuser.SwitchUserService;
import org.apache.isis.applib.profiles.Perspective;
import org.apache.isis.applib.profiles.Profile;

/**
 * Not intended to be used directly; decouples the {@link UserProfileFixture},
 * which needs to persist {@link Perspective}s, from the rest of the framework's
 * runtime.
 * 
 * <p>
 * A suitable implementation is injected into {@link UserProfileFixture} when
 * installed.
 * 
 * @see SwitchUserService
 */
public interface UserProfileService {

    Profile newUserProfile();

    Profile newUserProfile(Profile profile);

    void saveForUser(String name, Profile profile);

    void saveAsDefault(Profile profile);

}
