package org.apache.isis.applib.services.session;

import java.util.Date;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;

import lombok.extern.log4j.Log4j2;

@Service
@Named("isisApplib.SessionLoggingServiceLogging")
@Order(OrderPrecedence.LATE)
@Primary
@Qualifier("logging")
@Log4j2
// tag::refguide[]
public class SessionLoggingServiceLogging implements SessionLoggingService {

    @Override
    public void log(
            final Type type,
            final String username,
            final Date date,
            final CausedBy causedBy,
            final String sessionId) {

        if(log.isDebugEnabled()) {
            final StringBuilder logMessage = new StringBuilder();
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
            log.debug(logMessage);
        }
    }
}
// end::refguide[]
