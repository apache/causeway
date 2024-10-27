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
package org.apache.causeway.core.runtimeservices;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import org.springframework.core.io.AbstractResource;

import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.services.menu.MenuBarsLoaderService;
import org.apache.causeway.applib.services.menu.MenuBarsMarshallerService;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.internal.ioc._SingletonBeanProvider;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting.MetaModelContext_forTestingBuilder;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.runtimeservices.menubars.MenuBarsLoaderServiceDefault;
import org.apache.causeway.core.runtimeservices.menubars.bootstrap.MenuBarsMarshallerServiceBootstrap;
import org.apache.causeway.core.runtimeservices.menubars.bootstrap.MenuBarsServiceBootstrap;

import lombok.Getter;

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

    protected final AtomicReference<AbstractResource> menubarsLayoutXmlResourceRef =
            new AtomicReference<>();

    @BeforeEach
    final void setUp() throws Exception {
        var mmcBuilder = MetaModelContext_forTesting.builder()
                .memberExecutor(Mockito.mock(MemberExecutorService.class));

        // install runtime services into MMC (extend as needed)

        onSetUp(mmcBuilder);

        mmcBuilder.singletonProvider(
                _SingletonBeanProvider
                .forTestingLazy(MenuBarsMarshallerService.class, ()->{
                    var jaxbService = getServiceRegistry().lookupServiceElseFail(JaxbService.class);
                    return new MenuBarsMarshallerServiceBootstrap(
                            jaxbService);
                }));

        mmcBuilder.singletonProvider(
                _SingletonBeanProvider
                .forTestingLazy(MenuBarsLoaderService.class, ()->{
                    return new MenuBarsLoaderServiceDefault(
                            menubarsLayoutXmlResourceRef,
                            CommonMimeType.XML); // format under test
                }));

        mmcBuilder.singletonProvider(
                _SingletonBeanProvider
                .forTestingLazy(MenuBarsService.class, ()->{

                    var messageService = getServiceRegistry().lookupServiceElseFail(MessageService.class);
                    var jaxbService = getServiceRegistry().lookupServiceElseFail(JaxbService.class);
                    var menuBarsMenuBarsMarshaller = getServiceRegistry().lookupServiceElseFail(MenuBarsMarshallerService.class);
                    var menuBarsLoaderService = getServiceRegistry().lookupServiceElseFail(MenuBarsLoaderService.class);
                    return new MenuBarsServiceBootstrap(
                            menuBarsLoaderService,
                            menuBarsMenuBarsMarshaller,
                            messageService,
                            jaxbService,
                            metaModelContext);
                    }));

        metaModelContext = mmcBuilder.build();

        getConfiguration().getCore().getMetaModel().getIntrospector().setLockAfterFullIntrospection(false);

        afterSetUp();
    }

    @AfterEach
    final void tearDown() throws Exception {
        onTearDown();
        metaModelContext.getSpecificationLoader().disposeMetaModel();
        metaModelContext = null;
    }

    protected void onSetUp(final MetaModelContext_forTestingBuilder mmcBuilder) {
    }

    protected void afterSetUp() {
    }

    protected void onTearDown() {
    }

}
