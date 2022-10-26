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
package org.apache.causeway.extensions.hsqldbmgr.dom.services;

import javax.inject.Inject;
import javax.inject.Named;

import org.hsqldb.util.DatabaseManagerSwing;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.core.config.datasources.DataSourceIntrospectionService;
import org.apache.causeway.core.config.datasources.DataSourceIntrospectionService.DataSourceInfo;
import org.apache.causeway.extensions.hsqldbmgr.dom.CausewayModuleExtHsqldbMgr;

import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@DomainService(
        nature = NatureOfService.VIEW
)
@Named(CausewayModuleExtHsqldbMgr.NAMESPACE + ".HsqlDbManagerMenu")
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@Log4j2
public class HsqlDbManagerMenu {

    private final String url;

    @Inject
    public HsqlDbManagerMenu(final DataSourceIntrospectionService datasourceIntrospector) {
        this.url = datasourceIntrospector.getDataSourceInfos()
        .stream()
        .map(DataSourceInfo::getJdbcUrl)
        .filter(jdbcUrl->{
            if(jdbcUrl.contains("hsqldb:mem")) {
                log.info("found hsqldb in-memory data-source: {}", jdbcUrl);
                return true;
            }
            return false;
        })
        .findFirst()
        .orElse(null);
    }

    public static class ActionDomainEvent extends CausewayModuleApplib.ActionDomainEvent<HsqlDbManagerMenu> { }

    @Action(
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING,
            domainEvent = ActionDomainEvent.class
            )
    @ActionLayout(
            named = "HSQL DB Manager",
            cssClassFa = "database",
            sequence = "500.800")
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
