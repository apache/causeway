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

import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectMemberRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ScalarValueRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.apache.isis.viewer.restfulobjects.tck.Util;

import static org.apache.isis.core.commons.matchers.IsisMatchers.matches;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.assertThat;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isLink;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class Get_givenEntityWithPrimitiveProperties_thenRepresentation_ok {

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
        final LinkRepresentation link = Util.serviceActionListInvokeFirstReference(client, "PrimitiveValuedEntities");
        domainObjectRepr = client.follow(link).getEntity().as(DomainObjectRepresentation.class);

        // then members (types)
        DomainObjectMemberRepresentation property;
        ScalarValueRepresentation scalarRepr;
        
        property = domainObjectRepr.getProperty("booleanProperty");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is(nullValue()));
        assertThat(property.getXIsisFormat(), is("boolean"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isBoolean(), is(true));
        Boolean booleanValue = scalarRepr.asBoolean();
        assertThat(booleanValue, is(equalTo(Boolean.TRUE)));
        
        property = domainObjectRepr.getProperty("byteProperty");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("int"));
        assertThat(property.getXIsisFormat(), is("byte"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isIntegralNumber(), is(true));
        Byte byteValue = scalarRepr.asByte();
        assertThat(byteValue, is((byte)123));

        property = domainObjectRepr.getProperty("shortProperty");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("int"));
        assertThat(property.getXIsisFormat(), is("short"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isIntegralNumber(), is(true));
        Short shortValue = scalarRepr.asShort();
        assertThat(shortValue, is((short)32123));

        property = domainObjectRepr.getProperty("intProperty");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("int"));
        assertThat(property.getXIsisFormat(), is("int"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isInt(), is(true));
        Integer intValue = scalarRepr.asInt();
        assertThat(intValue, is(987654321));

        property = domainObjectRepr.getProperty("longProperty");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("int"));
        assertThat(property.getXIsisFormat(), is("long"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isLong(), is(true));
        Long longValue = scalarRepr.asLong();
        assertThat(longValue, is(2345678901234567890L));

        property = domainObjectRepr.getProperty("charProperty");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is(nullValue()));
        assertThat(property.getXIsisFormat(), is("char"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isString(), is(true));
        Character charValue = scalarRepr.asChar();
        assertThat(charValue, is('a'));
        
        property = domainObjectRepr.getProperty("floatProperty");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("decimal"));
        assertThat(property.getXIsisFormat(), is("float"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isNumber(), is(true));
        assertThat(scalarRepr.isIntegralNumber(), is(false));
        Float floatValue = scalarRepr.asFloat();
        assertThat(floatValue, is(12345678901234567890.1234567890F));
        
        property = domainObjectRepr.getProperty("doubleProperty");
        assertThat(property.getMemberType(), is("property"));
        assertThat(property.getFormat(), is("decimal"));
        assertThat(property.getXIsisFormat(), is("double"));
        scalarRepr = property.getRepresentation("value").as(ScalarValueRepresentation.class);
        assertThat(scalarRepr.isDouble(), is(true));
        Double doubleValue = scalarRepr.asDouble();
        assertThat(doubleValue, is(12345678901234567890.1234567890));
        


        // and then member types have links to details (selected ones inspected only)
        property = domainObjectRepr.getProperty("booleanProperty");
        assertThat(property.getLinkWithRel(Rel.DETAILS),
                isLink()
                    .href(matches(".+\\/objects\\/PRMV\\/\\d+\\/properties\\/booleanProperty"))
                    .httpMethod(RestfulHttpMethod.GET)
                    .type(RepresentationType.OBJECT_PROPERTY.getMediaType()));

        property = domainObjectRepr.getProperty("byteProperty");
        assertThat(property.getLinkWithRel(Rel.DETAILS),
                isLink()
                    .href(matches(".+\\/objects\\/PRMV\\/\\d+\\/properties\\/byteProperty"))
                    .httpMethod(RestfulHttpMethod.GET)
                    .type(RepresentationType.OBJECT_PROPERTY.getMediaType()));

        property = domainObjectRepr.getProperty("shortProperty");
        assertThat(property.getLinkWithRel(Rel.DETAILS), 
                isLink()
                    .href(matches(".+\\/objects\\/PRMV\\/\\d+\\/properties\\/shortProperty"))
                    .httpMethod(RestfulHttpMethod.GET)
                    .type(RepresentationType.OBJECT_PROPERTY.getMediaType()));

        // can navigate using fully qualified form of Rel
        property = domainObjectRepr.getProperty("booleanProperty");
        assertThat(property.getLinkWithRel(Rel.DETAILS.andParam("property", "booleanProperty")),
                isLink()
                        .href(matches(".+\\/objects\\/PRMV\\/\\d+\\/properties\\/booleanProperty"))
                        .httpMethod(RestfulHttpMethod.GET)
                        .type(RepresentationType.OBJECT_PROPERTY.getMediaType()));


    }


}
