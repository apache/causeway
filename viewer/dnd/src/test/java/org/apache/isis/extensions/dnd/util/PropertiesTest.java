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


package org.apache.isis.extensions.dnd.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.apache.isis.core.runtime.userprofile.Options;
import org.apache.isis.runtime.testsystem.TestProxySystemII;

import static org.junit.Assert.*;


public class PropertiesTest {

	@Ignore("having problems with test classpath - TestProxySystemII can't find JavaReflectorInstaller, for some reason")
    @Test
    public void getOptionsCreatesNewOptionsSet() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        new TestProxySystemII().init();
        
        Options options = Properties.getOptions("test");
        assertEquals(false, options.names().hasNext());
    }
}


