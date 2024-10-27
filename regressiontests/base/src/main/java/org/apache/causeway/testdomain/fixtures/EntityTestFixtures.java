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
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.springframework.beans.factory.DisposableBean;

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
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Oneshot;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.base._Refs.BooleanAtomicReference;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.datasources.DataSourceIntrospectionService;
import org.apache.causeway.testdomain.util.dto.BookDto;
import org.apache.causeway.testdomain.util.dto.IBook;
import org.apache.causeway.testdomain.util.kv.KVStoreForTesting;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class EntityTestFixtures
implements
    MetamodelListener,
    DisposableBean {

    @Inject protected RepositoryService repository;
    @Inject protected FactoryService factoryService;
    @Inject protected BookmarkService bookmarkService;
    @Inject protected InteractionService interactionService;
    @Inject protected DataSourceIntrospectionService dataSourceIntrospectionService;
    @Inject protected KVStoreForTesting kvStoreForTesting;

    //private Lock myLock;

    protected abstract Class<? extends InventoryJaxbVm<? extends IBook>> vmClass();

    @Override
    public final void onMetamodelLoaded() {
        // -- disabled
    }

    @Override
    public void destroy() {
        try {
            if(lockQueue!=null) {
                lockQueue.clear();
            }
        } finally {
            lockQueue=null;
        }
    }

    public final <B extends IBook, T extends InventoryJaxbVm<B>> T createViewmodelWithCurrentBooks() {
        final T inventoryJaxbVm =
                _Casts.uncheckedCast(factoryService.viewModel(vmClass()));
        var books = inventoryJaxbVm.listBooks();
        if(_NullSafe.size(books)>0) {
            var favoriteBook = books.get(0);
            inventoryJaxbVm.setName("Bookstore");
            inventoryJaxbVm.setBooks(books);
            inventoryJaxbVm.setFavoriteBook(favoriteBook);
            inventoryJaxbVm.setBooksForTab1(books.stream().skip(1).collect(Collectors.toList()));
            inventoryJaxbVm.setBooksForTab2(books.stream().limit(2).collect(Collectors.toList()));
        }
        return inventoryJaxbVm;
    }

    public final Bookmark getInventoryJaxbVmAsBookmark() {
        return bookmarkService.bookmarkForElseFail(createViewmodelWithCurrentBooks());
    }

    /** usable iff a transactional context is provided by the caller */
    public abstract void clearRepository();
    /** usable iff a transactional context is provided by the caller */
    public abstract void add3Books();
    public abstract Object addBook(BookDto bookDto);
    public abstract void addInventory(Set<?> books);

    public void clearRepositoryInBulk() {
        repository.execInBulk(()->{
            clearRepository();
            return null;
        });
    }

    public void add3BooksInBulk() {
        repository.execInBulk(()->{
            addInventory(BookDto.samples()
                    .sorted(Comparator.comparing(BookDto::getName))
                    .map(this::addBook)
                    .collect(Collectors.toSet()));
            return null;
        });
    }

    // -- ASSERTIONS

    public final void assertInventoryHasBooks(
            final Collection<?> products,
            final int...expectedBookIndices) {

        final int[] zeroBasedIndices = IntStream.of(expectedBookIndices)
                .map(i->i-1)
                .toArray();

        var expectedBookNames = BookDto.samples().collect(Can.toCan())
        .pickByIndex(zeroBasedIndices)
        .map(BookDto::getName)
        .sorted(_Strings::compareNullsFirst)
        .toArray(new String[0]);

        var actualBookNames = products.stream()
                .map(IBook.class::cast)
                .map(IBook::getName)
                .sorted()
                .toArray();

        assertArrayEquals(expectedBookNames, actualBookNames);
    }

    public final void assertHasPersistenceId(final Object entity) {
        var bookmark = bookmarkService.bookmarkForElseFail(entity);
        final int id = Integer.parseInt(bookmark.getIdentifier());
        assertTrue(id>=-1, ()->String.format("expected valid id; got %d", id));
    }

    public final void assertPopulatedWithDefaults(
            final InventoryJaxbVm<? extends IBook> inventoryJaxbVm) {
        assertEquals("*InventoryJaxbVm; Bookstore; 3 products", "*" + (inventoryJaxbVm.title().substring(3)));
        assertEquals("Bookstore", inventoryJaxbVm.getName());
        var books = inventoryJaxbVm.listBooks();
        assertEquals(3, books.size());
        var favoriteBook = inventoryJaxbVm.getFavoriteBook();
        var expectedBook = BookDto.sample();
        assertEquals(expectedBook.getName(), favoriteBook.getName());
        assertHasPersistenceId(favoriteBook);
        inventoryJaxbVm.listBooks()
        .forEach(this::assertHasPersistenceId);
    }

    public static Set<String> expectedBookTitles() {
        var expectedTitles = Set.of("Dune", "The Foundation", "The Time Machine");
        return expectedTitles;
    }

    // -- LOCKING

    @RequiredArgsConstructor
    public static class Lock {
        private final _Oneshot release = new _Oneshot();
        private final String dsUrl;
        private final EntityTestFixtures entityTestFixtures;
        public void install() {
            entityTestFixtures.install(this);
        }
        public void release() {
            release.trigger(()->entityTestFixtures.release(this));
        }
    }

    @SneakyThrows
    public final synchronized Lock aquireLock() {
        var dsUrl = dataSourceIntrospectionService.getDataSourceInfos().getFirstElseFail().getJdbcUrl();
        this.lockQueue = lockQueueByDatasource.computeIfAbsent(dsUrl, __->new LinkedBlockingQueue<>(1));
        log.info("waiting for lock {}", dsUrl);
        Lock lock;
        lockQueue.put(lock = new Lock(dsUrl, this)); // put next lock on the queue; blocks until space available
        log.info("lock aquired for {}", dsUrl);
        initSchema();
        kvStoreForTesting.clearValues();
        return lock;
    }

    /** XXX sporadically seeing errors like
     * ----
     * Error: (bad sql grammar exception): Column "INVENTORY_ID_EID" not found; SQL statement:
     * INSERT INTO "testdomain"."JdoProduct"
     * ("id","description","name","price","author","isbn","publisher","DISCRIMINATOR","INVENTORY_ID_EID")
     * VALUES (?,?,?,?,?,?,?,?,?) [42122-214]
     * ----
     * This is an attempt to force the JDO schema to properly initialize. */
    protected void initSchema() {};

    @SneakyThrows
    public final Lock aquireLockAndClear() {
        var lock = aquireLock();
        clearRepositoryInTransaction();
        kvStoreForTesting.clearValues();
        return lock;
    }

    @SneakyThrows
    private void release(final Lock lock) {
        log.info("about to release lock {}", lock.dsUrl);
        try {
            clearRepositoryInTransaction();
        } finally {
            lockQueue.take(); // remove lock from queue
            log.info("lock released {}", lock.dsUrl);
        }
    }

    // -- INSTALL FIXTURE

    public final void resetTo3Books(final Runnable onBeforeInstall) {
        isInstalled.compute(isInst->{
            interactionService.runAnonymous(()->{
                clearRepositoryInBulk();
                onBeforeInstall.run();
                add3BooksInBulk();
            });
            return false;
        });
    }

    // -- HELPER

    private final BooleanAtomicReference isInstalled = _Refs.booleanAtomicRef(false);
    private final static Map<String, LinkedBlockingQueue<Lock>> lockQueueByDatasource = new ConcurrentHashMap<>();
    private LinkedBlockingQueue<Lock> lockQueue;

    private void clearRepositoryInTransaction() {
        isInstalled.computeIfTrue(()->{
            interactionService.runAnonymous(()->{
                clearRepository();
            });
            return false;
        });
    }

    private void install(final @NonNull Lock lock) {
        try {
            interactionService.runAnonymous(()->{
                clearRepository();
                add3Books();
            });
        } catch (Exception ex) {
            lock.release();
        }
    }

//    private void add3BooksInTransaction() {
//        isInstalled.computeIfFalse(()->{
//            interactionService.runAnonymous(()->{
//                add3Books();
//            });
//            return true;
//        });
//    }

}
