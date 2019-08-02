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

package org.apache.isis.metamodel.adapter.version;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class VersionTest_differs {
    

    private Version version1, version2;

    private final static String EMPTY_USERNAME = null;

    @Test
    public void whenEqual() throws Exception {
        version1 = Version.of(123L, EMPTY_USERNAME);
        version2 = Version.of(123L, EMPTY_USERNAME);
        assertThat(version1.different(version2), is(false));
    }

    
    @Test
    public void whenNotEqual() throws Exception {
        version1 = Version.of(123L, EMPTY_USERNAME);
        version2 = Version.of(124L, EMPTY_USERNAME);
        assertThat(version1.different(version2), is(true));
    }

}
