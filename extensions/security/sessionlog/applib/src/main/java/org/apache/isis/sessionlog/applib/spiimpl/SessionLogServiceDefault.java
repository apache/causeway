package org.apache.isis.sessionlog.applib.spiimpl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.session.SessionLogService;
import org.apache.isis.sessionlog.applib.dom.SessionLogEntry;
import org.apache.isis.sessionlog.applib.dom.SessionLogEntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Implementation of the Isis {@link SessionLogService} creates a log
 * entry to the database (the {@link SessionLogEntry} entity) each time a
 * user either logs on or logs out, or if their session expires.
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Named("isis.ext.sessionlog.SessionLoggingServiceDefault")
@Priority(PriorityPrecedence.LATE)
@Qualifier("default")
@Log4j2
public class SessionLogServiceDefault implements SessionLogService {

    final SessionLogEntryRepository<? extends SessionLogEntry> sessionLogEntryRepository;
    final ClockService clockService;

    @Override
    public void log(final Type type, final String username, final Date date, final CausedBy causedBy, final UUID sessionGuid, final String httpSessionId) {
        if (type == Type.LOGIN) {
            sessionLogEntryRepository.create(username, sessionGuid, httpSessionId, causedBy, Timestamp.from(date.toInstant()));
        } else {
            val sessionLogEntryIfAny = sessionLogEntryRepository.findBySessionGuid(sessionGuid);
            sessionLogEntryIfAny
                    .ifPresent(entry -> {
                        entry.setLogoutTimestamp(Timestamp.from(date.toInstant()));
                        entry.setCausedBy(causedBy);
                    }
            );
        }
    }

}
