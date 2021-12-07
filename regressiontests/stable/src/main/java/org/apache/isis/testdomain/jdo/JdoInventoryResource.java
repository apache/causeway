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
package org.apache.isis.testdomain.jdo;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.jdo.entities.JdoProduct;
import org.apache.isis.testdomain.util.dto.BookDto;

import lombok.RequiredArgsConstructor;
import lombok.val;

@DomainService(
        nature = NatureOfService.REST,
        logicalTypeName = "testdomain.jdo.InventoryResource"
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class JdoInventoryResource {

    final RepositoryService repository;

    @Action
    public List<JdoProduct> listProducts() {
        return repository.allInstances(JdoProduct.class);
    }

    @Action
    public List<JdoBook> listBooks() {
        return repository.allInstances(JdoBook.class);
    }

    @Action
    public JdoBook recommendedBookOfTheWeek() {
        // for this test we do not care if we generate duplicates
        val book = JdoBook.of("Book of the week", "An awesome Book", 12, "Author", "ISBN", "Publisher");
        return repository.persist(book);
    }

    @Action
    public List<JdoBook> multipleBooks(

            @ParameterLayout(named = "") final
            int nrOfBooks

            ) {

        val books = _Lists.<JdoBook>newArrayList();

        // for this test we do not care if we generate duplicates
        for(int i=0; i<nrOfBooks; ++i) {
            val book = JdoBook.of("MultipleBooksTest", "An awesome Book["+i+"]", 12, "Author", "ISBN-"+i, "Publisher");
            books.add(repository.persist(book));
        }
        return books;
    }

    @Action //TODO improve the REST client such that the param can be of type Book
    public JdoBook storeBook(final String newBook) throws JAXBException {
        val book = JdoBook.fromDto(BookDto.decode(newBook));
        return repository.persist(book);
    }

    // -- NON - ENTITIES

    @Action
    public String httpSessionInfo() {

        // when running with basic-auth strategy, we don't want to create HttpSessions at all

        val servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        val httpSession = servletRequestAttributes.getRequest().getSession(false);
        if(httpSession==null) {
            return "no http-session";
        }
        val sessionAttributeNames = _NullSafe.stream(httpSession.getAttributeNames())
        .collect(Collectors.joining(","));

        return String.format("http-session attribute names: {%s}", sessionAttributeNames);
    }

    @Action
    public BookDto recommendedBookOfTheWeekAsDto() {
        // for this test we do not care if we generate duplicates
        val book = JdoBook.of("Book of the week", "An awesome Book", 12, "Author", "ISBN", "Publisher");
        return BookDto.from(book);
    }

    @Action
    public List<BookDto> multipleBooksAsDto(

            @ParameterLayout(named = "") final
            int nrOfBooks

            ) {

        val books = _Lists.<BookDto>newArrayList();

        // for this test we do not care if we generate duplicates
        for(int i=0; i<nrOfBooks; ++i) {
            val book = JdoBook.of("MultipleBooksTest", "An awesome Book["+i+"]", 12, "Author", "ISBN", "Publisher");
            books.add(BookDto.from(book));
        }
        return books;
    }


}
