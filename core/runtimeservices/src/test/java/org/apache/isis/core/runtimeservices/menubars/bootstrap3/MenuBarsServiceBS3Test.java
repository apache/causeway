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
package org.apache.isis.core.runtimeservices.menubars.bootstrap3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.runtimeservices.menubars.MenuBarsLoaderServiceDefault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.Getter;
import lombok.val;

class MenuBarsServiceBS3Test
implements HasMetaModelContext {

    @Getter(onMethod_ = {@Override})
    private MetaModelContext metaModelContext;

    private MenuBarsServiceBS3 menuBarsService;

    @BeforeEach
    void setUp() throws Exception {
        metaModelContext = MetaModelContext_forTesting
                .builder()
                .singleton(new Bar())
                .build();

        getSpecificationLoader().loadSpecification(Bar.class);

        assertTrue(getServiceRegistry().streamRegisteredBeans()
                .anyMatch(bean->bean.getBeanClass().equals(Bar.class)));

        assertTrue(metaModelContext.streamServiceAdapters()
                .anyMatch(domainObject->domainObject.getSpecification().getCorrespondingClass().equals(Bar.class)));


        val messageService = getServiceRegistry().lookupServiceElseFail(MessageService.class);
        val jaxbService = getServiceRegistry().lookupServiceElseFail(JaxbService.class);

        val menuBarsLoaderService = new MenuBarsLoaderServiceDefault(
                getSystemEnvironment(),
                jaxbService,
                getConfiguration());

        menuBarsService = new MenuBarsServiceBS3(
                menuBarsLoaderService,
                messageService,
                jaxbService,
                getSystemEnvironment(),
                metaModelContext);

    }

    @AfterEach
    void tearDown() throws Exception {
        getSpecificationLoader().disposeMetaModel();
    }

    @Test
    void test() {
        val menuBars = menuBarsService.menuBars();
        assertNotNull(menuBars);

        val menuCounter = _Refs.intRef(0);
        menuBars.visit(v->{
            menuCounter.inc();
        });

        assertEquals(1, menuCounter.getValue());

        //TODO[ISIS-2787] verify menu xml export (roundtrip?)
    }

}
