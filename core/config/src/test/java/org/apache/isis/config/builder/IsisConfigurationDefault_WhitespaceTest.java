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

package org.apache.isis.config.builder;

import java.util.Properties;

import org.apache.isis.config.builder.IsisConfigurationDefault;

import junit.framework.TestCase;

public class IsisConfigurationDefault_WhitespaceTest extends TestCase {

    private IsisConfigurationDefault configuration;

    public IsisConfigurationDefault_WhitespaceTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {

        configuration = new IsisConfigurationDefault();

        final Properties p = new Properties();
        p.put("properties.leadingSpaces", "  twoSpacesBeforeThis");
        p.put("properties.leadingTab", "\toneTabBeforeThis");
        p.put("properties.trailingSpaces", "twoSpacesAfterThis  ");
        p.put("properties.trailingTab", "oneTabAfterThis\t");
        p.put("properties.trailingTabAndSpaces", "oneTabAndTwoSpacesAfterThis\t  ");
        configuration.add(p, IsisConfigurationDefault.ContainsPolicy.IGNORE);

    }

    public void testLeadingSpaces() {
        assertEquals("twoSpacesBeforeThis", configuration.getString("properties.leadingSpaces"));
    }

    public void testLeadingTab() {
        assertEquals("oneTabBeforeThis", configuration.getString("properties.leadingTab"));
    }

    public void testTrailingSpaces() {
        assertEquals("twoSpacesAfterThis", configuration.getString("properties.trailingSpaces"));
    }

    public void testTrailingTab() {
        assertEquals("oneTabAfterThis", configuration.getString("properties.trailingTab"));
    }

    public void testTrailingTabSpaces() {
        assertEquals("oneTabAndTwoSpacesAfterThis", configuration.getString("properties.trailingTabAndSpaces"));
    }

}
