package org.apache.isis.applib.services.email.events;

/**
 * An event send to all services interested in user password reset
 */
public class PasswordResetEvent extends EmailEventAbstract {

    private final String confirmationUrl;

    public PasswordResetEvent(
        final String email,
        final String confirmationUrl) {
        super(email);
        this.confirmationUrl = confirmationUrl;
    }

    public String getConfirmationUrl() {
        return confirmationUrl;
    }
}
