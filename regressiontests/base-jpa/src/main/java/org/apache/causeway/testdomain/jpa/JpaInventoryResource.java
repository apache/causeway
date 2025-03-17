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
package org.apache.causeway.testdomain.jpa;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.xml.bind.JAXBException;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.testdomain.jpa.entities.JpaBook;
import org.apache.causeway.testdomain.jpa.entities.JpaProduct;
import org.apache.causeway.testdomain.util.dto.BookDto;

import lombok.RequiredArgsConstructor;

@Named("testdomain.jpa.InventoryResource")
@DomainService
@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class JpaInventoryResource {

    final RepositoryService repository;

    @Action
    public List<JpaProduct> listProducts() {
        return repository.allInstances(JpaProduct.class);
    }

    @Action
    public List<JpaBook> listBooks() {
        return repository.allInstances(JpaBook.class);
    }

    @Action
    public JpaBook recommendedBookOfTheWeek() {
        // for this test we do not care if we generate duplicates
        var book = JpaBook.of("Book of the week", "An awesome Book", 12, "Author", "ISBN", "Publisher");
        return repository.persist(book);
    }

    @Action
    public List<JpaBook> multipleBooks(

            @ParameterLayout(named = "") final
            int nrOfBooks

            ) {

        var books = _Lists.<JpaBook>newArrayList();

        // for this test we do not care if we generate duplicates
        for(int i=0; i<nrOfBooks; ++i) {
            var book = JpaBook.of("MultipleBooksTest", "An awesome Book["+i+"]", 12, "Author", "ISBN", "Publisher");
            books.add(repository.persist(book));
        }
        return books;
    }

    @Action //TODO improve the REST client such that the param can be of type Book
    public JpaBook storeBook(final String newBook) throws JAXBException {
        var book = JpaBook.fromDto(BookDto.decode(newBook));
        return repository.persist(book);
    }

    // -- NON - ENTITIES

    @Action
    public String httpSessionInfo() {

        // when running with basic-auth strategy, we don't want to create HttpSessions at all
        // however, this isn't the case if UserService is in use, as that _dpes_
        // use HttpSession to hold any impersonated user.

        var servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        var httpSession = servletRequestAttributes.getRequest().getSession(false);
        if(httpSession==null) {
            return "no http-session";
        }
        var sessionAttributeNames = _NullSafe.stream(httpSession.getAttributeNames())
        .collect(Collectors.joining(","));

        return String.format("http-session attribute names: {%s}", sessionAttributeNames);
    }

    @Action
    public BookDto recommendedBookOfTheWeekAsDto() {
        // for this test we do not care if we generate duplicates
        var book = JpaBook.of("Book of the week", "An awesome Book", 12, "Author", "ISBN", "Publisher");
        return BookDto.from(book);
    }

    @Action
    public List<BookDto> multipleBooksAsDto(

            @ParameterLayout(named = "") final
            int nrOfBooks

            ) {

        var books = _Lists.<BookDto>newArrayList();

        // for this test we do not care if we generate duplicates
        for(int i=0; i<nrOfBooks; ++i) {
            var book = JpaBook.of("MultipleBooksTest", "An awesome Book["+i+"]", 12, "Author", "ISBN", "Publisher");
            books.add(BookDto.from(book));
        }
        return books;
    }

}
