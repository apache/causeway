package org.apache.isis.applib.services.email.events;

/**
 * An event send to all services interested in user password reset
 */
public class PasswordResetEvent extends EmailEventAbstract {

    public PasswordResetEvent(
        final String email,
        final String confirmationUrl) {
        super(email, confirmationUrl);
    }

}
