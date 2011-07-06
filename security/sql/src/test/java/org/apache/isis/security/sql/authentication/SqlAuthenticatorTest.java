package org.apache.isis.security.sql.authentication;

import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;

public class SqlAuthenticatorTest {
    IsisConfigurationDefault configuration;
    SqlAuthenticator saipSqlAuthenticator;

    private static final String PASSWORD_FIELD = "password";
    private static final String USERNAME_FIELD = "email";
    private static final String USERS = "USERS";

    @Before
    public void setUp() {
        // Setup properties files
        final Properties properties = new Properties();
        properties.setProperty(SqlAuthenticator.USER_TABLE, USERS);
        properties.setProperty(SqlAuthenticator.USER_TABLE_NAME_FIELD, USERNAME_FIELD);
        properties.setProperty(SqlAuthenticator.USER_TABLE_PASSWORD_FIELD, PASSWORD_FIELD);

        properties.put(SqlAuthenticator.PROPERTY_BASE + ".jdbc.driver", "org.hsqldb.jdbcDriver");
        properties.put(SqlAuthenticator.PROPERTY_BASE + ".jdbc.connection",
            "jdbc:hsqldb:file:hsql-db/authenticator-tests");
        properties.put(SqlAuthenticator.PROPERTY_BASE + ".jdbc.user", "sa");
        properties.put(SqlAuthenticator.PROPERTY_BASE + ".jdbc.password", "");

        configuration = new IsisConfigurationDefault();
        configuration.add(properties);

        // setup configuration
        IsisContext.setConfiguration(configuration);

        // Setup database
        String sql;

        saipSqlAuthenticator = new SqlAuthenticator(configuration);
        saipSqlAuthenticator.init();

        if (saipSqlAuthenticator.isSetup()) { // clear existing data
            sql = "DROP TABLE " + USERS;
            saipSqlAuthenticator.update(sql);
        }

        sql = "CREATE TABLE " + USERS + " (" + USERNAME_FIELD + " VARCHAR(32), " + PASSWORD_FIELD + " VARCHAR(32)) ";
        saipSqlAuthenticator.update(sql);

        // create data
        sql = "INSERT INTO " + USERS + " VALUES ('user1','password1')";
        saipSqlAuthenticator.update(sql);

    }

    @Test
    public void VerifyThatIsValidReturnsTrue() {
        AuthenticationRequestPassword request = new AuthenticationRequestPassword("user1", "password1");
        Assert.assertTrue(saipSqlAuthenticator.isValid(request));
    }

    @Test
    public void VerifyThatIsValidReturnsTrueInMixedCase() {
        AuthenticationRequestPassword request = new AuthenticationRequestPassword("uSer1", "password1");
        Assert.assertTrue(saipSqlAuthenticator.isValid(request));
    }

    @Test
    public void VerifyThatIsValidReturnsFalseForNoPassword() {
        AuthenticationRequestPassword request = new AuthenticationRequestPassword("user1", "");
        Assert.assertFalse(saipSqlAuthenticator.isValid(request));
    }

    @Test
    public void VerifyThatIsValidReturnsFalseForWrongPassword() {
        AuthenticationRequestPassword request = new AuthenticationRequestPassword("user1", "password12");
        Assert.assertFalse(saipSqlAuthenticator.isValid(request));
    }

    @Test
    public void VerifyThatIsValidReturnsFalseForWrongUsername() {
        AuthenticationRequestPassword request = new AuthenticationRequestPassword("user", "password1");
        Assert.assertFalse(saipSqlAuthenticator.isValid(request));
    }

    @After
    public void tearDown() {
        saipSqlAuthenticator.shutdown();
    }
}
