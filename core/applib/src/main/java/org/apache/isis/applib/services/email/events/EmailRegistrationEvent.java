package org.apache.isis.applib.services.email.events;

/**
 * An event send to all services interested in user registration
 */
public class EmailRegistrationEvent extends EmailEventAbstract {
    
    private final String confirmationUrl;

    public EmailRegistrationEvent(
        final String email,
        final String confirmationUrl) {
        super(email);
        this.confirmationUrl = confirmationUrl;
    }

    public String getConfirmationUrl() {
        return confirmationUrl;
    }
}
