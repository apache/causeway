package org.apache.isis.viewer.wicket.ui.pages.register;

import java.io.Serializable;

/**
 * A model object for {@link org.apache.isis.viewer.wicket.ui.pages.register.RegisterPanel}
 */
public class Registree implements Serializable {

    private String username;
    private String password;
    private String verifyPassword;
    private String email;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(final String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }
    public void setPassword(final String password)
    {
        this.password = password;
    }

    public String getVerifyPassword() {
        return verifyPassword;
    }

    public void setVerifyPassword(String verifyPassword) {
        this.verifyPassword = verifyPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
