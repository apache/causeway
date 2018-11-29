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

import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY,
        objectType = "isisApplib.ConfigurationMenu"
        )
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.TERTIARY,
        menuOrder = "500.900"
        )
public class ConfigurationMenu {

    public static abstract class ActionDomainEvent
    extends IsisApplibModule.ActionDomainEvent<ConfigurationMenu> {
        private static final long serialVersionUID = 1L;
    }

    public static class AllConfigurationPropertiesDomainEvent
    extends ActionDomainEvent {
        private static final long serialVersionUID = 1L;
    }

    @Action(
            domainEvent = AllConfigurationPropertiesDomainEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @ActionLayout(
            cssClassFa = "fa-wrench"
            )
    @MemberOrder(sequence = "500.900.1")
    public Set<ConfigurationProperty> configuration(){
        return configurationService.allProperties();
    }

    @javax.inject.Inject
    private ConfigurationViewService configurationService;

}
