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
package domainapp.application.services.dbmanager;

import java.util.Map;

import javax.annotation.PostConstruct;

import com.google.common.base.Strings;

import org.hsqldb.util.DatabaseManagerSwing;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY
)
@DomainServiceLayout(
        named = "Prototyping",
        menuOrder = "999",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
public class HsqlDbManagerMenu {


    private String url;

    @PostConstruct
    public void init(Map<String,String> properties) {
        this.url = properties.get("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionURL");
    }


    @Action(
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            named = "HSQL DB Manager",
            cssClassFa = "database"
    )
    public void hsqlDbManager() {
        String[] args = {"--url", url, "--noexit" };
        DatabaseManagerSwing.main(args);
    }
    public boolean hideHsqlDbManager() {
        return Strings.isNullOrEmpty(url) || !url.contains("hsqldb:mem");
    }


}
