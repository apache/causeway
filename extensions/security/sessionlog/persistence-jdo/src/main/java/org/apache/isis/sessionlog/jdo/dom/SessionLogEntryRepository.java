package org.apache.isis.sessionlog.jdo.dom;

import java.sql.Timestamp;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.session.SessionLogService;

import lombok.NonNull;

@Service
public class SessionLogEntryRepository extends org.apache.isis.sessionlog.applib.dom.SessionLogEntryRepository<SessionLogEntry> {

    public SessionLogEntryRepository() {
        super(SessionLogEntry.class);
    }
}
