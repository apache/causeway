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