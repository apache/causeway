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
package org.apache.causeway.viewer.graphql.model.application;

import java.lang.reflect.Proxy;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.homepage.HomePageResolverService;
import org.apache.causeway.applib.services.layout.LayoutService;
import org.apache.causeway.viewer.graphql.model.domain.common.query.ResourcePath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationEntryServiceTest {

    @Test
    void missingHomeIsRepresentedAsAbsence() {
        var service = service(() -> null);

        var snapshot = service.applicationSnapshot(false);

        assertNull(snapshot.home());
        assertTrue(snapshot.issues().isEmpty());
    }

    @Test
    void unavailableMenuResourceReturnsBoundedNonDisclosingIssue() {
        var failingLayout = (LayoutService) Proxy.newProxyInstance(
                LayoutService.class.getClassLoader(),
                new Class<?>[] {LayoutService.class},
                (proxy, method, arguments) -> {
                    throw new IllegalStateException("PRIVATE_LAYOUT_FAILURE");
                });
        var service = new ApplicationEntryService(
                null,
                failingLayout,
                null,
                () -> null,
                null,
                null,
                (ResourcePath) null);

        var resource = service.menuBarsResource();

        assertNull(resource.xml());
        assertEquals("MENU_RESOURCE_UNAVAILABLE", resource.issues().get(0).code());
        assertTrue(resource.issues().get(0).message().indexOf("PRIVATE_LAYOUT_FAILURE") < 0);
    }

    @Test
    void failingResolverReturnsBoundedNonDisclosingIssue() {
        var service = service(() -> {
            throw new IllegalStateException("PRIVATE_HOME_FAILURE");
        });

        var snapshot = service.applicationSnapshot(false);

        assertNull(snapshot.home());
        assertEquals(1, snapshot.issues().size());
        assertEquals("HOME_UNAVAILABLE", snapshot.issues().get(0).code());
        assertTrue(snapshot.issues().get(0).message().indexOf("PRIVATE_HOME_FAILURE") < 0);
    }

    private static ApplicationEntryService service(final HomePageResolverService homePageResolverService) {
        return new ApplicationEntryService(
                null,
                null,
                null,
                homePageResolverService,
                null,
                null,
                (ResourcePath) null);
    }
}
