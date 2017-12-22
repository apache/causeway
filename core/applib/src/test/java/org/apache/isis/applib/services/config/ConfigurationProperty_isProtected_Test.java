package org.apache.isis.applib.services.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigurationProperty_isProtected_Test {

    @Test
    public void null_is_not() {
        assertFalse(ConfigurationProperty.Util.isProtected(null));

        assertEquals("xxx", ConfigurationProperty.Util.maskIfProtected(null, "xxx"));
    }

    @Test
    public void empty_is_not() {
        assertFalse(ConfigurationProperty.Util.isProtected(""));
    }

    @Test
    public void password_is() {
        assertTrue(ConfigurationProperty.Util.isProtected("foo.PassWord.bar"));
        assertTrue(ConfigurationProperty.Util.isProtected("password.bar"));
        assertTrue(ConfigurationProperty.Util.isProtected("foo.PASSWORD"));

        assertEquals("********", ConfigurationProperty.Util.maskIfProtected("password", "xxx"));
    }

    @Test
    public void apiKey_is() {
        assertTrue(ConfigurationProperty.Util.isProtected("foo.apiKey.bar"));
        assertTrue(ConfigurationProperty.Util.isProtected("APIKEY.bar"));
        assertTrue(ConfigurationProperty.Util.isProtected("foo.apikey"));
    }
    @Test
    public void authToken_is() {
        assertTrue(ConfigurationProperty.Util.isProtected("foo.authToken.bar"));
        assertTrue(ConfigurationProperty.Util.isProtected("AUTHTOKEN.bar"));
        assertTrue(ConfigurationProperty.Util.isProtected("foo.authtoken"));
    }
    @Test
    public void otherwise_is_not() {
        assertFalse(ConfigurationProperty.Util.isProtected("foo"));
    }
}