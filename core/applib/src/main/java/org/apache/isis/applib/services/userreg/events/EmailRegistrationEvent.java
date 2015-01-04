package org.apache.isis.applib.services.userreg.events;

/**
 * An event send to all services interested in user registration
 */
public class EmailRegistrationEvent extends EmailEventAbstract {
    
    public EmailRegistrationEvent(
            final String email,
            final String confirmationUrl,
            final String applicationName) {
        super(email, confirmationUrl, applicationName);
    }

}
