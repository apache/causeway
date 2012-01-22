package org.apache.isis.core.runtime.authentication.standard;

import org.apache.isis.core.runtime.authentication.RegistrationDetails;

public class RegistrationDetailsPassword implements RegistrationDetails {

    private final String user;
    private final String password;

    public RegistrationDetailsPassword(final String user, final String password) {
        super();
        this.user = user;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

}
