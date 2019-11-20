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
package domainapp.dom.impl;

import java.util.List;

import javax.inject.Inject;
import javax.jdo.JDOQLTypedQuery;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.jdo.jdosupport.IsisJdoSupport_v3_2;

import domainapp.dom.types.Name;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "helloworld.HelloWorldObjects"
        )
public class HelloWorldObjects {

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    @ActionLayout(promptStyle = PromptStyle.DIALOG_MODAL)
    public HelloWorldObject create(
            @Name final String name) {
        return repositoryService.persist(new HelloWorldObject(name));
    }
    public String default0Create() {
        return "Hello World!";
    }


    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(promptStyle = PromptStyle.DIALOG_SIDEBAR)
    public List<HelloWorldObject> findByName(
            @Name final String name) {
        JDOQLTypedQuery<HelloWorldObject> q = isisJdoSupport.newTypesafeQuery(HelloWorldObject.class);
        final QHelloWorldObject cand = QHelloWorldObject.candidate();
        q = q.filter(
                cand.name.indexOf(q.stringParameter("name")).ne(-1)
                );
        return q.setParameter("name", name)
                .executeList();
    }

    @Action(semantics = SemanticsOf.SAFE, restrictTo = RestrictTo.PROTOTYPING)
    public List<HelloWorldObject> listAll() {
        return repositoryService.allInstances(HelloWorldObject.class);
    }

    @Inject RepositoryService repositoryService;
    @Inject IsisJdoSupport_v3_2 isisJdoSupport;

}
