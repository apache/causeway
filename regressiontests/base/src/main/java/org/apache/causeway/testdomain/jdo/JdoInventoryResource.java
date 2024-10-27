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
package org.apache.causeway.testdomain.jdo;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBException;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.extensions.fullcalendar.applib.value.CalendarEvent;
import org.apache.causeway.testdomain.jdo.entities.JdoBook;
import org.apache.causeway.testdomain.jdo.entities.JdoProduct;
import org.apache.causeway.testdomain.util.dto.BookDto;

import lombok.RequiredArgsConstructor;

@Named("testdomain.jdo.InventoryResource")
@DomainService(
        aliased = "testdomain.jdo.InventoryResourceAlias" // <-- as tested with RestEndpointService
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class JdoInventoryResource {

    final RepositoryService repository;
    final FactoryService factoryService;
    final JdoTestFixtures jdoTestFixtures;

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
        return repository.persist(
                JdoBook.fromDto(recommendedBookOfTheWeekDto()));
    }

    @Action // REST stress test action
    public BookDto recommendedBookOfTheWeekDto() {
        return BookDto.builder()
                .name("Book of the week")
                .description("An awesome Book")
                .price(12)
                .author("Author")
                .isbn("ISBN")
                .publisher("Publisher")
                .build();
    }

    @Action
    public List<JdoBook> multipleBooks(
            @ParameterLayout(named = "")
            final int nrOfBooks) {
        return listBooks();
    }

    @Action //XXX improve the REST client such that the param can be of type JdoBook?
    public JdoBook storeBook(final String newBook) throws JAXBException {
        var book = JdoBook.fromDto(BookDto.decode(newBook));
        return repository.persist(book);
    }

    @Action // echos given CalendarEvent (composite value type test)
    public CalendarEvent echoCalendarEvent(final CalendarEvent calendarEvent) throws JAXBException {
        return calendarEvent;
    }

    // -- NON - ENTITIES

    @Action
    public String httpSessionInfo() {

        // when running with basic-auth strategy, we don't want to create HttpSessions at all

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
        var book = JdoBook
                .of("Book of the week", "An awesome Book", 12, "Author", "ISBN", "Publisher");
        return BookDto.from(book);
    }

    @Action
    public List<BookDto> multipleBooksAsDto(
            @ParameterLayout(named = "") final
            int nrOfBooks) {

        var books = _Lists.<BookDto>newArrayList();
        createMultipleBooks("MultipleBooksAsDtoTest", nrOfBooks, newBook->books.add(BookDto.from(newBook)));
        return books;
    }

    @Action
    public JdoInventoryJaxbVm inventoryAsJaxbVm() {
        var inventoryJaxbVm = factoryService.viewModel(new JdoInventoryJaxbVm());
        var books = listBooks();
        if(_NullSafe.size(books)>0) {
            inventoryJaxbVm.setName("Bookstore");
            inventoryJaxbVm.setBooks(books);
            inventoryJaxbVm.setFavoriteBook(books.get(0));
        }
        return inventoryJaxbVm;
    }

    // -- HELPER

    private List<JdoBook> createMultipleBooks(
            final String bookTitle,
            final int nrOfBooks,
            final Consumer<JdoBook> onNewBook) {

        var books = _Lists.<JdoBook>newArrayList();

        // for this test we do not care if we generate duplicates
        for(int i=0; i<nrOfBooks; ++i) {
            var book = JdoBook
                    .of(bookTitle, "An awesome Book["+i+"]", 12, "Author", "ISBN-"+i, "Publisher");
            onNewBook.accept(book);
            books.add(book);
        }
        return books;
    }

}
