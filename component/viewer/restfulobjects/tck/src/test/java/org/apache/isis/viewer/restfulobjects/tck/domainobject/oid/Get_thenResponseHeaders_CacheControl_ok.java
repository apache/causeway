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
package org.apache.isis.viewer.restfulobjects.tck.domainobject.oid;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.Header;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers;
import org.apache.isis.viewer.restfulobjects.tck.Util;
import org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.CacheControlMatcherBuilder;

public class Get_thenResponseHeaders_CacheControl_ok {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    protected RestfulClient client;

    private DomainObjectResource domainObjectResource;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        domainObjectResource = client.getDomainObjectResource();
    }

    @Test
    public void givenNotCached_whenUsingResourceProxy() throws Exception {

        // when
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = Util.domainObjectJaxrsResponse(client, "PrimitiveValuedEntities");

        // then
        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        
        final CacheControl expected = new CacheControl();
        expected.setNoCache(true);
        assertThat(restfulResponse.getHeader(Header.CACHE_CONTROL), isCacheControl().withNoCache().build());
    }

    
    @Ignore("Isis does not define any short-term cached semantics (use @Immutable, or (re)introduce @Cached?)")
    @Test
    public void givenShortTermCached_whenUsingResourceProxy() throws Exception {

    }


    @Ignore("TODO - Isis does not define any long-term cached semantics (use @Immutable, or (re)introduce @Cached?)")
    @Test
    public void givenLongTermCached_whenUsingResourceProxy() throws Exception {
    }


    private CacheControlMatcherBuilder isCacheControl() {
        return new RestfulMatchers.CacheControlMatcherBuilder();
    }

}
