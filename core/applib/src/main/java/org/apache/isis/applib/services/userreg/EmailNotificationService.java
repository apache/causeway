package org.apache.isis.applib.services.userreg;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.userreg.events.EmailRegistrationEvent;
import org.apache.isis.applib.services.userreg.events.PasswordResetEvent;

/**
 * Supporting service for the user-registration functionality.
 *
 * <p>
 *     The framework provides a default implementation which in turn uses the
 *     {@link org.apache.isis.applib.services.email.EmailService}, namely <code>EmailServiceDefault</code>.
 * </p>
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
