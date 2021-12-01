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

import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.testdomain.RegressionTestAbstract;
import org.apache.isis.testdomain.conf.Configuration_usingJpa;
import org.apache.isis.testdomain.conf.Configuration_usingWicket;
import org.apache.isis.testdomain.conf.Configuration_usingWicket.WicketTesterFactory;
import org.apache.isis.testdomain.jpa.JpaTestFixtures;
import org.apache.isis.viewer.wicket.model.util.PageParameterUtils;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
                Configuration_usingWicket.class
        },
        properties = {
        })
@TestPropertySource({
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class InteractionTestJpaWkt extends RegressionTestAbstract {

    @Inject private IsisAppCommonContext commonContext;
    @Inject private WicketTesterFactory wicketTesterFactory;
    @Inject private JpaTestFixtures testFixtures;
    private WicketTester wktTester;

    @BeforeEach
    void setUp() throws InterruptedException {

        wktTester = wicketTesterFactory.createTester();

        run(()->{
            testFixtures.setUp3Books();
        });
    }

    @AfterEach
    void cleanUp() {
        wktTester.destroy();
    }

    @Test
    void viewmodel_with_referenced_entities() {

        val pageParameters = call(()->{
            val inventoryJaxbVm = testFixtures.setUpViewmodelWith3Books();
            final ManagedObject domainObject = objectManager.adapt(inventoryJaxbVm);
            return PageParameterUtils.createPageParametersForObject(domainObject);
        });

        System.err.printf("pageParameters %s%n", pageParameters);

        run(()->{
            val entityPage = EntityPage.ofPageParameters(commonContext, pageParameters);
            wktTester.startPage(entityPage);
            wktTester.assertRenderedPage(EntityPage.class);
        });

        //TODO populate VM with entities
        //TODO simulate change of a String property -> should yield a new Title and serialized URL link
        //TODO simulate interaction with choice provider, where entries are entities -> should be attached, eg. test whether we can generate a title for these
    }
}