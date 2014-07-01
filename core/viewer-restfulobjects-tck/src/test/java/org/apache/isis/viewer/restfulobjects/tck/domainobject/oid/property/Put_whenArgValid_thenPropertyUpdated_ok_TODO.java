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

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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

public class Put_whenArgValid_thenPropertyUpdated_ok_TODO {

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
    public void primitivePropertiesUpdated() throws Exception {

        // byte
        final byte b = (byte)99;
        modifyLink = getObjectPropertyReprModifyLink("PRMV", "31", "byteProperty");
        argRepr = modifyLink.getArguments().mapPut("value", b);
        assertThat(followedRepr(modifyLink,argRepr).getByte("value"), is(b));
        
        // char
        final char c = 'b';
        modifyLink = getObjectPropertyReprModifyLink("PRMV", "31", "charProperty");
        argRepr = modifyLink.getArguments().mapPut("value", c);
        assertThat(followedRepr(modifyLink,argRepr).getChar("value"), is(c));

        // double
        final double d = 12345.678;
        modifyLink = getObjectPropertyReprModifyLink("PRMV", "31", "doubleProperty");
        argRepr = modifyLink.getArguments().mapPut("value", d);
        assertThat(followedRepr(modifyLink,argRepr).getDouble("value"), is(d));

        // float
        final float f = 54321.123F;
        modifyLink = getObjectPropertyReprModifyLink("PRMV", "31", "floatProperty");
        argRepr = modifyLink.getArguments().mapPut("value", f);
        assertThat(followedRepr(modifyLink,argRepr).getFloat("value"), is(f));
        
        // int
        final int i = 999999;
        modifyLink = getObjectPropertyReprModifyLink("PRMV", "31", "intProperty");
        argRepr = modifyLink.getArguments().mapPut("value", i);
        assertThat(followedRepr(modifyLink,argRepr).getInt("value"), is(i));
        
        // long
        final long l = 99999999999L;
        modifyLink = getObjectPropertyReprModifyLink("PRMV", "31", "longProperty");
        argRepr = modifyLink.getArguments().mapPut("value", l);
        assertThat(followedRepr(modifyLink,argRepr).getLong("value"), is(l));
        
        // short
        final short s = (short)999;
        modifyLink = getObjectPropertyReprModifyLink("PRMV", "31", "shortProperty");
        argRepr = modifyLink.getArguments().mapPut("value", s);
        assertThat(followedRepr(modifyLink,argRepr).getShort("value"), is(s));
        
        // boolean
        final boolean z = false;
        modifyLink = getObjectPropertyReprModifyLink("PRMV", "31", "booleanProperty");
        argRepr = modifyLink.getArguments().mapPut("value", z);
        assertThat(followedRepr(modifyLink,argRepr).getBoolean("value"), is(z));
        
    }

    @Test
    public void jdkPropertiesUpdated() throws Exception {

        // big decimal
        final BigDecimal bd = new BigDecimal("12345678901234567.789");
        modifyLink = getObjectPropertyReprModifyLink("JDKV", "29", "bigDecimalProperty");
        argRepr = modifyLink.getArguments().mapPut("value", bd);
        assertThat(followedRepr(modifyLink,argRepr).getBigDecimal("value"), is(new BigDecimal("12345678901234567.7890000000"))); // big-decimal(30,10)

        // big integer
        final BigInteger bi = new BigInteger("123456789012345678");
        modifyLink = getObjectPropertyReprModifyLink("JDKV", "29", "bigIntegerProperty");
        argRepr = modifyLink.getArguments().mapPut("value", bi);
        assertThat(followedRepr(modifyLink,argRepr).getBigInteger("value"), is(bi));

        // java.sql.Date
        final java.sql.Date sqld = new java.sql.Date(new DateTime(2014,5,1, 0,0, DateTimeZone.UTC).getMillis());
        modifyLink = getObjectPropertyReprModifyLink("JDKV", "29", "javaSqlDateProperty");
        argRepr = modifyLink.getArguments().mapPut("value", asIsoNoT(sqld));
        assertThat(followedRepr(modifyLink,argRepr).getDate("value"), is((java.util.Date)sqld));

        // java.sql.Time
        final java.sql.Time sqlt = new java.sql.Time(13,0,0);
        modifyLink = getObjectPropertyReprModifyLink("JDKV", "29", "javaSqlTimeProperty");
        argRepr = modifyLink.getArguments().mapPut("value", asIsoOnlyT(sqlt));
        assertThat(followedRepr(modifyLink,argRepr).getTime("value"), is((java.util.Date)sqlt));

        // java.sql.Timestamp
        final java.sql.Timestamp sqlts = new java.sql.Timestamp(114,4,1,13,0,0,0);
        modifyLink = getObjectPropertyReprModifyLink("JDKV", "29", "javaSqlTimestampProperty");
        argRepr = modifyLink.getArguments().mapPut("value", sqlts.getTime());
        assertThat(followedRepr(modifyLink,argRepr).getLong("value"), is(sqlts.getTime()));

        // java.util.Date
        final java.util.Date d = new java.util.Date(114,4,1,13,0,0);
        modifyLink = getObjectPropertyReprModifyLink("JDKV", "29", "javaUtilDateProperty");
        argRepr = modifyLink.getArguments().mapPut("value", asIso(d));
        assertThat(followedRepr(modifyLink,argRepr).getDateTime("value"), is(d));

        // enum
        final String e = "ORANGE";
        modifyLink = getObjectPropertyReprModifyLink("JDKV", "29", "myEnum");
        argRepr = modifyLink.getArguments().mapPut("value", e);
        assertThat(followedRepr(modifyLink,argRepr).getString("value"), is(e));

        // String
        final String s = "Tangerine";
        modifyLink = getObjectPropertyReprModifyLink("JDKV", "29", "stringProperty");
        argRepr = modifyLink.getArguments().mapPut("value", s);
        assertThat(followedRepr(modifyLink,argRepr).getString("value"), is(s));

    }

    
    @Ignore("breaking in CET")
    @Test
    public void jodaPropertiesUpdated() throws Exception {

        // LocalDate
        final LocalDate ld = new LocalDate(2013,5,1);
        modifyLink = getObjectPropertyReprModifyLink("JODA", "73", "localDateProperty");
        argRepr = modifyLink.getArguments().mapPut("value", "2013-05-01");
        assertThat(followedRepr(modifyLink,argRepr).getString("value"), is("2013-05-01")); // hacky

        // LocalDateTime
        final LocalDateTime ldt = new LocalDateTime(2013,2,1,14,15,0);
        modifyLink = getObjectPropertyReprModifyLink("JODA", "73", "localDateTimeProperty");
        argRepr = modifyLink.getArguments().mapPut("value", asIso(ldt.toDate()));
        assertThat(followedRepr(modifyLink,argRepr).getDateTime("value"), is(ldt.toDate()));
        
        // DateTime
        final DateTime dt = new DateTime(2013,2,1,14,15,0);
        modifyLink = getObjectPropertyReprModifyLink("JODA", "73", "dateTimeProperty");
        argRepr = modifyLink.getArguments().mapPut("value", asIso(dt.toDate()));
        assertThat(followedRepr(modifyLink,argRepr).getDateTime("value"), is(dt.toDate()));

        // String
        final String s = "New string";
        modifyLink = getObjectPropertyReprModifyLink("JODA", "73", "stringProperty");
        argRepr = modifyLink.getArguments().mapPut("value", s);
        assertThat(followedRepr(modifyLink,argRepr).getString("value"), is(s));
    }

    private ObjectPropertyRepresentation getObjectPropertyRepr(final String domainType, final String instanceId, String propertyId) throws JsonParseException, JsonMappingException, IOException {
        final Response domainObjectResp = domainObjectResource.propertyDetails(domainType, instanceId, propertyId);
        final RestfulResponse<ObjectPropertyRepresentation> domainObjectJsonResp = RestfulResponse.ofT(domainObjectResp);
        assertThat(domainObjectJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));

        final ObjectPropertyRepresentation repr = domainObjectJsonResp.getEntity();
        return repr;
    }

    private LinkRepresentation getObjectPropertyReprModifyLink(String domainType, String instanceId, String propertyId) throws JsonParseException, JsonMappingException, IOException {
        ObjectPropertyRepresentation objectPropertyRepr = getObjectPropertyRepr(domainType, instanceId, propertyId);
        return objectPropertyRepr.getLinkWithRel(Rel.MODIFY);
    }
    
    private JsonRepresentation followedRepr(LinkRepresentation modifyLink, JsonRepresentation argRepr) throws Exception {
        RestfulResponse<JsonRepresentation> result = client.follow(modifyLink, argRepr);
        assertThat(result.getStatus(), is(HttpStatusCode.OK));
        return result.getEntity().as(ObjectPropertyRepresentation.class);
    }


    private static String asIso(final java.util.Date d) {
        final org.joda.time.DateTime dt = new org.joda.time.DateTime(d.getTime());
        return asIso(dt);
    }

    private static String asIso(final org.joda.time.DateTime dt) {
        return ISODateTimeFormat.basicDateTimeNoMillis().withZoneUTC().print(dt);
    }
    
    
    private static String asIsoNoT(final java.util.Date d) {
        final org.joda.time.DateTime dt = new org.joda.time.DateTime(d.getTime());
        return asIsoNoT(dt);
    }

    private static String asIsoNoT(final org.joda.time.DateTime dt) {
        return ISODateTimeFormat.basicDate().withZoneUTC().print(dt);
    }
    
    private static String asIsoOnlyT(final java.util.Date d) {
        final org.joda.time.DateTime dt = new org.joda.time.DateTime(d.getTime());
        return asIsoOnlyT(dt);
    }

    private static String asIsoOnlyT(final org.joda.time.DateTime dt) {
        return ISODateTimeFormat.basicTime().withZoneUTC().print(dt);
    }

}
