package org.apache.isis.sessionlog.applib.dom;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.session.SessionLogService;
import org.apache.isis.core.config.IsisConfiguration;

import lombok.NonNull;
import lombok.val;

/**
 * Provides supporting functionality for querying {@link SessionLogEntry session log entry} entities.
 */
public abstract class SessionLogEntryRepository<E extends SessionLogEntry> {

    @Inject RepositoryService repositoryService;
    @Inject FactoryService factoryService;

    private final Class<E> sessionLogEntryClass;

    protected SessionLogEntryRepository(@NonNull Class<E> sessionLogEntryClass) {
        this.sessionLogEntryClass = sessionLogEntryClass;
    }

    public void logoutAllSessions(final Timestamp logoutTimestamp) {
        val allSessions = repositoryService.allMatches(
                Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_ACTIVE_SESSIONS));
        for (val activeEntry : allSessions) {
            activeEntry.setCausedBy(SessionLogService.CausedBy.RESTART);
            activeEntry.setLogoutTimestamp(logoutTimestamp);
        }
    }

    public SessionLogEntry create(
            final String username,
            final UUID sessionGuid,
            final String httpSessionId,
            final SessionLogService.CausedBy causedBy,
            final Timestamp timestamp) {
        E entry = factoryService.detachedEntity(sessionLogEntryClass);
        entry.setUsername(username);
        entry.setSessionGuidStr(sessionGuid.toString());
        entry.setHttpSessionId(httpSessionId);
        entry.setCausedBy(causedBy);
        entry.setLoginTimestamp(timestamp);
        return repositoryService.persistAndFlush(entry);
    }


    public Optional<E> findBySessionGuid(final UUID sessionUuid) {
        return findBySessionGuidStr(sessionUuid.toString());
    }


    public Optional<E> findBySessionGuidStr(final String sessionGuidStr) {
        return repositoryService.firstMatch(
                Query.named(sessionLogEntryClass,  SessionLogEntry.Nq.FIND_BY_SESSION_GUID_STR)
                     .withParameter("sessionGuidStr", sessionGuidStr));
    }


    public Optional<E> findByHttpSessionId(final String httpSessionId) {
        return repositoryService.firstMatch(
                Query.named(sessionLogEntryClass,  SessionLogEntry.Nq.FIND_BY_HTTP_SESSION_ID)
                     .withParameter("httpSessionId", httpSessionId));
    }


    public List<E> findByUsername(final String username) {
        return repositoryService.allMatches(
                Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USERNAME)
                     .withParameter("username", username));
    }


    public List<E> findByUsernameAndFromAndTo(
            final String username,
            final LocalDate from,
            final LocalDate to) {
        val fromTs = toTimestampStartOfDayWithOffset(from, 0);
        val toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<E> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USERNAME_AND_TIMESTAMP_BETWEEN)
                        .withParameter("username", username)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USERNAME_AND_TIMESTAMP_AFTER)
                        .withParameter("username", username)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USERNAME_AND_TIMESTAMP_BEFORE)
                        .withParameter("username", username)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USERNAME)
                        .withParameter("username", username);
            }
        }
        return repositoryService.allMatches(query);
    }


    public List<E> findByFromAndTo(
            final LocalDate from,
            final LocalDate to) {
        val fromTs = toTimestampStartOfDayWithOffset(from, 0);
        val toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<E> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_TIMESTAMP_BETWEEN)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_TIMESTAMP_AFTER)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_TIMESTAMP_BEFORE)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND);
            }
        }
        return repositoryService.allMatches(query);
    }


    public List<E> findByUsernameAndStrictlyBefore(
            final String username,
            final Timestamp from) {

        return repositoryService.allMatches(
                Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USERNAME_AND_TIMESTAMP_STRICTLY_BEFORE)
                    .withParameter("username", username)
                    .withParameter("from", from));
    }


    public List<E> findByUsernameAndStrictlyAfter(
            final String username,
            final Timestamp from) {
        return repositoryService.allMatches(
                Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USERNAME_AND_TIMESTAMP_STRICTLY_AFTER)
                    .withParameter("username", username)
                    .withParameter("from", from));
    }



    public List<E> findActiveSessions() {
        return repositoryService.allMatches(
                Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_ACTIVE_SESSIONS));
    }



    public List<E> findRecentByUsername(final String username) {
        return repositoryService.allMatches(
                Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_RECENT_BY_USERNAME)
                        .withParameter("username", username)
                        .withLimit(10));

    }

    private static Timestamp toTimestampStartOfDayWithOffset(final LocalDate dt, final int daysOffset) {
        return dt!=null
                ?new Timestamp(dt.toDateTimeAtStartOfDay().plusDays(daysOffset).getMillis())
                :null;
    }

}
