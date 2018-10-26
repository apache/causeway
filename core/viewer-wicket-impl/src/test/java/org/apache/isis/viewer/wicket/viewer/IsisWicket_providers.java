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

package org.apache.isis.viewer.wicket.viewer;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryBuilder;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class IsisWicket_providers {

    private IsisWicketModule isisWicketModule;
    private Injector injector;

    @Before
    public void setUp() throws Exception {
        isisWicketModule = new IsisWicketModule(null, null);
        injector = Guice.createInjector(isisWicketModule);
    }

    @Ignore // REVIEW: DKH
    @Test
    public void deploymentType() {
        final DeploymentType instance = injector.getInstance(DeploymentType.class);
        assertThat(instance, is(notNullValue()));
    }

    @Ignore // REVIEW: DKH
    @Test
    public void configurationBuilder() {
        final IsisConfigurationBuilder instance = injector.getInstance(IsisConfigurationBuilder.class);
        assertThat(instance, is(notNullValue()));
    }

    @Ignore
    // need to handle config
    @Test
    public void isisSystem() {
        final IsisSessionFactoryBuilder instance = injector.getInstance(IsisSessionFactoryBuilder.class);
        assertThat(instance, is(notNullValue()));
    }

}
