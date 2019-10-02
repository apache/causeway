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
package org.apache.isis.runtime.services.hsqldb;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.isis.config.IsisConfiguration;
import org.hsqldb.util.DatabaseManagerSwing;

import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "isisApplib.HsqlDbManagerMenu"
        )
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
        )
public class HsqlDbManagerMenu {


    @Inject IsisConfiguration isisConfiguration;

    private String url;

    @PostConstruct
    public void init() {
        this.url = isisConfiguration.getPersistor().getDatanucleus().getImpl().getJavax().getJdo().getOption().getConnectionUrl();
    }


    public static class ActionDomainEvent extends IsisApplibModule.ActionDomainEvent<HsqlDbManagerMenu> { 
        private static final long serialVersionUID = 1L; }

    @Action(
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING,
            domainEvent = ActionDomainEvent.class
            )
    @ActionLayout(
            named = "HSQL DB Manager",
            cssClassFa = "database"
            )
    @MemberOrder(sequence = "500.800")
    public void hsqlDbManager() {
        String[] args = {"--url", url, "--noexit" };
        DatabaseManagerSwing.main(args);
    }
    public boolean hideHsqlDbManager() {
        try {
            // hsqldb is configured as optional in the applib's pom.xml
            _Context.loadClass("org.hsqldb.util.DatabaseManagerSwing");
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return true;
        }
        return _Strings.isNullOrEmpty(url) || !url.contains("hsqldb:mem");
    }


}
