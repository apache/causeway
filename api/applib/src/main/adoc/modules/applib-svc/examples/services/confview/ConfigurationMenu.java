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

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.SemanticsOf;

@Named("isisApplib.ConfigurationMenu")
@DomainService(objectType = "isisApplib.ConfigurationMenu")
@DomainServiceLayout(menuBar = DomainServiceLayout.MenuBar.TERTIARY)
// tag::refguide[]
// ...
public class ConfigurationMenu {

    // ...
    // end::refguide[]
    public static abstract class ActionDomainEvent
            extends IsisModuleApplib.ActionDomainEvent<ConfigurationMenu> {}

    private final ConfigurationViewService configurationService;

    @Inject
    public ConfigurationMenu(ConfigurationViewService configurationService) {
        this.configurationService = configurationService;
    }

    public static class ConfigurationDomainEvent
            extends ActionDomainEvent {}

    @Action(
            domainEvent = ConfigurationDomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            cssClassFa = "fa-wrench"
    )
    @MemberOrder(sequence = "500.900.1")
    // tag::refguide[]
    public Set<ConfigurationProperty> configuration(){
        return configurationService.allProperties();
    }

}
// end::refguide[]
