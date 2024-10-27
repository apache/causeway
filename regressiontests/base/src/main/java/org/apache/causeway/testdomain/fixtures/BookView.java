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
package org.apache.causeway.testdomain.fixtures;

import java.util.Optional;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.testdomain.util.dto.IBook;

/**
 * Simply acts as wrapper of a book entity.
 * <p>
 * Introduced to test whether view-models that have references to entities,
 * do properly re-fetch those (particularly on AJAX requests).
 */
@DomainObject(nature = Nature.VIEW_MODEL)
@DomainObjectLayout(cssClassFa = "book-bookmark")
public interface BookView<T extends IBook> {

    @ObjectSupport
    default public String title() {
        return String.format("%s (%s)", getName(), getEntityState());
    }

    @Property
    @PropertyLayout(hidden = Where.EVERYWHERE)
    T getBook();

    @Property
    public default String getName() {
        return Optional.ofNullable(getBook())
                .map(IBook::getName)
                .orElse("no book referenced");
    }

    @Property
    public default String getEntityState() {
        return Optional.ofNullable(getBook())
                .map(book->{
                    var spec = specLoader().specForTypeElseFail(getBook().getClass());
                    var entityFacet = spec.entityFacetElseFail();
                    var entityState = entityFacet.getEntityState(book);
                    return entityState.name();
                })
                .orElse("no book referenced");
    }

    @Programmatic
    SpecificationLoader specLoader();

}