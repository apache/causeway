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
package org.apache.causeway.testdomain.persistence.jpa;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TransactionRequiredException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.persistence.jpa.applib.services.JpaSupportService;
import org.apache.causeway.testdomain.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.jpa.entities.JpaCallbackOrchestration;
import org.apache.causeway.testdomain.jpa.entities.JpaEntityNonGeneratedStringId;

@SpringBootTest(classes = Configuration_usingJpa.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:JpaPessimisticLockingTest;LOCK_TIMEOUT=10000",
        "eclipselink.cache.shared.default=true"
})
@TestPropertySource(CausewayPresets.UseLog4j2Test)
class JpaPessimisticLockingTest {
    @Inject private InteractionService interactions;
    @Inject private JpaSupportService jpa;
    @Inject private ObjectManager objects;
    @Inject private BookmarkService bookmarks;
    @Inject private SpecificationLoader specifications;
    @Inject private EntityManagerFactory emf;

    private ExecutorService worker;
    private String id;
    private String otherId;
    private Bookmark batchBookmark;
    private Bookmark otherBookmark;

    @BeforeEach
    void setUp() {
        worker = Executors.newSingleThreadExecutor();
        id = UUID.randomUUID().toString();
        otherId = UUID.randomUUID().toString();
        tx(() -> {
            final var em = jpa.getEntityManagerElseFail(JpaCallbackOrchestration.class);
            final var batch = new JpaCallbackOrchestration(id);
            final var other = new JpaCallbackOrchestration(otherId);
            em.persist(batch);
            em.persist(other);
            em.flush();
            batchBookmark = bookmarks.bookmarkFor(batch).orElseThrow();
            otherBookmark = bookmarks.bookmarkFor(other).orElseThrow();
            return null;
        });
        assertTrue(emf.getCache().contains(JpaCallbackOrchestration.class, id), "exercise shared-cache hits");
    }

    @AfterEach
    void tearDown() throws Exception {
        worker.shutdownNow();
        assertTrue(worker.awaitTermination(15, TimeUnit.SECONDS));
        tx(() -> {
            final var em = jpa.getEntityManagerElseFail(JpaCallbackOrchestration.class);
            em.remove(em.find(JpaCallbackOrchestration.class, id));
            em.remove(em.find(JpaCallbackOrchestration.class, otherId));
            return null;
        });
    }

    @Test
    void callbacksSerializeAndPublishCompletionOnce() throws Exception {
        final CountDownLatch attempting = new CountDownLatch(1);
        final Future<?> second = tx(() -> {
            final var first = load(id);
            first.onSuccess(0);
            final Future<?> future = worker.submit(() -> tx(() -> {
                attempting.countDown();
                final var next = load(id);
                assertEquals(1, next.getCompletedLines(), "waiter must see the committed first callback");
                next.onSuccess(1);
                return null;
            }));
            assertBlocked(attempting, future);
            return future;
        });
        second.get(15, TimeUnit.SECONDS);
        tx(() -> {
            final var batch = load(id);
            assertEquals(3, batch.getCompletedLines());
            assertEquals(1, batch.getCompletionEvents());
            batch.onSuccess(1); // repeated callback remains idempotent
            assertEquals(1, batch.getCompletionEvents());
            return null;
        });
    }

    @Test
    void rollbackReleasesLockWithoutPublishingChanges() throws Exception {
        final CountDownLatch attempting = new CountDownLatch(1);
        final java.util.concurrent.atomic.AtomicReference<Future<?>> second = new java.util.concurrent.atomic.AtomicReference<>();
        assertThrows(Rollback.class, () -> tx(() -> {
            load(id).onSuccess(0);
            final Future<?> future = worker.submit(() -> tx(() -> {
                attempting.countDown();
                final var batch = load(id);
                assertEquals(0, batch.getCompletedLines());
                batch.onSuccess(1);
                return null;
            }));
            second.set(future);
            assertBlocked(attempting, future);
            throw new Rollback();
        }));
        second.get().get(15, TimeUnit.SECONDS);
        tx(() -> {
            assertEquals(2, load(id).getCompletedLines());
            assertEquals(0, load(id).getCompletionEvents());
            return null;
        });
    }

    @Test
    void differentOrchestrationsDoNotShareALock() {
        tx(() -> {
            load(id);
            final Future<?> second = worker.submit(() -> tx(() -> {
                load(otherId).onSuccess(0);
                return null;
            }));
            try {
                second.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new AssertionError("different orchestration should proceed", e);
            }
            return null;
        });
    }

    @Test
    void firstLockCanRefreshPreviouslyUnlockedLocalChanges() {
        tx(() -> {
            final var existing = jpa.getEntityManagerElseFail(JpaCallbackOrchestration.class)
                    .find(JpaCallbackOrchestration.class, id);
            existing.onSuccess(0);
            assertSame(existing, load(id));
            assertEquals(0, existing.getCompletedLines(), "EclipseLink refreshes on first lock acquisition");
            return null;
        });
    }

    @Test
    void repeatedLockedLoadsPreserveCallbackChanges() {
        tx(() -> {
            final var existing = load(id);
            existing.onSuccess(0);
            assertSame(existing, load(id));
            assertEquals(1, existing.getCompletedLines());
            return null;
        });
    }

    @Test
    void firstLockRefreshesPreviouslyManagedStaleState() {
        tx(() -> {
            final var original = jpa.getEntityManagerElseFail(JpaCallbackOrchestration.class)
                    .find(JpaCallbackOrchestration.class, id);
            final Long previousVersion = original.getVersion();
            final Future<?> update = worker.submit(() -> tx(() -> {
                load(id).onSuccess(0);
                return null;
            }));
            try {
                update.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new AssertionError(e);
            }
            assertSame(original, load(id));
            assertEquals(1, original.getCompletedLines());
            assertTrue(original.getVersion() > previousVersion);
            return null;
        });
    }

    @Test
    void pessimisticLoadRequiresTransaction() {
        final var facet = specifications.loadSpecification(JpaCallbackOrchestration.class).entityFacetElseFail();
        assertThrows(TransactionRequiredException.class, () -> facet.fetchByBookmark(bookmark(id)));
    }

    @Test
    void defaultOptimisticLoadStillWorksWithoutTransaction() {
        final Bookmark bookmark = tx(() -> {
            final var entity = new JpaEntityNonGeneratedStringId(id);
            final var em = jpa.getEntityManagerElseFail(JpaEntityNonGeneratedStringId.class);
            em.persist(entity);
            em.flush();
            return bookmarks.bookmarkFor(entity).orElseThrow();
        });
        try {
            final var facet = specifications.loadSpecification(JpaEntityNonGeneratedStringId.class).entityFacetElseFail();
            assertTrue(facet.fetchByBookmark(bookmark).isPresent());
        } finally {
            tx(() -> {
                final var em = jpa.getEntityManagerElseFail(JpaEntityNonGeneratedStringId.class);
                em.remove(em.find(JpaEntityNonGeneratedStringId.class, id));
                return null;
            });
        }
    }

    private JpaCallbackOrchestration load(final String identifier) {
        return (JpaCallbackOrchestration) objects.loadObject(bookmark(identifier)).orElseThrow().getPojo();
    }

    private Bookmark bookmark(final String identifier) {
        return identifier.equals(id) ? batchBookmark : otherBookmark;
    }

    private <T> T tx(final Supplier<T> body) {
        return interactions.callAnonymous(body::get);
    }

    private void assertBlocked(final CountDownLatch attempting, final Future<?> future) {
        try {
            assertTrue(attempting.await(5, TimeUnit.SECONDS), "second callback must enter its transaction");
            assertThrows(TimeoutException.class, () -> future.get(300, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    private static class Rollback extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
