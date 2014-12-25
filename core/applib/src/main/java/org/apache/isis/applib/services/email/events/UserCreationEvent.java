package org.apache.isis.applib.services.email.events;

/**
 *
 */
public class UserCreationEvent {
    
    private final String email;
    private final String password;

    public UserCreationEvent(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
