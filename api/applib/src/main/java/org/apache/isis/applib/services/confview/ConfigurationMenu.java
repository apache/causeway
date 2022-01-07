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
import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.DomainService;
import org.apache.isis.applib.annotations.DomainServiceLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.NatureOfService;
import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.applib.services.factory.FactoryService;

import lombok.RequiredArgsConstructor;

/**
 * Simply provides a UI in order to access the configuration properties
 * available from {@link ConfigurationViewService}.
 *
 * @since 2.0 {@index}
 */
@DomainService(
        nature = NatureOfService.VIEW,
        logicalTypeName = ConfigurationMenu.LOGICAL_TYPE_NAME
)
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.TERTIARY
)
@Named(ConfigurationMenu.LOGICAL_TYPE_NAME)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class ConfigurationMenu {

    public static final String LOGICAL_TYPE_NAME = IsisModuleApplib.NAMESPACE_CONF + ".ConfigurationMenu";

    public static abstract class ActionDomainEvent<T> extends IsisModuleApplib.ActionDomainEvent<T> {}

    final FactoryService factoryService;


    @Action(
            domainEvent = configuration.ActionEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            cssClassFa = "fa-wrench",
            sequence = "500.900.1")
    public class configuration{

        public class ActionEvent extends ActionDomainEvent<configuration> {}

        @MemberSupport public ConfigurationViewmodel act(){
            return factoryService.viewModel(new ConfigurationViewmodel());
        }
    }

}
