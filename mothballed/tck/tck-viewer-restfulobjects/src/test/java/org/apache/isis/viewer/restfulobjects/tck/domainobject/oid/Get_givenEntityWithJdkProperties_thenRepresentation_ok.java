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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectMemberRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ScalarValueRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.apache.isis.viewer.restfulobjects.tck.Util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Get_givenEntityWithJdkProperties_thenRepresentation_ok {

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
        final LinkRepresentation link = Util.serviceActionListInvokeFirstReference(client, "JdkValuedEntities");
        final RestfulResponse<JsonRepresentation> restResp = client.follow(link);
        final JsonRepresentation entityRepr = restResp.getEntity();
        domainObjectRepr = entityRepr.as(DomainObjectRepresentation.class);

        // and then members (types)
        DomainObjectMemberRepresentation property;
        ScalarValueRepresentation scalarRepr;

        property = domainObjectRepr.getProperty("bigDecimalProperty");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("big-decimal(30,10)"));
        assertThat(property.getXIsisFormat(), is("javamathbigdecimal"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isString(), is(true));
        BigDecimal bigDecimal = scalarRepr.asBigDecimal(property.getFormat());
        assertThat(bigDecimal, is(new BigDecimal("12345678901234567890.1234567890")));

        property = domainObjectRepr.getProperty("bigDecimalProperty2");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("big-decimal(18,2)"));
        assertThat(property.getXIsisFormat(), is("javamathbigdecimal"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isString(), is(true));
        BigDecimal bigDecimal2 = scalarRepr.asBigDecimal(property.getFormat());
        assertThat(bigDecimal2, is(new BigDecimal("123.45")));

        property = domainObjectRepr.getProperty("bigIntegerProperty");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("big-integer"));
        assertThat(property.getXIsisFormat(), is("javamathbiginteger"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isString(), is(true));
        BigInteger bigInteger = scalarRepr.asBigInteger(property.getFormat());
        assertThat(bigInteger, is(new BigInteger("123456789012345678")));

        property = domainObjectRepr.getProperty("bigIntegerProperty2");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("big-integer"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isString(), is(true));
        BigInteger bigInteger2 = scalarRepr.asBigInteger(property.getFormat());
        assertThat(bigInteger2, is(new BigInteger("12345")));

        property = domainObjectRepr.getProperty("javaSqlDateProperty");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("date"));
        assertThat(property.getXIsisFormat(), is("javasqldate"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isString(), is(true));
        assertThat(scalarRepr.asString(), is("2014-04-24"));
        assertThat(scalarRepr.asDate(), is(asDate("2014-04-24")));

        property = domainObjectRepr.getProperty("javaSqlTimeProperty");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("time"));
        assertThat(property.getXIsisFormat(), is("javasqltime"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isString(), is(true));
        assertThat(scalarRepr.asString(), is("12:34:45"));
        assertThat(scalarRepr.asTime(), is(asDateTime("1970-01-01T12:34:45Z")));

        property = domainObjectRepr.getProperty("javaSqlTimestampProperty");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("utc-millisec"));
        assertThat(property.getXIsisFormat(), is("javasqltimestamp"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isInt() || scalarRepr.isLong(), is(true));
        Long aLong = scalarRepr.asLong();
        assertThat(aLong, is(new Long("1234567890")));

        property = domainObjectRepr.getProperty("javaUtilDateProperty");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("date-time"));
        assertThat(property.getXIsisFormat(), is("javautildate"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isString(), is(true));
        Date utilDate = scalarRepr.asDateTime();
        assertThat(utilDate, is(asDateTime("2013-05-25T12:34:45Z")));
        assertThat(scalarRepr.asString(), is("2013-05-25T12:34:45Z"));

        property = domainObjectRepr.getProperty("myEnum");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("string"));
        assertThat(property.getXIsisFormat(), is("string"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isString(), is(true));
        String myEnumStr = scalarRepr.asString();
        assertThat(myEnumStr, is("RED"));
    }


    private static Date asDate(final String text) {
        return new java.util.Date(JsonRepresentation.yyyyMMdd.withZoneUTC().parseDateTime(text).getMillis());
    }

    private static Date asDateTime(final String text) {
        return new java.util.Date(JsonRepresentation.yyyyMMddTHHmmssZ.withZoneUTC().parseDateTime(text).getMillis());
    }

}
