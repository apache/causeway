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


package org.apache.isis.example.expenses.fixtures;

import org.apache.isis.applib.fixtures.UserProfileFixture;
import org.apache.isis.applib.profiles.Perspective;
import org.apache.isis.applib.profiles.Profile;
import org.apache.isis.example.expenses.claims.Claims;
import org.apache.isis.example.expenses.employee.Employees;
import org.apache.isis.example.expenses.recordedAction.impl.RecordActionServiceImpl;


public class ExplorationUserProfileFixture extends UserProfileFixture {

    @Override
    protected void installProfiles() {

        Profile templateProfile = newUserProfile();
        Perspective perspective = templateProfile.newPerspective("Expenses");
        perspective.addToServices(Claims.class);
        perspective.addToServices(Employees.class);
        saveAsDefault(templateProfile);

        Profile svenProfile = newUserProfile();
        Perspective claimsPerspective = svenProfile.newPerspective("Claims");
        claimsPerspective.addToServices(Claims.class);
        claimsPerspective.addToServices(Employees.class);
        saveForUser("sven", svenProfile);

        saveForUser("bob", newUserProfile(svenProfile));
        saveForUser("joe", newUserProfile(svenProfile));

        Profile dickProfile = newUserProfile();
        Perspective supervisorPerspective = dickProfile.newPerspective("Supervisor");
        supervisorPerspective.addToServices(Claims.class);
        supervisorPerspective.addToServices(Employees.class);
        supervisorPerspective.addToServices(RecordActionServiceImpl.class);

        saveForUser("dick", dickProfile);

    }
}
