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
package org.apache.isis.testdomain.jpa;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.testdomain.jpa.entities.JpaBook;
import org.apache.isis.testdomain.jpa.entities.JpaInventory;
import org.apache.isis.testdomain.jpa.entities.JpaProduct;
import org.apache.isis.testdomain.util.dto.BookDto;

import lombok.val;

@Service
public class JpaTestFixtures {

    @Inject private RepositoryService repository;
    @Inject private FactoryService factoryService;
    @Inject private BookmarkService bookmarkService;

    public void cleanUpRepository() {
        repository.allInstances(JpaInventory.class).forEach(repository::remove);
        repository.allInstances(JpaBook.class).forEach(repository::remove);
        repository.allInstances(JpaProduct.class).forEach(repository::remove);
    }

    public void setUp3Books() {

        cleanUpRepository();
        // given - expected pre condition: no inventories
        assertEquals(0, repository.allInstances(JpaInventory.class).size());

        // setup sample Inventory with 3 Books
        SortedSet<JpaProduct> products = new TreeSet<>();

        BookDto.samples()
        .map(JpaBook::fromDto)
        .forEach(products::add);

        val inventory = new JpaInventory("Sample Inventory", products);
        repository.persistAndFlush(inventory);
    }

    public void addABookTo(final JpaInventory inventory) {
        inventory.getProducts()
        .add(JpaBook.of("Sample Book-1", "A sample book for testing.", 39., "Sample Author", "ISBN-A",
                "Sample Publisher"));
    }

    public JpaInventoryJaxbVm setUpViewmodelWith3Books() {
        val inventoryJaxbVm = factoryService.viewModel(new JpaInventoryJaxbVm());
        val books = inventoryJaxbVm.listBooks();
        val favoriteBook = books.get(0);
        inventoryJaxbVm.setName("Bookstore");
        inventoryJaxbVm.setBooks(books);
        inventoryJaxbVm.setFavoriteBook(favoriteBook);
        return inventoryJaxbVm;
    }

    // -- ASSERTIONS

    public void assertInventoryHasBooks(
            final Collection<? extends JpaProduct> products,
            final int...expectedBookIndices) {

        final int[] zeroBasedIndices = IntStream.of(expectedBookIndices)
                .map(i->i-1)
                .toArray();

        val expectedBookNames = BookDto.samples().collect(Can.toCan())
        .pickByIndex(zeroBasedIndices)
        .map(BookDto::getName)
        .sorted(_Strings::compareNullsFirst)
        .toArray(new String[0]);

        val actualBookNames = products.stream()
                .map(JpaProduct::getName)
                .sorted()
                .toArray();

        assertArrayEquals(expectedBookNames, actualBookNames);
    }

    public void assertPopulatedWithDefaults(
            final JpaInventoryJaxbVm inventoryJaxbVm) {

        assertEquals("JpaInventoryJaxbVm; Bookstore; 3 products", inventoryJaxbVm.title());
        assertEquals("Bookstore", inventoryJaxbVm.getName());
        val books = inventoryJaxbVm.listBooks();
        assertEquals(3, books.size());
        val favoriteBook = inventoryJaxbVm.getFavoriteBook();
        val expectedBook = BookDto.sample();
        assertEquals(expectedBook.getName(), favoriteBook.getName());
        assertHasPersistenceId(favoriteBook);
        inventoryJaxbVm.listBooks()
        .forEach(this::assertHasPersistenceId);
    }

    public void assertHasPersistenceId(final Object entity) {
        val bookmark = bookmarkService.bookmarkForElseFail(entity);
        final int id = Integer.parseInt(bookmark.getIdentifier());
        assertTrue(id>0, ()->String.format("expected valid id; got %d", id));
        //System.err.printf("%s%n", bookmark);
    }


}
