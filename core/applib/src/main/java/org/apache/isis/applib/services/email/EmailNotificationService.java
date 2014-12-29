package org.apache.isis.applib.services.email;

import java.io.Serializable;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.email.events.EmailRegistrationEvent;

/**
 * TODO ISIS-987 Javadoc
 */
public interface EmailNotificationService extends Serializable {

    @Programmatic
    boolean send(EmailRegistrationEvent emailRegistrationEvent);
}
