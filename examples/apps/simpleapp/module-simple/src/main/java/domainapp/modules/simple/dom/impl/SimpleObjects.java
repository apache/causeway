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
package domainapp.modules.simple.dom.impl;

import java.util.List;

import javax.inject.Inject;
import javax.jdo.JDOQLTypedQuery;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.jdo.jdosupport.IsisJdoSupport_v3_2;

import domainapp.modules.simple.dom.types.Name;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "simple.SimpleObjects"
        )
public class SimpleObjects {

    public static class CreateDomainEvent extends ActionDomainEvent<SimpleObjects> {
        private static final long serialVersionUID = 1L;}

    @Action(domainEvent = CreateDomainEvent.class)
    @ActionLayout(promptStyle = PromptStyle.DIALOG_MODAL)
    public SimpleObject create(
            @Name final String name
            ) {
        return repositoryService.persist(SimpleObject.ofName(name));
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(bookmarking = BookmarkPolicy.AS_ROOT, promptStyle = PromptStyle.DIALOG_SIDEBAR)
    public List<SimpleObject> findByName(
            @Name final String name
            ) {
        JDOQLTypedQuery<SimpleObject> q = isisJdoSupport.newTypesafeQuery(SimpleObject.class);
        final QSimpleObject cand = QSimpleObject.candidate();
        q = q.filter(
                cand.name.indexOf(q.stringParameter("name")).ne(-1)
                );
        return q.setParameter("name", name)
                .executeList();
    }

    public SimpleObject findByNameExact(final String name) {
        JDOQLTypedQuery<SimpleObject> q = isisJdoSupport.newTypesafeQuery(SimpleObject.class);
        final QSimpleObject cand = QSimpleObject.candidate();
        q = q.filter(
                cand.name.eq(q.stringParameter("name"))
                );
        return q.setParameter("name", name)
                .executeUnique();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(bookmarking = BookmarkPolicy.AS_ROOT)
    public List<SimpleObject> listAll() {
        return repositoryService.allInstances(SimpleObject.class);
    }

    public void ping() {
        JDOQLTypedQuery<SimpleObject> q = isisJdoSupport.newTypesafeQuery(SimpleObject.class);
        final QSimpleObject candidate = QSimpleObject.candidate();
        q.range(0,2);
        q.orderBy(candidate.name.asc());
        q.executeList();
    }

    @Inject RepositoryService repositoryService;
    @Inject IsisJdoSupport_v3_2 isisJdoSupport;

}
