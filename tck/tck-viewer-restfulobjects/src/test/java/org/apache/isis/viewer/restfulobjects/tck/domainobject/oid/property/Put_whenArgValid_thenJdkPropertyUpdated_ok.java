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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectPropertyRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Put_whenArgValid_thenJdkPropertyUpdated_ok {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    protected RestfulClient client;

    private DomainObjectResource domainObjectResource;

    private LinkRepresentation modifyLink;
    private JsonRepresentation argRepr;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        domainObjectResource = client.getDomainObjectResource();
    }

    /**
     * Tests change state, so discard such that will be recreated by next test.
     */
    @After
    public void tearDown() throws Exception {
        webServerRule.discardWebApp();
    }


    @Test
    public void jdkPropertiesUpdated() throws Exception {

        // big decimal
        final BigDecimal bd = new BigDecimal("12345678901234567.789");
        modifyLink = getObjectPropertyReprModifyLink("JDKV", "38", "bigDecimalProperty");
        argRepr = modifyLink.getArguments().mapPut("value", bd);
        assertThat(followedRepr(modifyLink,argRepr).getBigDecimal("value"), is(new BigDecimal("12345678901234567.7890000000"))); // big-decimal(30,10)

        // big integer
        final BigInteger bi = new BigInteger("123456789012345678");
        modifyLink = getObjectPropertyReprModifyLink("JDKV", "38", "bigIntegerProperty");
        argRepr = modifyLink.getArguments().mapPut("value", bi);
        assertThat(followedRepr(modifyLink,argRepr).getBigInteger("value"), is(bi));

        // java.sql.Date
        final java.sql.Date sqld = new java.sql.Date(new DateTime(2014,5,1, 0,0, DateTimeZone.UTC).getMillis());
        modifyLink = getObjectPropertyReprModifyLink("JDKV", "38", "javaSqlDateProperty");
        argRepr = modifyLink.getArguments().mapPut("value", asIsoNoT(sqld));
        assertThat(followedRepr(modifyLink,argRepr).getDate("value"), is((java.util.Date)sqld));

        // java.sql.Time
        final java.sql.Time sqlt = new java.sql.Time(13,0,0);
        modifyLink = getObjectPropertyReprModifyLink("JDKV", "38", "javaSqlTimeProperty");
        argRepr = modifyLink.getArguments().mapPut("value", asIsoOnlyT(sqlt));
        assertThat(followedRepr(modifyLink,argRepr).getTime("value"), is((java.util.Date)sqlt));

        // java.sql.Timestamp
        final java.sql.Timestamp sqlts = new java.sql.Timestamp(114,4,1,13,0,0,0);
        modifyLink = getObjectPropertyReprModifyLink("JDKV", "38", "javaSqlTimestampProperty");
        argRepr = modifyLink.getArguments().mapPut("value", sqlts.getTime());
        assertThat(followedRepr(modifyLink,argRepr).getLong("value"), is(sqlts.getTime()));

        // java.util.Date
        final java.util.Date d = new java.util.Date(114,4,1,13,0,0);
        modifyLink = getObjectPropertyReprModifyLink("JDKV", "38", "javaUtilDateProperty");
        argRepr = modifyLink.getArguments().mapPut("value", asIso(d));
        assertThat(followedRepr(modifyLink,argRepr).getDateTime("value"), is(d));

        // enum
        final String e = "ORANGE";
        modifyLink = getObjectPropertyReprModifyLink("JDKV", "38", "myEnum");
        argRepr = modifyLink.getArguments().mapPut("value", e);
        assertThat(followedRepr(modifyLink,argRepr).getString("value"), is(e));

        // String
        final String s = "Tangerine";
        modifyLink = getObjectPropertyReprModifyLink("JDKV", "38", "stringProperty");
        argRepr = modifyLink.getArguments().mapPut("value", s);
        assertThat(followedRepr(modifyLink,argRepr).getString("value"), is(s));

    }


    private ObjectPropertyRepresentation getObjectPropertyRepr(final String domainType, final String instanceId, String propertyId) throws IOException {
        final Response domainObjectResp = domainObjectResource.propertyDetails(domainType, instanceId, propertyId);
        final RestfulResponse<ObjectPropertyRepresentation> domainObjectJsonResp = RestfulResponse.ofT(domainObjectResp);
        assertThat(domainObjectJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));

        final ObjectPropertyRepresentation repr = domainObjectJsonResp.getEntity();
        return repr;
    }

    private LinkRepresentation getObjectPropertyReprModifyLink(String domainType, String instanceId, String propertyId) throws IOException {
        ObjectPropertyRepresentation objectPropertyRepr = getObjectPropertyRepr(domainType, instanceId, propertyId);
        return objectPropertyRepr.getLinkWithRel(Rel.MODIFY);
    }

    private JsonRepresentation followedRepr(LinkRepresentation modifyLink, JsonRepresentation argRepr) throws Exception {
        RestfulResponse<JsonRepresentation> result = client.follow(modifyLink, argRepr);
        assertThat(result.getStatus(), is(HttpStatusCode.OK));
        return result.getEntity().as(ObjectPropertyRepresentation.class);
    }


    private static String asIso(final java.util.Date d) {
        final DateTime dt = new DateTime(d.getTime());
        return asIso(dt);
    }

    private static String asIso(final DateTime dt) {
        return ISODateTimeFormat.basicDateTimeNoMillis().withZoneUTC().print(dt);
    }

    private static String asIsoNoT(final java.util.Date d) {
        final DateTime dt = new DateTime(d.getTime());
        return asIsoNoT(dt);
    }

    private static String asIsoNoT(final DateTime dt) {
        return ISODateTimeFormat.basicDate().withZoneUTC().print(dt);
    }

    private static String asIsoOnlyT(final java.util.Date d) {
        final DateTime dt = new DateTime(d.getTime());
        return asIsoOnlyT(dt);
    }

    private static String asIsoOnlyT(final DateTime dt) {
        return ISODateTimeFormat.basicTime().withZoneUTC().print(dt);
    }

}
