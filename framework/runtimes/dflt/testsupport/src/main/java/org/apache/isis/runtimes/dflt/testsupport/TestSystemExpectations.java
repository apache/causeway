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

package org.apache.isis.runtimes.dflt.testsupport;

import org.jmock.Expectations;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.runtime.userprofile.UserProfile;
import org.apache.isis.core.runtime.userprofile.UserProfileStore;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;

public class TestSystemExpectations {

    public static void allowingPersistenceSessionFactoryToCreatePersistenceSession(final JUnitRuleMockery2 context, final PersistenceSessionFactory mockPersistenceSessionFactory, final PersistenceSession mockPersistenceSession) {
        context.checking(new Expectations() {
            {
                allowing(mockPersistenceSessionFactory).createPersistenceSession();
                will(returnValue(mockPersistenceSession));
            }
        });
    }
    
    public static void allowingUserProfileLoaderToCreateUserProfile(final JUnitRuleMockery2 context, final UserProfileStore mockProfileStore, final UserProfile mockProfile) {
        context.checking(new Expectations() {
            {
                allowing(mockProfileStore).getUserProfile(with(any(String.class)));
                will(returnValue(mockProfile));
            }
        });
    }

    public static void allowingUserProfileToReturnLocalization(JUnitRuleMockery2 context, final UserProfile mockUserProfile, final Localization mockLocalization) {
        context.checking(new Expectations() {
            {
                allowing(mockUserProfile).getLocalization();
                will(returnValue(mockLocalization));
            }
        });
        
    }

}
