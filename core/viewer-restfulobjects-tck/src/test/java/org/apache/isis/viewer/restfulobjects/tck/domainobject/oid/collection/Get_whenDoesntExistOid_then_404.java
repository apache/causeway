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
package org.apache.isis.viewer.restfulobjects.tck.domainobject.oid.collection;

import org.jboss.resteasy.client.core.executors.URLConnectionClientExecutor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulRequest;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.version.VersionRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Get_whenDoesntExistOid_then_404 {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;

    @Before
    public void setUp() throws Exception {
        client = webServerRule.getClient(new URLConnectionClientExecutor());
    }

    @Test
    public void returns404() throws Exception {

        givenWhenThen("73", RestfulResponse.HttpStatusCode.OK);
        givenWhenThen("nonExistentOid", RestfulResponse.HttpStatusCode.NOT_FOUND);

    }

    private void givenWhenThen(String oid, RestfulResponse.HttpStatusCode statusCode1) {
        // given
        RestfulRequest request = client.createRequest(RestfulHttpMethod.GET, "objects/BSRL/" +
                oid +
                "/collections/visibleAndEditableCollection");

        // when
        RestfulResponse<VersionRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(statusCode1));
    }

}
