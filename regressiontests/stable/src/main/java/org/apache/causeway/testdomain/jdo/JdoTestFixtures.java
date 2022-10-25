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

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.events.metamodel.MetamodelListener;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Oneshot;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.base._Refs.BooleanAtomicReference;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.testdomain.jdo.entities.JdoBook;
import org.apache.causeway.testdomain.jdo.entities.JdoInventory;
import org.apache.causeway.testdomain.jdo.entities.JdoProduct;
import org.apache.causeway.testdomain.util.dto.BookDto;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@Service
public class JdoTestFixtures implements MetamodelListener {

    @Inject private RepositoryService repository;
    @Inject private FactoryService factoryService;
    @Inject private BookmarkService bookmarkService;
    @Inject private InteractionService interactionService;

    @RequiredArgsConstructor
    public static class Lock {
        private final _Oneshot release = new _Oneshot();
        private final JdoTestFixtures jdoTestFixtures;
        public void release() {
            release.trigger(()->jdoTestFixtures.release(this));
        }
    }

    private BooleanAtomicReference isInstalled = _Refs.booleanAtomicRef(false);
    private LinkedBlockingQueue<Lock> lockQueue = new LinkedBlockingQueue<>(1);

    @Override
    public void onMetamodelLoaded() {
        install();
    }

    public void install(final Lock lock) {
        _Assert.assertEquals(lockQueue.peek(), lock);
        install();
    }

    public void reinstall(final Runnable onBeforeInstall) {
        isInstalled.compute(isInst->{
            interactionService.runAnonymous(()->{
                cleanUpRepository();
                onBeforeInstall.run();
                setUp3Books();
            });
            return false;
        });
    }

    @SneakyThrows
    public Lock clearAndAquireLock() {
        Lock lock;
        lockQueue.put(lock = new Lock(this)); // put next lock on the queue; blocks until space available
        clear();
        return lock;
    }

    @SneakyThrows
    void release(final Lock lock) {
        reinstall(()->{});
        lockQueue.take(); // remove lock from queue
    }

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

    public void assertInventoryHasBooks(
            final Collection<? extends JdoProduct> products,
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
                .map(JdoProduct::getName)
                .sorted()
                .toArray();

        assertArrayEquals(expectedBookNames, actualBookNames);
    }

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

    public void assertHasPersistenceId(final Object entity) {
        val bookmark = bookmarkService.bookmarkForElseFail(entity);
        final int id = Integer.parseInt(bookmark.getIdentifier());
        assertTrue(id>-2, ()->String.format("expected valid id; got %d", id));
        //System.err.printf("%s%n", bookmark);
    }

    public static Set<String> expectedBookTitles() {
        val expectedTitles = Set.of("Dune", "The Foundation", "The Time Machine");
        return expectedTitles;
    }

    // -- HELPER

    private void clear() {
        isInstalled.computeIfTrue(()->{
            interactionService.runAnonymous(()->{
                cleanUpRepository();
            });
            return false;
        });
    }

    private void install() {
        isInstalled.computeIfFalse(()->{
            interactionService.runAnonymous(()->{
                cleanUpRepository();
                setUp3Books();
            });
            return true;
        });
    }

    private void cleanUpRepository() {
        repository.allInstances(JdoInventory.class).forEach(repository::remove);
        repository.allInstances(JdoBook.class).forEach(repository::remove);
        repository.allInstances(JdoProduct.class).forEach(repository::remove);
    }

    private void setUp3Books() {

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
