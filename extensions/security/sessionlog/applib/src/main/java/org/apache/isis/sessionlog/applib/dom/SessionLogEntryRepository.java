package org.apache.isis.sessionlog.applib.dom;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.session.SessionLogService;

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
            final String sessionId,
            final SessionLogService.CausedBy causedBy,
            final Timestamp timestamp) {
        E entry = factoryService.detachedEntity(sessionLogEntryClass);
        entry.setUsername(username);
        entry.setSessionId(sessionId);
        entry.setCausedBy(causedBy);
        entry.setLoginTimestamp(timestamp);
        return repositoryService.persistAndFlush(entry);
    }

    public Optional<E> findBySessionId(final String sessionId) {
        return repositoryService.firstMatch(
                Query.named(sessionLogEntryClass,  SessionLogEntry.Nq.FIND_BY_SESSION_ID)
                     .withParameter("sessionId", sessionId));
    }


    public List<E> findByUser(final String username) {
        return repositoryService.allMatches(
                Query.named(sessionLogEntryClass, "findByUsername")
                     .withParameter("username", username));
    }


    public List<E> findByUserAndFromAndTo(
            final String user,
            final LocalDate from,
            final LocalDate to) {
        val fromTs = toTimestampStartOfDayWithOffset(from, 0);
        val toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<E> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USER_AND_TIMESTAMP_BETWEEN)
                        .withParameter("user", user)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USER_AND_TIMESTAMP_AFTER)
                        .withParameter("user", user)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USER_AND_TIMESTAMP_BEFORE)
                        .withParameter("user", user)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USER)
                        .withParameter("user", user);
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


    public List<E> findByUserAndStrictlyBefore(
            final String user,
            final Timestamp from) {

        return repositoryService.allMatches(
                Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USER_AND_TIMESTAMP_STRICTLY_BEFORE)
                    .withParameter("user", user)
                    .withParameter("from", from));
    }


    public List<E> findByUserAndStrictlyAfter(
            final String user,
            final Timestamp from) {
        return repositoryService.allMatches(
                Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USER_AND_TIMESTAMP_STRICTLY_AFTER)
                    .withParameter("user", user)
                    .withParameter("from", from));
    }



    public List<E> findActiveSessions() {
        return repositoryService.allMatches(
                Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_ACTIVE_SESSIONS));
    }



    public List<E> findRecentByUser(final String user) {
        return repositoryService.allMatches(
                Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_RECENT_BY_USER)
                        .withParameter("user", user));

    }

    private static Timestamp toTimestampStartOfDayWithOffset(final LocalDate dt, final int daysOffset) {
        return dt!=null
                ?new Timestamp(dt.toDateTimeAtStartOfDay().plusDays(daysOffset).getMillis())
                :null;
    }

}
