package org.apache.isis.applib.services.email;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.email.events.EmailRegistrationEvent;
import org.apache.isis.applib.services.email.events.PasswordResetEvent;

/**
 * TODO ISIS-987 Javadoc
 */
public interface EmailNotificationService extends Serializable {

    @PostConstruct
    @Programmatic
    public void init() ;

    @Programmatic
    boolean send(EmailRegistrationEvent emailRegistrationEvent);

    @Programmatic
    boolean send(PasswordResetEvent emailRegistrationEvent);
}
