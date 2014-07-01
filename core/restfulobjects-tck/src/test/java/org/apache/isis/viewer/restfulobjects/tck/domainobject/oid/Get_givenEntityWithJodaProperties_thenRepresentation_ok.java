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

import java.util.Date;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.*;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectMemberRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ScalarValueRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.apache.isis.viewer.restfulobjects.tck.Util;

import static org.apache.isis.core.commons.matchers.IsisMatchers.matches;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.assertThat;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isLink;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Get_givenEntityWithJodaProperties_thenRepresentation_ok {

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
    public void thenMembers() throws Exception {

        // when
        final LinkRepresentation link = Util.serviceActionListInvokeFirstReference(client, "JodaValuedEntities");
        domainObjectRepr = client.follow(link).getEntity().as(DomainObjectRepresentation.class);

        // and then members (types)
        DomainObjectMemberRepresentation property;
        ScalarValueRepresentation scalarRepr;

        property = domainObjectRepr.getProperty("dateTimeProperty");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("date-time"));
        assertThat(property.getXIsisFormat(), is("jodadatetime"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isString(), is(true));
        assertThat(scalarRepr.asDateTime(), is(asDateTime("2010-03-31T09:50:43Z")));
        assertThat(scalarRepr.asString(), is("2010-03-31T09:50:43Z"));

        property = domainObjectRepr.getProperty("localDateProperty");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("date"));
        assertThat(property.getXIsisFormat(), is("jodalocaldate"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isString(), is(true));
        assertThat(scalarRepr.asDate(), is(asDate("2008-03-21")));
        assertThat(scalarRepr.asString(), is("2008-03-21"));

        property = domainObjectRepr.getProperty("localDateTimeProperty");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("date-time"));
        assertThat(property.getXIsisFormat(), is("jodalocaldatetime"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isString(), is(true));

        final LocalDateTime expected = new LocalDateTime(2009, 4, 29, 13, 45, 22);

        assertThat(scalarRepr.asDateTime(), is(expected.toDate()));
        assertThat(scalarRepr.asString(), is(ISODateTimeFormat.dateTimeNoMillis().withZoneUTC().print(expected.toDateTime())));

        // and then member types have links to details (selected ones inspected only)
        property = domainObjectRepr.getProperty("localDateProperty");
        assertThat(property.getLinkWithRel(Rel.DETAILS), 
                isLink()
                    .href(matches(".+\\/objects\\/JODA\\/\\d+\\/properties\\/localDateProperty"))
                    .httpMethod(RestfulHttpMethod.GET)
                    .type(RepresentationType.OBJECT_PROPERTY.getMediaType()));

        property = domainObjectRepr.getProperty("localDateTimeProperty");
        assertThat(property.getLinkWithRel(Rel.DETAILS), 
                isLink()
                    .href(matches(".+\\/objects\\/JODA\\/\\d+\\/properties\\/localDateTimeProperty"))
                    .httpMethod(RestfulHttpMethod.GET)
                    .type(RepresentationType.OBJECT_PROPERTY.getMediaType()));
    }

    private static Date asDate(final String text) {
        return new java.util.Date(JsonRepresentation.yyyyMMdd.withZoneUTC().parseDateTime(text).getMillis());
    }

    private static Date asDateTime(final String text) {
        return new java.util.Date(JsonRepresentation.yyyyMMddTHHmmssZ.withZoneUTC().parseDateTime(text).getMillis());
    }

}
