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


package org.apache.isis.runtime.fixturesinstaller;

import org.apache.isis.applib.profiles.Perspective;
import org.apache.isis.applib.profiles.Profile;
import org.apache.isis.applib.profiles.ProfileService;
import org.apache.isis.applib.profiles.ProfileServiceAware;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.userprofile.PerspectiveEntry;
import org.apache.isis.runtime.userprofile.UserProfile;
import org.apache.isis.runtime.userprofile.UserProfileLoader;

public class ProfileServiceImpl implements ProfileService {

	public Profile newUserProfile() {
		return new ProfileImpl();
	}

	public Profile newUserProfile(Profile profileTemplate) {
		return new ProfileImpl((ProfileImpl) profileTemplate);
	}

	public void saveAsDefault(Profile profile) {
		getUserProfileLoader().saveAsDefault(createUserProfile(profile));
	}

	public void saveForUser(String name, Profile profile) {
		getUserProfileLoader().saveForUser(name, createUserProfile(profile));
	}

	private UserProfile createUserProfile(Profile profile) {
		return ((ProfileImpl) profile).getUserProfile();
	}

	public void injectInto(Object fixture) {
		if (fixture instanceof ProfileServiceAware) {
			ProfileServiceAware serviceAware = (ProfileServiceAware) fixture;
			serviceAware.setService(this);
		}
	}

	private static UserProfileLoader getUserProfileLoader() {
		return IsisContext.getUserProfileLoader();
	}

}

class ProfileImpl implements Profile {
	private final UserProfile userProfile;

	public ProfileImpl(ProfileImpl profileTemplate) {
		this();
		userProfile.copy(profileTemplate.userProfile);
	}

	public ProfileImpl() {
		userProfile = new UserProfile();
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}

	public void addToOptions(String name, String value) {
		userProfile.addToOptions(name, value);
	}

	public void addToPerspectives(Perspective perspective) {
		userProfile.addToPerspectives(((PerspectiveImpl) perspective)
				.getPerspectiveEntry());
	}

	public Perspective getPerspective(String name) {
		PerspectiveEntry perspectiveEntry = userProfile.getPerspective(name);
		if (perspectiveEntry == null) {
			throw new IsisException("No perspective found for "
					+ name);
		}
		return new PerspectiveImpl(perspectiveEntry);
	}

	public Perspective newPerspective(String name) {
		PerspectiveEntry perspectiveEntry = userProfile.newPerspective(name);
		return new PerspectiveImpl(perspectiveEntry);
	}

}

class PerspectiveImpl implements Perspective {
	private final PerspectiveEntry entry;

	public PerspectiveImpl(PerspectiveEntry perspectiveEntry) {
		entry = perspectiveEntry;
	}

	public PerspectiveEntry getPerspectiveEntry() {
		return entry;
	}

	public void addGenericRepository(Class<?>... classes) {
		for (Class<?> cls : classes) {
			entry.addGenericRepository(cls);
		}
	}

	public void addToObjects(Object... objects) {
		for (Object object : objects) {
			entry.addToObjects(object);
		}
	}

	public Object addToServices(Class<?> cls) {
		return entry.addToServices(cls);
	}

	public void addToServices(Class<?>... classes) {
		for (Class<?> cls : classes) {
			entry.addToServices(cls);
		}
	}

	public void removeFromServices(Class<?>... classes) {
		for (Class<?> cls : classes) {
			entry.removeFromServices(cls);
		}
	}

}

