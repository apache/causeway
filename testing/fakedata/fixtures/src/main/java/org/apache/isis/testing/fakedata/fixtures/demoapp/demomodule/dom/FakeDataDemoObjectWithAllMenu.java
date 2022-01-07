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
package org.apache.isis.testing.fakedata.fixtures.demoapp.demomodule.dom;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.BookmarkPolicy;
import org.apache.isis.applib.annotations.DomainService;
import org.apache.isis.applib.annotations.DomainServiceLayout;
import org.apache.isis.applib.annotations.NatureOfService;
import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.applib.services.repository.RepositoryService;

import lombok.val;

@DomainService(
        nature = NatureOfService.VIEW,
        logicalTypeName = "libFakeDataFixture.FakeDataDemoObjectWithAllMenu"
)
@DomainServiceLayout(
        named = "Demo"
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class FakeDataDemoObjectWithAllMenu {


    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(bookmarking = BookmarkPolicy.AS_ROOT, sequence = "1")
    public List<FakeDataDemoObjectWithAll> listAllDemoObjectsWithAll() {
        return repositoryService.allInstances(FakeDataDemoObjectWithAll.class);
    }

    @ActionLayout(sequence = "2")
    public FakeDataDemoObjectWithAll createDemoObjectWithAll(
            final String name,
            final boolean someBoolean,
            final char someChar,
            final byte someByte,
            final short someShort,
            final int someInt,
            final long someLong,
            final float someFloat,
            final double someDouble) {
        val obj = new FakeDataDemoObjectWithAll(name);
        obj.setSomeBoolean(someBoolean);
        obj.setSomeChar(someChar);
        obj.setSomeByte(someByte);
        obj.setSomeShort(someShort);
        obj.setSomeInt(someInt);
        obj.setSomeLong(someLong);
        obj.setSomeFloat(someFloat);
        obj.setSomeDouble(someDouble);
        repositoryService.persist(obj);
        return obj;
    }

    @Inject RepositoryService repositoryService;

}
