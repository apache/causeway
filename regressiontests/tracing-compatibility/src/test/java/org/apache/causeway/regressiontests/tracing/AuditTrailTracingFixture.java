/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.regressiontests.tracing;

import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.UUID;

import static org.mockito.Mockito.*;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.AuditTrail.PersistPolicy;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntryRepository;
import org.apache.causeway.extensions.audittrail.applib.spiimpl.EntityPropertyChangeSubscriberForAuditTrail;

/** Actual subscriber with controlled persistence, not an audit entity ORM integration test. */
final class AuditTrailTracingFixture {
    static void write(final CausewayObservationIntegration integration,
            final boolean agent, final boolean enabled, final boolean fail) {
        var repository = mock(AuditTrailEntryRepository.class);
        var config = mock(CausewayConfiguration.class, RETURNS_DEEP_STUBS);
        when(config.extensions().auditTrail().persist()).thenReturn(enabled ? PersistPolicy.ENABLED : PersistPolicy.DISABLED);
        var change = EntityPropertyChange.of(UUID.randomUUID(), 1,
                Bookmark.forLogicalTypeNameAndIdentifier("sentinel.AuditTarget", "sentinel-bookmark"),
                "sentinel.AuditTarget#secret", "secret", "sentinel-before", "sentinel-after",
                "sentinel-audit-user", new Timestamp(0));
        var changes = Can.of(change, change);
        Runnable sql = () -> {
            if (agent) executeSql();
            else integration.createNotStarted(AuditTrailTracingFixture.class, "fixture.audit.jdbc")
                    .observe(AuditTrailTracingFixture::executeSql);
        };
        when(repository.createFor(change)).thenAnswer(call -> {
            sql.run();
            return null;
        });
        var failure = new IllegalStateException("audit fixture failure");
        when(repository.createFor(changes)).thenAnswer(call -> {
            sql.run();
            if (fail) throw failure;
            return Can.empty();
        });
        var subscriber = new EntityPropertyChangeSubscriberForAuditTrail(
                mock(TransactionService.class), repository, config, integration);
        var previous = integration.observationRegistry().getCurrentObservation();
        try {
            subscriber.onChanging(change);
            subscriber.onChanging(changes);
            if (enabled && fail) throw new AssertionError("missing repository failure");
        } catch (IllegalStateException ex) {
            if (ex != failure) throw new AssertionError("repository failure was replaced", ex);
            throw ex;
        } finally {
            if (previous != integration.observationRegistry().getCurrentObservation())
                throw new AssertionError("audit scope not restored");
            if (!enabled) verifyNoInteractions(repository);
            else {
                verify(repository).createFor(change);
                verify(repository).createFor(changes);
                verifyNoMoreInteractions(repository);
            }
        }
    }

    private static void executeSql() {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:audit;DB_CLOSE_DELAY=-1");
                var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS audit_fixture (id INT)");
            statement.executeUpdate("INSERT INTO audit_fixture VALUES (1)");
        } catch (java.sql.SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
