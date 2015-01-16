package org.apache.isis.applib.services.session;

import java.util.Date;

import org.apache.isis.applib.annotation.Programmatic;

/**
 *
 */
public interface SessionLoggingService {

    public enum Type {
        LOGIN,
        LOGOUT
    }

    public enum CausedBy {
        USER,
        SESSION_EXPIRATION
    }

    @Programmatic
    void log(Type type, String username, Date date, CausedBy causedBy, String sessionId);


    public static class Stderr implements SessionLoggingService {

        @Override
        public void log(final Type type, final String username, final Date date, final CausedBy causedBy, final String sessionId) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("User '").append(username);
            logMessage.append("' with sessionId '").append(sessionId)
            .append("' has logged ");
            if (type == Type.LOGIN) {
                logMessage.append("in");
            } else {
                logMessage.append("out");
            }
            logMessage.append(" at '").append(date).append("'.");
            if (causedBy == CausedBy.SESSION_EXPIRATION) {
                logMessage.append("Cause: session expiration");
            }
            System.err.println(logMessage);
        }
    }
}
