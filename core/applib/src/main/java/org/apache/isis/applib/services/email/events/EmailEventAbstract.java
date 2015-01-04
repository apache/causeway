package org.apache.isis.applib.services.email.events;

/**
 * An event send to all services interested in user registration
 */
public abstract class EmailEventAbstract {

    private final String email;
    private final String confirmationUrl;

    public EmailEventAbstract(
            final String email,
            final String confirmationUrl) {
        this.email = email;
        this.confirmationUrl = confirmationUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getConfirmationUrl() {
        return confirmationUrl;
    }

}
