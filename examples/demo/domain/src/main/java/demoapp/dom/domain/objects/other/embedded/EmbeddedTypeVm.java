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
package demoapp.dom.domain.objects.other.embedded;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotations.Collection;
import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.applib.annotations.ObjectSupport;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.other.embedded.persistence.NumberConstantEntity;

// tag::class[]
@DomainObject(nature=Nature.VIEW_MODEL, logicalTypeName = "demo.EmbeddedTypeVm")
public class EmbeddedTypeVm implements HasAsciiDocDescription {

    // ...
// end::class[]
    @Inject private ValueHolderRepository<ComplexNumber, ? extends NumberConstantEntity> numberConstantRepo;

    @ObjectSupport public String title() {
        return "Embedded Types";
    }

// tag::class[]
    @Collection
    public List<? extends NumberConstantEntity> getAllConstants(){
        return numberConstantRepo.all();
    }
}
// end::class[]
