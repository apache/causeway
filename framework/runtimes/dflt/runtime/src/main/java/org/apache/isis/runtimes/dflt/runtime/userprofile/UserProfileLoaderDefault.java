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

package org.apache.isis.runtimes.dflt.runtime.userprofile;

import java.util.List;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.userprofile.UserProfile;
import org.apache.isis.core.runtime.userprofile.UserProfileLoader;
import org.apache.isis.core.runtime.userprofile.UserProfileStore;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSession;

/**
 * Acts like a bridge, loading the profile from the underlying store.
 */
public class UserProfileLoaderDefault implements UserProfileLoader, DebuggableWithTitle {

    private static final String DEFAULT_PERSPECTIVE_NAME = "Apache Isis";
    private static final String EXPLORATION = " Exploration";

    private final Logger LOG = Logger.getLogger(UserProfile.class);

    public static enum Mode {
        /**
         * Must provide some services.
         */
        STRICT,
        /**
         * For testing only, no services is okay.
         */
        RELAXED
    }

    private final UserProfileStore store;
    private final Mode mode;

    private UserProfile userProfile;

    private List<Object> serviceList;

    // //////////////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////////////

    public UserProfileLoaderDefault(final UserProfileStore store) {
        this(store, Mode.STRICT);
    }

    /**
     * for testing purposes, explicitly specify the Mode.
     */
    public UserProfileLoaderDefault(final UserProfileStore store, final Mode mode) {
        this.store = store;
        this.mode = mode;
    }

    // //////////////////////////////////////////////////////
    // init, shutdown
    // //////////////////////////////////////////////////////

    /**
     * Does nothing.
     */
    @Override
    public void init() {
    }

    /**
     * Does nothing.
     */
    @Override
    public void shutdown() {
    }

    // //////////////////////////////////////////////////////
    // Fixtures
    // //////////////////////////////////////////////////////

    /**
     * @see PersistenceSession#isFixturesInstalled()
     */
    @Override
    public boolean isFixturesInstalled() {
        return store.isFixturesInstalled();
    }

    // //////////////////////////////////////////////////////
    // saveAs...
    // //////////////////////////////////////////////////////

    @Override
    public void saveAsDefault(final UserProfile userProfile) {
        store.save("_default", userProfile);
    }

    @Override
    public void saveForUser(final String userName, final UserProfile userProfile) {
        store.save(userName, userProfile);
    }

    // //////////////////////////////////////////////////////
    // saveSession
    // //////////////////////////////////////////////////////

    @Override
    public void saveSession(final List<ObjectAdapter> objects) {
        loadOrCreateProfile();
        userProfile.saveObjects(objects);
        save(userProfile);
    }

    private void save(final UserProfile userProfile) {
        saveForUser(userName(), userProfile);
    }

    // //////////////////////////////////////////////////////
    // getProfile
    // //////////////////////////////////////////////////////

    @Override
    public UserProfile getProfile(final AuthenticationSession session) {
        final String userName = session.getUserName();
        final UserProfile profile = store.getUserProfile(userName);
        userProfile = profile != null ? profile : createUserProfile(userName);
        return userProfile;
    }

    @Override
    @Deprecated
    public UserProfile getProfile() {
        loadOrCreateProfile();
        return userProfile;
    }

    // //////////////////////////////////////////////////////
    // Helpers: (for getProfile)
    // //////////////////////////////////////////////////////

    private void loadOrCreateProfile() {
        if (userProfile == null) {
            final String userName = userName();
            final UserProfile profile = store.getUserProfile(userName);
            userProfile = profile != null ? profile : createUserProfile(userName);
        }
    }

    private UserProfile createUserProfile(final String userName) {
        final UserProfile template = store.getUserProfile("_default");
        if (template == null) {
            return createDefaultProfile(userName);
        } else {
            return createProfileFromTemplate(userName, template);
        }
    }

    private UserProfile createDefaultProfile(final String userName) {
        final UserProfile profile = new UserProfile();
        profile.newPerspective(DEFAULT_PERSPECTIVE_NAME + (IsisContext.getDeploymentType().isExploring() ? EXPLORATION : ""));

        final List<Object> services = getServices();
        if (services.size() == 0 && mode == Mode.STRICT) {
            throw new IsisException("No known services");
        }
        for (final Object service : services) {
            profile.getPerspective().addToServices(service);
        }
        LOG.debug("creating exploration UserProfile for " + userName);
        return profile;
    }

    private UserProfile createProfileFromTemplate(final String userName, final UserProfile template) {
        final UserProfile userProfile = new UserProfile();
        userProfile.copy(template);
        LOG.info("creating UserProfile, from template, for " + userName);
        return userProfile;
    }

    // //////////////////////////////////////////////////////
    // Debugging
    // //////////////////////////////////////////////////////

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendln("Store", store.toString());
        debug.appendln("Mode", mode);

        debug.append(store);
        debug.append(userProfile);
    }

    @Override
    public String debugTitle() {
        return "User Profile Service";
    }

    // //////////////////////////////////////////////////////
    // Dependencies (injected via setters)
    // //////////////////////////////////////////////////////

    @Override
    public List<Object> getServices() {
        return serviceList;
    }

    @Override
    public void setServices(final List<Object> serviceList) {
        this.serviceList = serviceList;
    }

    // //////////////////////////////////////////////////////
    // Dependencies (from context)
    // //////////////////////////////////////////////////////

    private static AuthenticationSession getAuthenticationSession() {
        return getSession().getAuthenticationSession();
    }

    private static String userName() {
        return getAuthenticationSession().getUserName();
    }

    private static IsisSession getSession() {
        return IsisContext.getSession();
    }

}
