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
package domainapp.application.manifest;

import java.util.List;

import com.google.common.collect.Lists;

import domainapp.modules.simple.fixture.scenario.CreateSimpleObjects;

/**
 * Run the app but without setting up any fixtures.
 */
public class DomainAppAppManifestWithFixtures extends DomainAppAppManifest {

    public DomainAppAppManifestWithFixtures() {
        this(null);
    }

    public DomainAppAppManifestWithFixtures(final String authMechanism) {
        super((List)Lists.newArrayList(CreateSimpleObjects.class), authMechanism, null);
    }

}
