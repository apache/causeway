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
package org.apache.isis.config;

import org.junit.Test;

import org.apache.isis.config.ConfigurationConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigurationConstants_isProtected_Test {

    @Test
    public void null_is_not() {
        assertFalse(ConfigurationConstants.isProtected(null));

        assertEquals("xxx", ConfigurationConstants.maskIfProtected(null, "xxx"));
    }

    @Test
    public void empty_is_not() {
        assertFalse(ConfigurationConstants.isProtected(""));
    }

    @Test
    public void password_is() {
        assertTrue(ConfigurationConstants.isProtected("foo.PassWord.bar"));
        assertTrue(ConfigurationConstants.isProtected("password.bar"));
        assertTrue(ConfigurationConstants.isProtected("foo.PASSWORD"));

        assertEquals("********", ConfigurationConstants.maskIfProtected("password", "xxx"));
    }

    @Test
    public void apiKey_is() {
        assertTrue(ConfigurationConstants.isProtected("foo.apiKey.bar"));
        assertTrue(ConfigurationConstants.isProtected("APIKEY.bar"));
        assertTrue(ConfigurationConstants.isProtected("foo.apikey"));
    }
    @Test
    public void authToken_is() {
        assertTrue(ConfigurationConstants.isProtected("foo.authToken.bar"));
        assertTrue(ConfigurationConstants.isProtected("AUTHTOKEN.bar"));
        assertTrue(ConfigurationConstants.isProtected("foo.authtoken"));
    }
    @Test
    public void otherwise_is_not() {
        assertFalse(ConfigurationConstants.isProtected("foo"));
    }
}