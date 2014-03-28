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
package org.apache.isis.viewer.restfulobjects.tck.any;

import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class NotAuthorizedTest_TODO {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();
    private RestfulClient client;

    @Before
    public void setUp() throws Exception {
        client = webServerRule.getClient();
    }

    @Ignore("TODO")
    @Test
    public void whenAuthenticated() throws Exception {

    }

    @Ignore("TODO")
    @Test
    public void whenNotAuthenticated() throws Exception {
     // should return 401 (13.5)
    }

}
