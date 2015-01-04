package org.apache.isis.applib.services.email.events;

/**
 * An event send to all services interested in user registration
 */
public class EmailRegistrationEvent extends EmailEventAbstract {
    
    public EmailRegistrationEvent(
        final String email,
        final String confirmationUrl) {
        super(email, confirmationUrl);
    }

}
