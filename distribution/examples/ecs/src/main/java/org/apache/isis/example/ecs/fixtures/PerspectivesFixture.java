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


package org.apache.isis.example.ecs.fixtures;

import org.apache.isis.applib.fixtures.UserProfileFixture;
import org.apache.isis.applib.profiles.Perspective;
import org.apache.isis.applib.profiles.Profile;
import org.apache.isis.example.ecs.CustomerFactory;
import org.apache.isis.example.ecs.LocationFactory;
import org.apache.isis.example.ecs.services.CustomerRepository;
import org.apache.isis.example.ecs.services.PaymentMethodFactory;
import org.apache.isis.example.ecs.services.PhoneNumberFactory;


public class PerspectivesFixture extends UserProfileFixture {
	@Override
	protected void installProfiles() {
		Profile profile = newUserProfile();
		Perspective perspective = profile.newPerspective("ECS");
		perspective.addToServices(PhoneNumberFactory.class);
		perspective.addToServices(LocationFactory.class);
		perspective.addToServices(CustomerRepository.class);
		perspective.addToServices(PaymentMethodFactory.class);
		perspective.addToServices(CustomerFactory.class);
		
		saveAsDefault(profile);
	}
}
