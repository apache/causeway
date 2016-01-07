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
package org.apache.isis.core.metamodel.layoutxml.v1_0;

import java.util.Map;

import javax.xml.bind.Marshaller;

import com.google.common.collect.ImmutableMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.layout.v1_0.Action;
import org.apache.isis.applib.layout.v1_0.Collection;
import org.apache.isis.applib.layout.v1_0.Column;
import org.apache.isis.applib.layout.v1_0.DomainObject;
import org.apache.isis.applib.layout.v1_0.Property;
import org.apache.isis.applib.layout.v1_0.PropertyGroup;
import org.apache.isis.applib.layout.v1_0.Tab;
import org.apache.isis.applib.layout.v1_0.TabGroup;
import org.apache.isis.applib.services.jaxb.JaxbService;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DomainObjectTest {

    private JaxbService jaxbService;

    @Before
    public void setUp() throws Exception {
        jaxbService = new JaxbService.Simple();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void xxx() throws Exception {

        final DomainObject domainObject = new DomainObject();

        TabGroup tabGroup = domainObject.getTabGroups().get(0);
        Tab tab = tabGroup.getTabs().get(0);
        tab.setName("Common");
        Column left = tab.getLeft();

        PropertyGroup leftPropGroup = new PropertyGroup();
        left.getContent().add(leftPropGroup);
        leftPropGroup.setName("General");

        Collection similarToColl = new Collection();
        left.getContent().add(similarToColl);
        similarToColl.setId("similarTo");

        Property nameProperty = leftPropGroup.getProperties().get(0);
        nameProperty.setId("name");

        Action updateNameAction = new Action();
        updateNameAction.setId("updateName");
        nameProperty.getActions().add(updateNameAction);

        Action deleteAction = new Action();
        deleteAction.setId("delete");
        domainObject.getActions().add(deleteAction);

        String xml = jaxbService.toXml(domainObject,
                ImmutableMap.<String,Object>of(
                        Marshaller.JAXB_SCHEMA_LOCATION,
                        "http://isis.apache.org/schema/applib/layout http://isis.apache.org/schema/applib/layout/layout-1.0.xsd"
                ));
        System.out.println(xml);

        DomainObject domainObjectRoundtripped = jaxbService.fromXml(DomainObject.class, xml);
        String xmlRoundtripped = jaxbService.toXml(domainObjectRoundtripped,
                ImmutableMap.<String,Object>of(
                        Marshaller.JAXB_SCHEMA_LOCATION,
                        "http://isis.apache.org/schema/applib/layout http://isis.apache.org/schema/applib/layout/layout-1.0.xsd"
                ));
        assertThat(xml, is(equalTo(xmlRoundtripped)));


        System.out.println("==========");

        dumpXsd(domainObject);
    }

    protected void dumpXsd(final DomainObject domainObject) {
        Map<String, String> schemas = jaxbService.toXsd(domainObject, JaxbService.IsisSchemas.INCLUDE);
        for (Map.Entry<String, String> entry : schemas.entrySet()) {
            System.out.println(entry.getKey() + ":");
            System.out.println(entry.getValue());
        }
    }
}