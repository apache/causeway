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

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

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
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Oneshot;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.base._Refs.BooleanAtomicReference;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.testdomain.util.dto.BookDto;
import org.apache.causeway.testdomain.util.dto.IBook;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

public abstract class EntityTestFixtures implements MetamodelListener {

    @Inject protected RepositoryService repository;
    @Inject protected FactoryService factoryService;
    @Inject protected BookmarkService bookmarkService;
    @Inject protected InteractionService interactionService;

    @Override
    public final void onMetamodelLoaded() {
        install();
    }

    public final <B extends IBook, T extends InventoryJaxbVm<B>> T setUpViewmodelWith3Books() {
        final T inventoryJaxbVm =
                _Casts.uncheckedCast(factoryService.viewModel(vmClass()));
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

    public final Bookmark getInventoryJaxbVmAsBookmark() {
        return bookmarkService.bookmarkForElseFail(setUpViewmodelWith3Books());
    }

    protected abstract Class<? extends InventoryJaxbVm<? extends IBook>> vmClass();
    protected abstract void clearRepository();
    protected abstract void setUp3Books();

    // -- ASSERTIONS

    public final void assertInventoryHasBooks(
            final Collection<?> products,
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
                .map(IBook.class::cast)
                .map(IBook::getName)
                .sorted()
                .toArray();

        assertArrayEquals(expectedBookNames, actualBookNames);
    }

    public final void assertHasPersistenceId(final Object entity) {
        val bookmark = bookmarkService.bookmarkForElseFail(entity);
        final int id = Integer.parseInt(bookmark.getIdentifier());
        assertTrue(id>=-1, ()->String.format("expected valid id; got %d", id));
    }

    public final void assertPopulatedWithDefaults(
            final InventoryJaxbVm inventoryJaxbVm) {
        assertEquals("*InventoryJaxbVm; Bookstore; 3 products", "*" + (inventoryJaxbVm.title().substring(3)));
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

    public static Set<String> expectedBookTitles() {
        val expectedTitles = Set.of("Dune", "The Foundation", "The Time Machine");
        return expectedTitles;
    }

    // -- LOCKING

    @RequiredArgsConstructor
    public static class Lock {
        private final _Oneshot release = new _Oneshot();
        private final EntityTestFixtures entityTestFixtures;
        public void release() {
            release.trigger(()->entityTestFixtures.release(this));
        }
    }

    @SneakyThrows
    public final Lock clearAndAquireLock() {
        Lock lock;
        lockQueue.put(lock = new Lock(this)); // put next lock on the queue; blocks until space available
        clear();
        return lock;
    }

    @SneakyThrows
    private void release(final Lock lock) {
        reinstall(()->{});
        lockQueue.take(); // remove lock from queue
    }

    // -- INSTALL

    public final void install(final Lock lock) {
        _Assert.assertEquals(lockQueue.peek(), lock);
        install();
    }

    public final void reinstall(final Runnable onBeforeInstall) {
        isInstalled.compute(isInst->{
            interactionService.runAnonymous(()->{
                clearRepository();
                onBeforeInstall.run();
                setUp3Books();
            });
            return false;
        });
    }

    // -- HELPER

    private BooleanAtomicReference isInstalled = _Refs.booleanAtomicRef(false);
    private LinkedBlockingQueue<Lock> lockQueue = new LinkedBlockingQueue<>(1);

    private void clear() {
        isInstalled.computeIfTrue(()->{
            interactionService.runAnonymous(()->{
                clearRepository();
            });
            return false;
        });
    }

    private void install() {
        isInstalled.computeIfFalse(()->{
            interactionService.runAnonymous(()->{
                setUp3Books();
            });
            return true;
        });
    }



}
