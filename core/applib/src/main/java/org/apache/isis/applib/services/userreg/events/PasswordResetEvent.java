package org.apache.isis.applib.services.userreg.events;

/**
 * An event send to all services interested in user password reset
 */
public class PasswordResetEvent extends EmailEventAbstract {

    public PasswordResetEvent(
            final String email,
            final String confirmationUrl,
            final String applicationName) {
        super(email, confirmationUrl, applicationName);
    }

}
