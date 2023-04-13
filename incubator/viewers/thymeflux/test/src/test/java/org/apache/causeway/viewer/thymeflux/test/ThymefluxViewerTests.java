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
package org.apache.causeway.viewer.thymeflux.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ui.Model;
import org.springframework.validation.support.BindingAwareModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.causeway.applib.annotation.DomainServiceLayout.MenuBar;
import org.apache.causeway.commons.io.YamlUtils;
import org.apache.causeway.viewer.commons.applib.services.header.HeaderUiModel;
import org.apache.causeway.viewer.thymeflux.model.root.ThymefluxRootController;
import org.apache.causeway.viewer.thymeflux.viewer.CausewayModuleIncViewerThymefluxViewer;

import demoapp.testing.jpa.DemoDomainJpa_forTesting;

@SpringBootTest(
        classes = {
                DemoDomainJpa_forTesting.class,
                CausewayModuleIncViewerThymefluxViewer.class
        },
        //webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                //"spring.jpa.show-sql=true",
                //"logging.level.org.springframework.orm.jpa=DEBUG"
        })
@ActiveProfiles(profiles = "demo-jpa")
@AutoConfigureWebTestClient
class ThymefluxViewerTests {

    //@Autowired private WebTestClient client;
    @Autowired private ThymefluxRootController rootController;

    @Test
    void rootController() {
        final Model model = new BindingAwareModelMap();
        rootController.root(model);

        var headerUiModel = (HeaderUiModel)model.getAttribute("headerUiModel");
        assertNotNull(headerUiModel);

        var navbar = headerUiModel.navbar();

        assertEquals(MenuBar.PRIMARY,   navbar.primary().menuBarSelect());
        assertEquals(MenuBar.SECONDARY, navbar.secondary().menuBarSelect());
        assertEquals(MenuBar.TERTIARY,  navbar.tertiary().menuBarSelect());

        System.err.printf("== navbar: %n%s====%n", YamlUtils.toStringUtf8(navbar.primary()));
    }


//    @Test
//    void responseOkOnRoot() {
////        Mockito
////            .when(repository.getIsDataReady(ArgumentMatchers.any()))
////            .thenReturn(Mono.just(true));
//
//        client.get()
//            .uri("/tflux/")
//            //.accept(MediaType.APPLICATION_JSON_UTF8)
//            .exchange()
//            .expectStatus().isOk();
//    }
}

