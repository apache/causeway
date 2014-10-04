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

package org.apache.isis.core.profilestore;

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.runtime.userprofile.UserProfile;
import org.apache.isis.core.runtime.userprofile.UserProfileStore;

public class InMemoryUserProfileStore implements UserProfileStore, DebuggableWithTitle {

    private static final Map<String, UserProfile> profiles = new HashMap<String, UserProfile>();

    @Override
    public boolean isFixturesInstalled() {
        return false;
    }

    @Override
    public UserProfile getUserProfile(final String name) {
        return profiles.get(name);
    }

    @Override
    public void save(final String name, final UserProfile userProfile) {
        profiles.put(name, userProfile);
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        for (final String name : profiles.keySet()) {
            debug.appendln(name, profiles.get(name));
        }
    }

    @Override
    public String debugTitle() {
        return "InMemoryUserProfileStore";
    }

}
