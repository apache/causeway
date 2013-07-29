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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.format.ISODateTimeFormat;
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
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectMemberRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

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

    @Ignore
    @Test
    public void primitivePropertiesUpdated() throws Exception {
        
        final DomainObjectRepresentation domainObjectRepr = getObjectRepr("PRMV", "31");
        
        final LinkRepresentation updateLink = domainObjectRepr.getLinkWithRel(Rel.UPDATE);
        
        final JsonRepresentation argRepr = updateLink.getArguments();
        
        // {
        //   "byteProperty":{"value":123,"x-isis-format":"byte"},
        //   "charProperty":{"value":"a","x-isis-format":"char"},
        //   "doubleProperty":{"value":1.2345678901234567E19,"format":"decimal","x-isis-format":"double"},
        //   "floatProperty":{"value":1.2345679E19,"format":"decimal","x-isis-format":"float"},
        //   "intProperty":{"value":987654321,"format":"int","x-isis-format":"int"},
        //   "longProperty":{"value":2345678901234567890,"format":"int","x-isis-format":"long"},
        //   "shortProperty":{"value":32123,"x-isis-format":"short"},
        //   "booleanProperty":{"value":true,"x-isis-format":"boolean"},
        //   "id":{"value":0,"format":"int","x-isis-format":"int"}
        // }

        final byte b = (byte)99;
        final char c = 'b';
        final double d = 12345.678;
        final float f = 54321.123F;
        final int i = 999999;
        final int l = 999999999;
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
        assertThat(afterResp.getProperty("longProperty").getInt("value"), is(l));
        assertThat(afterResp.getProperty("shortProperty").getShort("value"), is(s));
        assertThat(afterResp.getProperty("booleanProperty").getBoolean("value"), is(z));
    }

    @Test
    public void jdkPropertiesUpdated() throws Exception {
        
        final DomainObjectRepresentation domainObjectRepr = getObjectRepr("JDKV", "29");
        
        final LinkRepresentation updateLink = domainObjectRepr.getLinkWithRel(Rel.UPDATE);
        
        final JsonRepresentation argRepr = updateLink.getArguments();
        
        //{
        //  bigDecimalProperty: {
        //    value: null,
        //    format: "decimal",
        //    x-isis-format: "bigdecimal"
        //  },
        //  bigIntegerProperty: {
        //    value: null,
        //    format: "int",
        //    x-isis-format: "biginteger"
        //  },
        //  javaSqlDateProperty: {
        //    value: null,
        //    format: "decimal",
        //    x-isis-format: "bigdecimal"
        //  },
        //  javaSqlTimeProperty: {
        //    value: null,
        //    format: "decimal",
        //    x-isis-format: "bigdecimal"
        //  },
        //  javaSqlTimestampProperty: {
        //    value: null,
        //    format: "decimal",
        //    x-isis-format: "bigdecimal"
        //  },
        //  javaUtilDateProperty: {
        //    value: null,
        //    format: "decimal",
        //    x-isis-format: "bigdecimal"
        //  },
        //  myEnum: {
        //    value: null,
        //    format: "decimal",
        //    x-isis-format: "bigdecimal"
        //  },
        //  stringProperty: {
        //    value: null,
        //    format: "decimal",
        //    x-isis-format: "bigdecimal"
        //  }
        //}
        final BigDecimal bd = new BigDecimal("12345678901234567.789");
        final BigInteger bi = new BigInteger("12345678901234567890");
        final java.sql.Date sqld = new java.sql.Date(114,4,1);
        final java.sql.Time sqlt = new java.sql.Time(13,0,0);
        final java.sql.Timestamp sqlts = new java.sql.Timestamp(114,4,1,13,0,0,0);
        final java.util.Date d = new java.util.Date(114,4,1,13,0,0);
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
        RestfulResponse<JsonRepresentation> result = client.follow(updateLink, argRepr);
        assertThat(result.getStatus(), is(HttpStatusCode.OK));
        
        final DomainObjectRepresentation afterResp = result.getEntity().as(DomainObjectRepresentation.class);
        
        // TODO: bigdecimal being truncated/converted to doubles...
        //assertThat(afterResp.getProperty("bigDecimalProperty").getBigDecimal("value"), is(bd));
        assertThat(afterResp.getProperty("bigIntegerProperty").getBigInteger("value"), is(bi));
        assertThat(afterResp.getProperty("javaSqlDateProperty").getDate("value"), is((java.util.Date)sqld));
        assertThat(afterResp.getProperty("javaSqlTimeProperty").getTime("value"), is((java.util.Date)sqlt));
        assertThat(afterResp.getProperty("javaSqlTimestampProperty").getLong("value"), is(sqlts.getTime()));
        assertThat(afterResp.getProperty("javaUtilDateProperty").getDateTime("value"), is(d));
        assertThat(afterResp.getProperty("myEnum").getString("value"), is(e));
        assertThat(afterResp.getProperty("stringProperty").getString("value"), is(s));
        
    }

    private static String asIso(final java.util.Date d) {
        return ISODateTimeFormat.basicDateTimeNoMillis().print(new org.joda.time.DateTime(d.getTime()));
    }
    
    private static String asIsoNoT(final java.util.Date d) {
        return ISODateTimeFormat.basicDate().print(new org.joda.time.DateTime(d.getTime()));
    }
    
    private static String asIsoOnlyT(final java.util.Date d) {
        return ISODateTimeFormat.basicTime().print(new org.joda.time.DateTime(d.getTime()));
    }

    private DomainObjectRepresentation getObjectRepr(final String domainType, final String instanceId) throws JsonParseException, JsonMappingException, IOException {
        final Response domainObjectResp = domainObjectResource.object(domainType, instanceId);
        final RestfulResponse<DomainObjectRepresentation> domainObjectJsonResp = RestfulResponse.ofT(domainObjectResp);
        assertThat(domainObjectJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));

        final DomainObjectRepresentation domainObjectRepr = domainObjectJsonResp.getEntity();
        return domainObjectRepr;
    }
}
