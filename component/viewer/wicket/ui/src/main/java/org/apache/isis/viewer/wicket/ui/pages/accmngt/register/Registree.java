package org.apache.isis.viewer.wicket.ui.pages.accmngt.register;

import java.io.Serializable;

/**
 * A model object for {@link org.apache.isis.viewer.wicket.ui.pages.accmngt.register.RegisterPanel}
 */
public class Registree implements Serializable {

    private String username;
    private String password;
    private String confirmPassword;
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
