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

package org.apache.isis.runtimes.dflt.runtime.fixtures;

import org.apache.isis.applib.fixtures.userprofile.UserProfileService;
import org.apache.isis.applib.fixtures.userprofile.UserProfileServiceAware;
import org.apache.isis.applib.profiles.Perspective;
import org.apache.isis.applib.profiles.Profile;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.runtime.userprofile.PerspectiveEntry;
import org.apache.isis.core.runtime.userprofile.UserProfile;
import org.apache.isis.core.runtime.userprofile.UserProfileLoader;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

public class ProfileServiceImpl implements UserProfileService {

    @Override
    public Profile newUserProfile() {
        return new ProfileImpl();
    }

    @Override
    public Profile newUserProfile(final Profile profileTemplate) {
        return new ProfileImpl((ProfileImpl) profileTemplate);
    }

    @Override
    public void saveAsDefault(final Profile profile) {
        getUserProfileLoader().saveAsDefault(createUserProfile(profile));
    }

    @Override
    public void saveForUser(final String name, final Profile profile) {
        getUserProfileLoader().saveForUser(name, createUserProfile(profile));
    }

    private UserProfile createUserProfile(final Profile profile) {
        return ((ProfileImpl) profile).getUserProfile();
    }

    public void injectInto(final Object fixture) {
        if (fixture instanceof UserProfileServiceAware) {
            final UserProfileServiceAware serviceAware = (UserProfileServiceAware) fixture;
            serviceAware.setService(this);
        }
    }

    private static UserProfileLoader getUserProfileLoader() {
        return IsisContext.getUserProfileLoader();
    }

}

class ProfileImpl implements Profile {
    private final UserProfile userProfile;

    public ProfileImpl(final ProfileImpl profileTemplate) {
        this();
        userProfile.copy(profileTemplate.userProfile);
    }

    public ProfileImpl() {
        userProfile = new UserProfile();
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    @Override
    public void addToOptions(final String name, final String value) {
        userProfile.addToOptions(name, value);
    }

    @Override
    public void addToPerspectives(final Perspective perspective) {
        userProfile.addToPerspectives(((PerspectiveImpl) perspective).getPerspectiveEntry());
    }

    @Override
    public Perspective getPerspective(final String name) {
        final PerspectiveEntry perspectiveEntry = userProfile.getPerspective(name);
        if (perspectiveEntry == null) {
            throw new IsisException("No perspective found for " + name);
        }
        return new PerspectiveImpl(perspectiveEntry);
    }

    @Override
    public Perspective newPerspective(final String name) {
        final PerspectiveEntry perspectiveEntry = userProfile.newPerspective(name);
        return new PerspectiveImpl(perspectiveEntry);
    }

}

class PerspectiveImpl implements Perspective {
    private final PerspectiveEntry entry;

    public PerspectiveImpl(final PerspectiveEntry perspectiveEntry) {
        entry = perspectiveEntry;
    }

    public PerspectiveEntry getPerspectiveEntry() {
        return entry;
    }

    @Override
    public void addGenericRepository(final Class<?>... classes) {
        for (final Class<?> cls : classes) {
            final Object service = getPersistenceSession().getService("repository#" + cls.getName()).getObject();
            entry.addToServices(service);
        }
    }

    @Override
    public void addToObjects(final Object... objects) {
        for (final Object object : objects) {
            entry.addToObjects(object);
        }
    }

    @Override
    public Object addToServices(final Class<?> serviceType) {
        final Object service = findService(serviceType);
        entry.addToServices(service);
        return service;
    }

    @Override
    public void removeFromServices(final Class<?> serviceType) {
        final Object service = findService(serviceType);
        entry.removeFromServices(service);
    }

    private Object findService(final Class<?> serviceType) {
        for (final Object service : IsisContext.getServices()) {
            if (service.getClass().isAssignableFrom(serviceType)) {
                return service;
            }
        }
        throw new IsisException("No service of type " + serviceType.getName());
    }

    @Override
    public void addToServices(final Class<?>... classes) {
        for (final Class<?> cls : classes) {
            addToServices(cls);
        }
    }

    @Override
    public void removeFromServices(final Class<?>... classes) {
        for (final Class<?> cls : classes) {
            removeFromServices(cls);
        }
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

}
