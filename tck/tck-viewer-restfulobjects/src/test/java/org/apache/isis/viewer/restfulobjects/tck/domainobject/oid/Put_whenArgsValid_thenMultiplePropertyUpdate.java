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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
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
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Put_whenArgsValid_thenMultiplePropertyUpdate {

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

    /**
     * Tests change state, so discard such that will be recreated by next test.
     */
    @After
    public void tearDown() throws Exception {
        webServerRule.discardWebApp();
    }

    
    @Test
    public void primitivePropertiesUpdated() throws Exception {
        
        final DomainObjectRepresentation domainObjectRepr = getObjectRepr("PRMV", "43");
        
        final LinkRepresentation updateLink = domainObjectRepr.getLinkWithRel(Rel.UPDATE);
        
        final JsonRepresentation argRepr = updateLink.getArguments();
        
        final byte b = (byte)99;
        final char c = 'b';
        final double d = 12345.678;
        final float f = 54321.123F;
        final int i = 999999;
        final long l = 99999999999L;
        final short s = (short)999;
        final boolean z = false;
        argRepr.mapPut("byteProperty.value", b);
        argRepr.mapPut("charProperty.value", c);
        argRepr.mapPut("doubleProperty.value", d);
        argRepr.mapPut("floatProperty.value", f);
        argRepr.mapPut("intProperty.value", i);
        argRepr.mapPut("longProperty.value", l);
        argRepr.mapPut("shortProperty.value", s);
        argRepr.mapPut("booleanProperty.value", z);
        RestfulResponse<JsonRepresentation> result = client.follow(updateLink, argRepr);
        assertThat(result.getStatus(), is(HttpStatusCode.OK));
        
        final DomainObjectRepresentation afterResp = result.getEntity().as(DomainObjectRepresentation.class);
        assertThat(afterResp.getProperty("byteProperty").getByte("value"), is(b));
        assertThat(afterResp.getProperty("charProperty").getChar("value"), is(c));
        assertThat(afterResp.getProperty("doubleProperty").getDouble("value"), is(d));
        assertThat(afterResp.getProperty("floatProperty").getFloat("value"), is(f));
        assertThat(afterResp.getProperty("intProperty").getInt("value"), is(i));
        assertThat(afterResp.getProperty("longProperty").getLong("value"), is(l));
        assertThat(afterResp.getProperty("shortProperty").getShort("value"), is(s));
        assertThat(afterResp.getProperty("booleanProperty").getBoolean("value"), is(z));
    }

    @Test
    public void jdkPropertiesUpdated() throws Exception {
        
        final DomainObjectRepresentation domainObjectRepr = getObjectRepr("JDKV", "38");
        final LinkRepresentation updateLink = domainObjectRepr.getLinkWithRel(Rel.UPDATE);
        final JsonRepresentation argRepr = updateLink.getArguments();

        final BigDecimal bd = new BigDecimal("12345678901234567.789");
        final BigInteger bi = new BigInteger("123456789012345678");
        final java.sql.Date sqld = new java.sql.Date(new DateTime(2014,5,1, 0,0, DateTimeZone.UTC).getMillis());
        final java.sql.Time sqlt = new java.sql.Time(13,0,0);
        final java.sql.Timestamp sqlts = new java.sql.Timestamp(114,4,1,13,0,0,0);
        final java.util.Date d = new DateTime(2014,5,1, 11,45, DateTimeZone.UTC).toDate();
        final String e = "ORANGE";
        final String s = "Tangerine";
        
        argRepr.mapPut("bigDecimalProperty.value", bd);
        argRepr.mapPut("bigIntegerProperty.value", bi);
        argRepr.mapPut("javaSqlDateProperty.value", asIsoNoT(sqld)); // 1-may-2014
        argRepr.mapPut("javaSqlTimeProperty.value", asIsoOnlyT(sqlt)); // 1 pm
        argRepr.mapPut("javaSqlTimestampProperty.value", sqlts.getTime());
        argRepr.mapPut("javaUtilDateProperty.value", asIso(d));
        argRepr.mapPut("myEnum.value", e);
        argRepr.mapPut("stringProperty.value", s);
        
        final RestfulResponse<JsonRepresentation> result = client.follow(updateLink, argRepr);
        assertThat(result.getStatus(), is(HttpStatusCode.OK));
        
        final DomainObjectRepresentation afterResp = result.getEntity().as(DomainObjectRepresentation.class);
        
        assertThat(afterResp.getProperty("bigDecimalProperty").getBigDecimal("value"), is(new BigDecimal("12345678901234567.7890000000"))); // big-decimal(30,10)
        assertThat(afterResp.getProperty("bigIntegerProperty").getBigInteger("value"), is(bi));
        assertThat(afterResp.getProperty("javaSqlDateProperty").getDate("value"), is((java.util.Date)sqld));
        assertThat(afterResp.getProperty("javaSqlTimeProperty").getTime("value"), is((java.util.Date)sqlt));
        assertThat(afterResp.getProperty("javaSqlTimestampProperty").getLong("value"), is(sqlts.getTime()));
        assertThat(afterResp.getProperty("javaUtilDateProperty").getDateTime("value"), is(d));
        assertThat(afterResp.getProperty("myEnum").getString("value"), is(e));
        assertThat(afterResp.getProperty("stringProperty").getString("value"), is(s));
        
    }

    @Test
    public void jodaPropertiesUpdated() throws Exception {
        
        final DomainObjectRepresentation domainObjectRepr = getObjectRepr("JODA", "83");
        
        final LinkRepresentation updateLink = domainObjectRepr.getLinkWithRel(Rel.UPDATE);
        
        final JsonRepresentation argRepr = updateLink.getArguments();
        
        final LocalDate ld = new LocalDate(2013,5,1);
        final LocalDateTime ldt = new LocalDateTime(2013,2,1,14,15,0);
        final DateTime dt = new DateTime(2013,2,1,14,15,0, DateTimeZone.UTC);
        final String s = "New string";
        
        argRepr.mapPut("localDateProperty.value", "2013-05-01");
        argRepr.mapPut("localDateTimeProperty.value", "2013-02-01T14:15:00Z");
        argRepr.mapPut("dateTimeProperty.value", asIso(dt.toDate()));
        argRepr.mapPut("stringProperty.value", s);

        final RestfulResponse<JsonRepresentation> result = client.follow(updateLink, argRepr);
        assertThat(result.getStatus(), is(HttpStatusCode.OK));
        
        final DomainObjectRepresentation afterResp = result.getEntity().as(DomainObjectRepresentation.class);
        
        assertThat(afterResp.getProperty("localDateProperty").getString("value"), is("2013-05-01")); // being a bit hacky here...
        assertThat(afterResp.getProperty("localDateTimeProperty").getDateTime("value"), is(ldt.toDate()));
        assertThat(afterResp.getProperty("dateTimeProperty").getDateTime("value"), is(dt.toDate()));
        assertThat(afterResp.getProperty("stringProperty").getString("value"), is(s));
    }
    
    private static String asIso(final java.util.Date d) {
        final org.joda.time.DateTime dt = new org.joda.time.DateTime(d.getTime());
        return asIso(dt);
    }

    private static String asIso(final org.joda.time.DateTime dt) {
        return ISODateTimeFormat.dateTimeNoMillis().withZoneUTC().print(dt);
    }

    private static String asIsoNoT(final java.util.Date d) {
        final org.joda.time.DateTime dt = new org.joda.time.DateTime(d.getTime());
        return asIsoNoT(dt);
    }

    private static String asIsoNoT(final org.joda.time.DateTime dt) {
        return ISODateTimeFormat.date().withZoneUTC().print(dt);
    }
    
    private static String asIsoOnlyT(final java.util.Date d) {
        final org.joda.time.DateTime dt = new org.joda.time.DateTime(d.getTime());
        return asIsoOnlyT(dt);
    }

    private static String asIsoOnlyT(final org.joda.time.DateTime dt) {
        return ISODateTimeFormat.timeNoMillis().withZoneUTC().print(dt);
    }

    private DomainObjectRepresentation getObjectRepr(final String domainType, final String instanceId) throws IOException {
        final Response domainObjectResp = domainObjectResource.object(domainType, instanceId);
        final RestfulResponse<DomainObjectRepresentation> domainObjectJsonResp = RestfulResponse.ofT(domainObjectResp);
        assertThat(domainObjectJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));

        final DomainObjectRepresentation domainObjectRepr = domainObjectJsonResp.getEntity();
        return domainObjectRepr;
    }
}
