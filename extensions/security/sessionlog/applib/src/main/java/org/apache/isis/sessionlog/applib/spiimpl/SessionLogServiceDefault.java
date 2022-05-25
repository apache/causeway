package org.apache.isis.sessionlog.applib.spiimpl;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.session.SessionLogService;
import org.apache.isis.sessionlog.applib.dom.SessionLogEntry;
import org.apache.isis.sessionlog.applib.dom.SessionLogEntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Implementation of the Isis {@link SessionLogService} creates a log
 * entry to the database (the {@link SessionLogEntry} entity) each time a
 * user either logs on or logs out, or if their session expires.
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class SessionLogServiceDefault implements SessionLogService {

    final SessionLogEntryRepository<? extends SessionLogEntry> sessionLogEntryRepository;
    final ClockService clockService;

    @PostConstruct
    public void init() {
        val timestamp = clockService.getClock().nowAsJavaSqlTimestamp();
        sessionLogEntryRepository.logoutAllSessions(timestamp);
    }

    @Override
    public void log(final Type type, final String username, final Date date, final CausedBy causedBy, final UUID sessionGuid, String httpSessionId) {
        val timestamp = clockService.getClock().nowAsJavaSqlTimestamp();
        if (type == Type.LOGIN) {
            sessionLogEntryRepository.create(username, sessionGuid, httpSessionId, causedBy, timestamp);
        } else {
            val sessionLogEntryIfAny = sessionLogEntryRepository.findBySessionGuid(sessionGuid);
            sessionLogEntryIfAny
                    .ifPresent(entry -> {
                        entry.setLogoutTimestamp(timestamp);
                        entry.setCausedBy(causedBy);
                    }
            );
        }
    }

}
