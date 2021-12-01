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
package org.apache.isis.testdomain.viewers.jpa.wkt;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.conf.Configuration_usingJpa;
import org.apache.isis.testdomain.conf.Configuration_usingWicket;
import org.apache.isis.testdomain.conf.Configuration_usingWicket.RequestCycleFactory;
import org.apache.isis.testdomain.model.interaction.InteractionDemo;
import org.apache.isis.testdomain.util.interaction.InteractionTestAbstract;
import org.apache.isis.viewer.wicket.model.util.PageParameterUtils;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingJpa.class,
                Configuration_usingWicket.class
        },
        properties = {
        })
@TestPropertySource({
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class InteractionTestJpaWkt extends InteractionTestAbstract {

    @Inject IsisAppCommonContext commonContext;
    @Inject RequestCycleFactory requestCycleFactory;

    private ManagedObject domainObject;

    @BeforeEach
    void setUp() {
        domainObject = newViewmodel(InteractionDemo.class);
        requestCycleFactory.newRequestCycle(
                EntityPage.class,
                PageParameterUtils.createPageParametersForObject(domainObject));
    }

    @AfterEach
    void cleanUp() {
        requestCycleFactory.clearRequestCycle();
    }

    @Test
    void viewmodel_with_referenced_entities() {
        //TODO populate VM with entities
        //TODO simulate change of a String property -> should yield a new Title and serialized URL link
        //TODO simulate interaction with choice provider, where entries are entities -> should be attached, eg. test whether we can generate a title for these
    }
}