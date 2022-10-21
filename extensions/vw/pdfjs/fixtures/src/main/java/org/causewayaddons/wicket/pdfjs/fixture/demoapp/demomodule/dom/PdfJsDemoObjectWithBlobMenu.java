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
package org.causewayaddons.wicket.pdfjs.fixture.demoapp.demomodule.dom;

import java.util.List;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberOrder;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.repository.RepositoryService;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY,
        logicalTypeName = "wktPdfJsFixture.PdfJsDemoObjectWithBlobMenu"
)
@DomainServiceLayout(
        named = "Demo",
        menuOrder = "10.4"
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class PdfJsDemoObjectWithBlobMenu {

    final RepositoryService repositoryService;

    @Action(
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            bookmarking = BookmarkPolicy.AS_ROOT
    )
    @MemberOrder(sequence = "1")
    public List<PdfJsDemoObjectWithBlob> listAllDemoObjectsWithBlob() {
        return repositoryService.allInstances(PdfJsDemoObjectWithBlob.class);
    }


    @MemberOrder(sequence = "2")
    public PdfJsDemoObjectWithBlob createDemoObjectWithBlob(
            @ParameterLayout(named = "Name")
            final String name) {
        final PdfJsDemoObjectWithBlob obj = repositoryService.instantiate(PdfJsDemoObjectWithBlob.class);
        obj.setName(name);
        repositoryService.persist(obj);
        return obj;
    }



}
