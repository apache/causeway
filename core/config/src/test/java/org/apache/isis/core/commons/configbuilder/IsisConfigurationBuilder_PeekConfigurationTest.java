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

package org.apache.isis.core.commons.configbuilder;

import org.apache.isis.core.commons.config.IsisConfiguration;

import junit.framework.TestCase;

public class IsisConfigurationBuilder_PeekConfigurationTest extends TestCase {

    private IsisConfigurationBuilder configurationBuilder;

    public IsisConfigurationBuilder_PeekConfigurationTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
    	configurationBuilder = IsisConfigurationBuilder.empty();
    	configurationBuilder.add("isis.test", "val");
    }

    public void testPeekConfiguration() {
    	
    	IsisConfiguration configuration = null;
    	
    	// pre-conditions
    	assertNull(configuration);
    	assertEquals("val", configurationBuilder.peekAtString("isis.test"));
    	
    	configuration = configurationBuilder.build();
    	
    	// builder is no longer usable
    	try {
    	    assertEquals("val", configurationBuilder.peekAtString("isis.test"));
    	    fail("should throw");
    	} catch (NullPointerException e) {
    	    // expected
        }
    	
    	// post-conditions
    	assertNotNull(configuration);
        assertEquals("val", configuration.getString("isis.test"));
        
    }

}
