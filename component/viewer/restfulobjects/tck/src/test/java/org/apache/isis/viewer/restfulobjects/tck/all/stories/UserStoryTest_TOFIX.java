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
package org.apache.isis.viewer.restfulobjects.tck.all.stories;

import static org.apache.isis.core.commons.matchers.IsisMatchers.matches;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.client.RepresentationWalker;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.homepage.HomePageResource;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

public class UserStoryTest_TOFIX {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    protected RestfulClient client;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
    }

    @Ignore("to get working again")
    @Test
    public void walkResources() throws Exception {

        // given a response for an initial resource
        final HomePageResource homePageResource = client.getHomePageResource();
        final Response homePageResp = homePageResource.homePage();

        // and given a walker starting from this response
        final RepresentationWalker walker = client.createWalker(homePageResp);

        // when walk the home pages' 'services' link
        walker.walk("services");

        // and when locate the ApplibValues repo and walk the its 'object' link
        walker.walk("values[title=ApplibValues].links[rel=object]");

        // and when locate the AppLibValues repo's "newEntity" action and walk
        // to its details
        walker.walk("values[objectMemberType=action].details");

        // and when find the invoke body for the "newEntity" action and then
        // walk the action using the body
        final JsonRepresentation newEntityActionDetails = walker.getEntity();
        final JsonRepresentation newEntityActionInvokeBody = newEntityActionDetails.getArray("invoke.body");
        walker.walk("invoke", newEntityActionInvokeBody);

        // and when walk the link to the returned object
        walker.walk("link");

        // then the returned object is created with its OID
        final JsonRepresentation newEntityDomainObject = walker.getEntity();
        assertThat(newEntityDomainObject.getString("_self.link.href"), matches(".+/objects/OID:[\\d]+$"));
    }

}
