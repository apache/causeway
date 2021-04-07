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

package org.apache.isis.applib.services.confview;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.factory.FactoryService;

/**
 * Simply provides a UI in order to access the configuration properties
 * available from {@link ConfigurationViewService}.
 *
 * @since 2.0 {@index}
 */
@Named("isis.applib.ConfigurationMenu")
@DomainService(objectType = "isis.applib.ConfigurationMenu")
@DomainServiceLayout(menuBar = DomainServiceLayout.MenuBar.TERTIARY)
public class ConfigurationMenu {

    public static abstract class ActionDomainEvent
            extends IsisModuleApplib.ActionDomainEvent<ConfigurationMenu> {}

    private final FactoryService factoryService;

    @Inject
    public ConfigurationMenu(FactoryService factoryService) {
        this.factoryService = factoryService;
    }

    public static class ConfigurationDomainEvent
            extends ActionDomainEvent {}

    @Action(
            domainEvent = ConfigurationDomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            cssClassFa = "fa-wrench",
            sequence = "500.900.1")
    public ConfigurationViewmodel configuration(){
        return factoryService.viewModel(new ConfigurationViewmodel());
    }


}
