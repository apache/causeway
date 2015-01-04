package org.apache.isis.applib.services.userreg.events;

/**
 * An event send to all services interested in user registration
 */
public abstract class EmailEventAbstract {

    private final String email;
    private final String confirmationUrl;
    private final String applicationName;

    public EmailEventAbstract(
            final String email,
            final String confirmationUrl,
            final String applicationName) {
        this.email = email;
        this.confirmationUrl = confirmationUrl;
        this.applicationName = applicationName;
    }

    public String getEmail() {
        return email;
    }

    public String getConfirmationUrl() {
        return confirmationUrl;
    }

    public String getApplicationName() {
        return applicationName;
    }
}
