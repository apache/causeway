package org.apache.isis.applib.services.email.events;

/**
 * An event send to all services interested in user password reset
 */
public class PasswordResetEvent {

    private final String email;
    private final String confirmationUrl;

    public PasswordResetEvent(
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
