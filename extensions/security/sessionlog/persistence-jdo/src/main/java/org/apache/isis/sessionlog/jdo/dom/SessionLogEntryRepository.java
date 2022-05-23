package org.apache.isis.sessionlog.jdo.dom;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.session.SessionLogService;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Provides supporting functionality for querying {@link SessionLogEntry session log entry} entities.
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class SessionLogEntryRepository {

    final RepositoryService repositoryService;

    public void logoutAllSessions(final Timestamp logoutTimestamp) {

        val allSessions = repositoryService.allMatches(
                Query.named(SessionLogEntry.class, SessionLogEntry.Nq.FIND_ACTIVE_SESSIONS));
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
        return repositoryService.persistAndFlush(new SessionLogEntry(sessionId, username, causedBy, timestamp));
    }

    public Optional<SessionLogEntry> findBySessionId(final String sessionId) {
        return repositoryService.firstMatch(
                Query.named(SessionLogEntry.class,  SessionLogEntry.Nq.FIND_BY_SESSION_ID)
                     .withParameter("sessionId", sessionId));
    }


    public List<SessionLogEntry> findByUser(final String username) {
        return repositoryService.allMatches(
                Query.named(SessionLogEntry.class, "findByUsername")
                     .withParameter("username", username));
    }


    public List<SessionLogEntry> findByUserAndFromAndTo(
            final String user,
            final LocalDate from,
            final LocalDate to) {
        val fromTs = toTimestampStartOfDayWithOffset(from, 0);
        val toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<SessionLogEntry> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(SessionLogEntry.class, SessionLogEntry.Nq.FIND_BY_USER_AND_TIMESTAMP_BETWEEN)
                        .withParameter("user", user)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(SessionLogEntry.class, SessionLogEntry.Nq.FIND_BY_USER_AND_TIMESTAMP_AFTER)
                        .withParameter("user", user)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(SessionLogEntry.class, SessionLogEntry.Nq.FIND_BY_USER_AND_TIMESTAMP_BEFORE)
                        .withParameter("user", user)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(SessionLogEntry.class, SessionLogEntry.Nq.FIND_BY_USER)
                        .withParameter("user", user);
            }
        }
        return repositoryService.allMatches(query);
    }


    public List<SessionLogEntry> findByFromAndTo(
            final LocalDate from,
            final LocalDate to) {
        val fromTs = toTimestampStartOfDayWithOffset(from, 0);
        val toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<SessionLogEntry> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(SessionLogEntry.class, SessionLogEntry.Nq.FIND_BY_TIMESTAMP_BETWEEN)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(SessionLogEntry.class, SessionLogEntry.Nq.FIND_BY_TIMESTAMP_AFTER)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(SessionLogEntry.class, SessionLogEntry.Nq.FIND_BY_TIMESTAMP_BEFORE)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(SessionLogEntry.class, SessionLogEntry.Nq.FIND);
            }
        }
        return repositoryService.allMatches(query);
    }


    public List<SessionLogEntry> findByUserAndStrictlyBefore(
            final String user,
            final Timestamp from) {

        return repositoryService.allMatches(
                Query.named(SessionLogEntry.class, SessionLogEntry.Nq.FIND_BY_USER_AND_TIMESTAMP_STRICTLY_BEFORE)
                    .withParameter("user", user)
                    .withParameter("from", from));
    }


    public List<SessionLogEntry> findByUserAndStrictlyAfter(
            final String user,
            final Timestamp from) {
        return repositoryService.allMatches(
                Query.named(SessionLogEntry.class, SessionLogEntry.Nq.FIND_BY_USER_AND_TIMESTAMP_STRICTLY_AFTER)
                    .withParameter("user", user)
                    .withParameter("from", from));
    }



    public List<SessionLogEntry> findActiveSessions() {
        return repositoryService.allMatches(
                Query.named(SessionLogEntry.class, SessionLogEntry.Nq.FIND_ACTIVE_SESSIONS));
    }



    public List<SessionLogEntry> findRecentByUser(final String user) {
        return repositoryService.allMatches(
                Query.named(SessionLogEntry.class, SessionLogEntry.Nq.FIND_RECENT_BY_USER)
                        .withParameter("user", user));

    }

    private static Timestamp toTimestampStartOfDayWithOffset(final LocalDate dt, final int daysOffset) {
        return dt!=null
                ?new Timestamp(dt.toDateTimeAtStartOfDay().plusDays(daysOffset).getMillis())
                :null;
    }

}
