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
package org.apache.isis.viewer.restfulobjects.tck.domainobject.oid.property;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.apache.isis.viewer.restfulobjects.tck.Util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Get_whenDoesntExistProp_then_404 {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    protected RestfulClient client;

    private LinkRepresentation link;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        
        link = new LinkRepresentation();
    }

    @Test
    public void whenPropertyDoesntExist() throws Exception {

        Util.serviceActionListInvokeFirstReference(client, "PrimitiveValuedEntities");

        // given
        final LinkRepresentation linkToExistingObject = Util.serviceActionListInvokeFirstReference(client, "PrimitiveValuedEntities");
        link.withHref(linkToExistingObject.getHref() + "/properties/nonExistentProperty");
        
        // when
        final RestfulResponse<JsonRepresentation> restfulResp = client.follow(link);
        
        // then
        assertThat(restfulResp.getStatus(), is(HttpStatusCode.NOT_FOUND));
        
        Util.serviceActionListInvokeFirstReference(client, "PrimitiveValuedEntities");

    }

}
