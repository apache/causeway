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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.apache.isis.viewer.restfulobjects.tck.Util;

import static org.apache.isis.core.commons.matchers.IsisMatchers.matches;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.assertThat;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isLink;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class Get_givenEntity_thenRepresentation_ofTitleIdLinksEtc_ok {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    protected RestfulClient client;

    private DomainObjectRepresentation domainObjectRepr;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
    }

    @Test
    public void thenTitle_andExtensions_andLinks() throws Exception {

        // when
        final LinkRepresentation link = Util.serviceActionListInvokeFirstReference(client, "PrimitiveValuedEntities");
        domainObjectRepr = client.follow(link).getEntity().as(DomainObjectRepresentation.class);

        // then has domain type, instanceId, title
        assertThat(domainObjectRepr, is(not(nullValue())));

        assertThat(domainObjectRepr.getTitle(), is("Primitive Valued Entity #0")); // running in-memory
        assertThat(domainObjectRepr.getDomainType(), is("PRMV"));
        assertThat(domainObjectRepr.getInstanceId(), is(not(nullValue())));
        
        // and then extensions
        assertThat(domainObjectRepr.getExtensions().getString("oid"), IsisMatchers.startsWith("PRMV:" + domainObjectRepr.getInstanceId()));
        assertThat(domainObjectRepr.getExtensions().getBoolean("isService"), is(false));
        assertThat(domainObjectRepr.getExtensions().getBoolean("isPersistent"), is(true));

        // and then has links
        final LinkRepresentation self = domainObjectRepr.getSelf();
        assertThat(self, isLink()
                            .rel(Rel.SELF)
                            .href(matches(".+\\/objects\\/PRMV\\/\\d+"))
                            .httpMethod(RestfulHttpMethod.GET)
                            .type(RepresentationType.DOMAIN_OBJECT.getMediaType()));
        assertThat(domainObjectRepr.getLinkWithRel(Rel.DESCRIBEDBY), 
                        isLink()
                            .href(matches(".+\\/domain-types\\/PRMV"))
                            .httpMethod(RestfulHttpMethod.GET)
                            .type(RepresentationType.DOMAIN_TYPE.getMediaType()));
        assertThat(domainObjectRepr.getLinkWithRel(Rel.UPDATE),
                        isLink()
                            .href(matches(".+\\/objects\\/PRMV\\/\\d+"))
                            .httpMethod(RestfulHttpMethod.PUT)
                            .type(RepresentationType.DOMAIN_OBJECT.getMediaType()));
        assertThat(domainObjectRepr.getLinkWithRel(Rel.ICON),  
                is(nullValue()));

    }


}
