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
package org.apache.isis.runtime.services.h2;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.runtime.system.context.IsisContext;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY,
        objectType = "isisApplib.H2ManagerMenu"
        )
@DomainServiceLayout(
        named = "Prototyping",
        menuOrder = "500.800",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
        )
public class H2ManagerMenu {


    private String url;

    @PostConstruct
    public void init(Map<String,String> properties) {
        this.url = IsisContext.getConfiguration()
                .getString("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionURL");
    }


    public static class ActionDomainEvent extends IsisApplibModule.ActionDomainEvent<H2ManagerMenu>{ 
        private static final long serialVersionUID = 1L; }

    @Action(
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING,
            domainEvent = ActionDomainEvent.class
            )
    @ActionLayout(
            named = "H2 Console",
            cssClassFa = "database"
            )
    public LocalResourcePath openH2Console() {
        // TODO: this is a bit of a hack, needs to be improved, eg by searching on the classpath, also make the URL configurable
        return new LocalResourcePath("/db/");
    }
    public boolean hideOpenH2Console() {
        return _Strings.isNullOrEmpty(url) || !url.contains("h2:mem");
    }

}
