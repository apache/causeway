/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.security.sql.authentication;

import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.runtime.system.context.IsisContext;

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
        properties.put(SqlAuthenticator.PROPERTY_BASE + ".jdbc.connection", "jdbc:hsqldb:file:hsql-db/authenticator-tests");
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
        final AuthenticationRequestPassword request = new AuthenticationRequestPassword("user1", "password1");
        Assert.assertTrue(saipSqlAuthenticator.isValid(request));
    }

    @Test
    public void VerifyThatIsValidReturnsTrueInMixedCase() {
        final AuthenticationRequestPassword request = new AuthenticationRequestPassword("uSer1", "password1");
        Assert.assertTrue(saipSqlAuthenticator.isValid(request));
    }

    @Test
    public void VerifyThatIsValidReturnsFalseForNoPassword() {
        final AuthenticationRequestPassword request = new AuthenticationRequestPassword("user1", "");
        Assert.assertFalse(saipSqlAuthenticator.isValid(request));
    }

    @Test
    public void VerifyThatIsValidReturnsFalseForWrongPassword() {
        final AuthenticationRequestPassword request = new AuthenticationRequestPassword("user1", "password12");
        Assert.assertFalse(saipSqlAuthenticator.isValid(request));
    }

    @Test
    public void VerifyThatIsValidReturnsFalseForWrongUsername() {
        final AuthenticationRequestPassword request = new AuthenticationRequestPassword("user", "password1");
        Assert.assertFalse(saipSqlAuthenticator.isValid(request));
    }

    @After
    public void tearDown() {
        saipSqlAuthenticator.shutdown();
    }
}
