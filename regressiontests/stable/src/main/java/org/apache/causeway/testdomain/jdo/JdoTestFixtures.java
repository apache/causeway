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

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.testdomain.fixtures.EntityTestFixtures;
import org.apache.causeway.testdomain.jdo.entities.JdoBook;
import org.apache.causeway.testdomain.jdo.entities.JdoInventory;
import org.apache.causeway.testdomain.jdo.entities.JdoProduct;
import org.apache.causeway.testdomain.util.dto.BookDto;

import lombok.val;

@Service
public class JdoTestFixtures extends EntityTestFixtures {

    public void addABookTo(final JdoInventory inventory) {
        inventory.getProducts()
        .add(JdoBook.of("Sample Book-1", "A sample book for testing.", 39., "Sample Author", "ISBN-A",
                "Sample Publisher"));
    }

    public Bookmark getJdoInventoryJaxbVmAsBookmark() {
        return bookmarkService.bookmarkForElseFail(setUpViewmodelWith3Books());
    }

    public JdoInventoryJaxbVm setUpViewmodelWith3Books() {
        val inventoryJaxbVm = factoryService.viewModel(new JdoInventoryJaxbVm());
        val books = inventoryJaxbVm.listBooks();
        if(_NullSafe.size(books)>0) {
            val favoriteBook = books.get(0);
            inventoryJaxbVm.setName("Bookstore");
            inventoryJaxbVm.setBooks(books);
            inventoryJaxbVm.setFavoriteBook(favoriteBook);
            inventoryJaxbVm.setBooksForTab1(books.stream().skip(1).collect(Collectors.toList()));
            inventoryJaxbVm.setBooksForTab2(books.stream().limit(2).collect(Collectors.toList()));
        }
        return inventoryJaxbVm;
    }

    // -- ASSERTIONS

    public void assertPopulatedWithDefaults(
            final JdoInventoryJaxbVm inventoryJaxbVm) {
        assertEquals("JdoInventoryJaxbVm; Bookstore; 3 products", inventoryJaxbVm.title());
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

    // -- HELPER

    @Override
    protected void clearRepository() {
        repository.allInstances(JdoInventory.class).forEach(repository::remove);
        repository.allInstances(JdoBook.class).forEach(repository::remove);
        repository.allInstances(JdoProduct.class).forEach(repository::remove);
    }

    @Override
    protected void setUp3Books() {

        // given - expected pre condition: no inventories
        assertEquals(0, repository.allInstances(JdoInventory.class).size());

        // setup sample Inventory with 3 Books
        SortedSet<JdoProduct> products = new TreeSet<>();

        BookDto.samples()
        .map(JdoBook::fromDto)
        .forEach(products::add);

        val inventory = JdoInventory.of("Sample Inventory", products);
        repository.persistAndFlush(inventory);
    }

}
