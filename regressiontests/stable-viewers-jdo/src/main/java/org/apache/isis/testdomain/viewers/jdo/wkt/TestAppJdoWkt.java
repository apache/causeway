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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.HomePage;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.commons.internal.debug.xray.XrayEnable;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.conf.Configuration_usingWicket;
import org.apache.isis.testdomain.jdo.JdoInventoryJaxbVm;
import org.apache.isis.testdomain.jdo.JdoTestFixtures;
import org.apache.isis.viewer.wicket.viewer.IsisModuleViewerWicketViewer;

/**
 * Bootstrap the test application.
 */
@SpringBootApplication
@Import({
    Configuration_usingJdo.class,
    Configuration_usingWicket.class,

    // UI (Wicket Viewer)
    IsisModuleViewerWicketViewer.class,

    XrayEnable.class // for debugging only
})
public class TestAppJdoWkt extends SpringBootServletInitializer {

    /**
     *
     * @param args
     * @implNote this is to support the <em>Spring Boot Maven Plugin</em>, which auto-detects an
     * entry point by searching for classes having a {@code main(...)}
     */
    public static void main(final String[] args) {
        IsisPresets.prototyping();
        SpringApplication.run(new Class[] { TestAppJdoWkt.class }, args);
    }

    @DomainObject(
            nature=Nature.VIEW_MODEL,
            logicalTypeName = "testdomain.jdo.TestHomePage"
            )
    @HomePage
    public static class TestHomePage {

        @Inject UserService userService;
        @Inject JdoTestFixtures testFixtures;

        @ObjectSupport public String title() {
            return "Hello, " + userService.currentUserNameElseNobody();
        }

        @Action @ActionLayout(sequence = "0.1")
        public TestHomePage setup() {
            testFixtures.setUp3Books();
            return this;
        }

        @Action @ActionLayout(sequence = "0.2")
        public JdoInventoryJaxbVm openSamplePage() {
            return testFixtures.setUpViewmodelWith3Books();
        }

    }

}
