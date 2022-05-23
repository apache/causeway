package org.apache.isis.sessionlog.jdo.spiimpl;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.session.SessionLogService;
import org.apache.isis.sessionlog.jdo.dom.SessionLogEntry;
import org.apache.isis.sessionlog.jdo.dom.SessionLogEntryRepository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Date;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Implementation of the Isis {@link SessionLogService} creates a log
 * entry to the database (the {@link SessionLogEntry} entity) each time a
 * user either logs on or logs out, or if their session expires.
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class SessionLoggingServiceDefault implements SessionLogService {

    final SessionLogEntryRepository sessionLogEntryRepository;
    final ClockService clockService;

    @PostConstruct
    public void init() {
        val timestamp = clockService.getClock().nowAsJavaSqlTimestamp();
        sessionLogEntryRepository.logoutAllSessions(timestamp);
    }

    @Programmatic
    @Override
    public void log(final Type type, final String username, final Date date, final CausedBy causedBy, final String sessionId) {
        val timestamp = clockService.getClock().nowAsJavaSqlTimestamp();
        if (type == Type.LOGIN) {
            sessionLogEntryRepository.create(username, sessionId, causedBy, timestamp);
        } else {
            sessionLogEntryRepository.findBySessionId(sessionId)
                    .ifPresent(entry -> {
                        entry.setLogoutTimestamp(timestamp);
                        entry.setCausedBy(causedBy);
                    }
            );
        }
    }

}
