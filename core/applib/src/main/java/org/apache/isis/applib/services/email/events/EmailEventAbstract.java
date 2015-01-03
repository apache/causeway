package org.apache.isis.applib.services.email.events;

/**
 * An event send to all services interested in user registration
 */
public abstract class EmailEventAbstract {

    private final String email;

    public EmailEventAbstract(
        final String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
