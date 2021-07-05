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
package org.apache.isis.core.runtimeservices;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.menu.MenuBarsLoaderService;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.commons.internal.ioc._ManagedBeanAdapter;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting.MetaModelContext_forTestingBuilder;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.runtimeservices.menubars.MenuBarsLoaderServiceDefault;
import org.apache.isis.core.runtimeservices.menubars.bootstrap3.MenuBarsServiceBS3;

import lombok.Getter;
import lombok.val;

/**
 * Prototypical test base for the RuntimeServices module.
 *
 * @apiNote taking the {@code MetaModelTestAbstract} as blueprint
 *
 * @since 2.0
 */
public abstract class RuntimeServicesTestAbstract
implements HasMetaModelContext {

    @Getter(onMethod_ = {@Override})
    private MetaModelContext metaModelContext;

    @BeforeEach
    final void setUp() throws Exception {
        val mmcBuilder = MetaModelContext_forTesting.builder();

        // install runtime services into MMC (extend as needed)

        mmcBuilder.singletonProvider(
                _ManagedBeanAdapter
                .forTestingLazy(MenuBarsLoaderService.class, ()->{

                    val jaxbService = getServiceRegistry().lookupServiceElseFail(JaxbService.class);
                    return new MenuBarsLoaderServiceDefault(
                            getSystemEnvironment(),
                            jaxbService,
                            getConfiguration());
                }));


        mmcBuilder.singletonProvider(
                _ManagedBeanAdapter
                .forTestingLazy(MenuBarsService.class, ()->{

                    val messageService = getServiceRegistry().lookupServiceElseFail(MessageService.class);
                    val jaxbService = getServiceRegistry().lookupServiceElseFail(JaxbService.class);
                    val menuBarsLoaderService = getServiceRegistry().lookupServiceElseFail(MenuBarsLoaderService.class);
                    return new MenuBarsServiceBS3(
                            menuBarsLoaderService,
                            messageService,
                            jaxbService,
                            getSystemEnvironment(),
                            metaModelContext);

                    }));

        mmcBuilder.singletonProvider(
                _ManagedBeanAdapter
                .forTestingLazy(MenuBarsLoaderService.class, ()->{

                    val jaxbService = getServiceRegistry().lookupServiceElseFail(JaxbService.class);
                    return new MenuBarsLoaderServiceDefault(
                            getSystemEnvironment(),
                            jaxbService,
                            getConfiguration());

                    }));

        onSetUp(mmcBuilder);
        metaModelContext = mmcBuilder.build();
        afterSetUp();
    }

    @AfterEach
    final void tearDown() throws Exception {
        onTearDown();
        metaModelContext.getSpecificationLoader().disposeMetaModel();
        metaModelContext = null;
    }

    protected void onSetUp(MetaModelContext_forTestingBuilder mmcBuilder) {
    }

    protected void afterSetUp() {
    }

    protected void onTearDown() {
    }

}