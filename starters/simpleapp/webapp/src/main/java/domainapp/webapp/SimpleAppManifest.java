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
package domainapp.webapp;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import org.apache.isis.config.IsisPresets;
import org.apache.isis.extensions.fixtures.IsisExtFixturesModule;
import org.apache.isis.jdo.IsisBootDataNucleus;
import org.apache.isis.runtime.spring.IsisBoot;
import org.apache.isis.security.shiro.IsisBootSecurityShiro;
import org.apache.isis.viewer.restfulobjects.IsisBootViewerRestfulObjects;
import org.apache.isis.viewer.wicket.viewer.IsisBootViewerWicket;

import domainapp.application.ApplicationModule;
import domainapp.application.fixture.scenarios.DomainAppDemo;

/**
 * Makes the integral parts of the 'simple app' web application.
 */
@Configuration
@PropertySources({
    @PropertySource(IsisPresets.H2InMemory),
    @PropertySource(IsisPresets.NoTranslations),
    @PropertySource(IsisPresets.DataNucleusAutoCreate),
})
@Import({
    IsisBoot.class,
    IsisBootSecurityShiro.class,
    IsisBootDataNucleus.class,
    IsisBootViewerRestfulObjects.class,
    IsisBootViewerWicket.class,
    IsisExtFixturesModule.class,

    ApplicationModule.class,

    DomainAppDemo.class // register this fixture
})
public class SimpleAppManifest {


}
