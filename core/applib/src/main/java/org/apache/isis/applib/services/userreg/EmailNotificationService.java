package org.apache.isis.applib.services.userreg;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.userreg.events.EmailRegistrationEvent;
import org.apache.isis.applib.services.userreg.events.PasswordResetEvent;

/**
 * TODO ISIS-987 Javadoc
 */
public interface EmailNotificationService extends Serializable {

    @PostConstruct
    @Programmatic
    public void init() ;

    @Programmatic
    boolean send(EmailRegistrationEvent ev);

    @Programmatic
    boolean send(PasswordResetEvent ev);

    /**
     * Whether this service has been configured and thus available for use.
     */
    @Programmatic
    boolean isConfigured();
}
