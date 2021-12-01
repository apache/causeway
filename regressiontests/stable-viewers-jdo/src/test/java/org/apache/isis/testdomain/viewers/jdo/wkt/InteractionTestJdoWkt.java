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
package org.apache.isis.testdomain.viewers.jdo.wkt;

import javax.inject.Inject;

import org.apache.wicket.Page;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.markup.head.filter.JavaScriptFilteredIntoFooterHeaderResponse;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.testdomain.RegressionTestAbstract;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.conf.Configuration_usingWicket;
import org.apache.isis.testdomain.conf.Configuration_usingWicket.RequestCycleFactory;
import org.apache.isis.testdomain.jdo.JdoTestFixtures;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettingsAccessor;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.model.util.PageParameterUtils;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
                Configuration_usingWicket.class
        },
        properties = {
        })
@TestPropertySource({
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class InteractionTestJdoWkt extends RegressionTestAbstract {

    @Inject private IsisAppCommonContext commonContext;
    @Inject private RequestCycleFactory requestCycleFactory;
    @Inject private JdoTestFixtures testFixtures;
    private WicketTester wktTester;

    @RequiredArgsConstructor
    static class WicketApplicationStub
    extends WebApplication
    implements
        ComponentFactoryRegistryAccessor,
        WicketViewerSettingsAccessor,
        HasCommonContext {
        private static final long serialVersionUID = 1L;

        @Override
        protected void init() {
            super.init();
            final IBootstrapSettings settings = new BootstrapSettings();
            settings.setDeferJavascript(false);
            Bootstrap.install(this, settings);
            setHeaderResponseDecorator(response ->
                new ResourceAggregator(new JavaScriptFilteredIntoFooterHeaderResponse(response, "footerJS")));
        }

        @Getter
        private final IsisAppCommonContext commonContext;

        @Getter(lazy=true)
        private final ComponentFactoryRegistry componentFactoryRegistry =
                getCommonContext().lookupServiceElseFail(ComponentFactoryRegistry.class);

        @Getter(lazy=true)
        private final PageClassRegistry pageClassRegistry =
                getCommonContext().lookupServiceElseFail(PageClassRegistry.class);

        @Getter(lazy=true)
        private final WicketViewerSettings settings =
                getCommonContext().lookupServiceElseFail(WicketViewerSettings.class);

        @Override
        public Class<? extends Page> getHomePage() {
            return getPageClassRegistry().getPageClass(PageType.HOME);
        }

    }


    @BeforeEach
    void setUp() throws InterruptedException {

        run(()->{

            testFixtures.setUp3Books();

//            val inventoryJaxbVm = testFixtures.setUpViewmodelWith3Books();
//            final ManagedObject domainObject = objectManager.adapt(inventoryJaxbVm);
//
//            requestCycleFactory.newRequestCycle(
//                    EntityPage.class,
//                    PageParameterUtils.createPageParametersForObject(domainObject));
//
            wktTester = new WicketTester(new WicketApplicationStub(commonContext));

        });
    }

    @AfterEach
    void cleanUp() {
        requestCycleFactory.clearRequestCycle();
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