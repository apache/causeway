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
import com.google.common.collect.Lists;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.layout.v1_0.ActionLayoutMetadata;
import org.apache.isis.applib.layout.v1_0.CollectionLayoutMetadata;
import org.apache.isis.applib.layout.fixedcols.ColumnMetadata;
import org.apache.isis.applib.layout.fixedcols.ObjectLayoutMetadata;
import org.apache.isis.applib.layout.v1_0.PropertyLayoutMetadata;
import org.apache.isis.applib.layout.v1_0.PropertyGroupMetadata;
import org.apache.isis.applib.layout.fixedcols.TabMetadata;
import org.apache.isis.applib.layout.fixedcols.TabGroupMetadata;
import org.apache.isis.applib.services.jaxb.JaxbService;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ObjectLayoutMetadataTest {

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

        final ObjectLayoutMetadata objectLayoutMetadata = new ObjectLayoutMetadata();

        objectLayoutMetadata.setTabGroups(Lists.<TabGroupMetadata>newArrayList());
        objectLayoutMetadata.getTabGroups().add(new TabGroupMetadata());
        TabGroupMetadata tabGroup = objectLayoutMetadata.getTabGroups().get(0);
        TabMetadata tabMetadata = tabGroup.getTabs().get(0);
        tabMetadata.setName("Common");
        ColumnMetadata left = tabMetadata.getLeft();

        PropertyGroupMetadata leftPropGroup = new PropertyGroupMetadata();
        left.setPropertyGroups(Lists.<PropertyGroupMetadata>newArrayList());
        left.getPropertyGroups().add(leftPropGroup);
        leftPropGroup.setName("General");

        CollectionLayoutMetadata similarToColl = new CollectionLayoutMetadata();
        left.setCollections(Lists.<CollectionLayoutMetadata>newArrayList());
        left.getCollections().add(similarToColl);
        similarToColl.setId("similarTo");

        left.getPropertyGroups().add(new PropertyGroupMetadata("General"));
        PropertyLayoutMetadata namePropertyLayoutMetadata = new PropertyLayoutMetadata("name");
        left.getPropertyGroups().get(0).getProperties().add(namePropertyLayoutMetadata);

        ActionLayoutMetadata updateNameActionLayoutMetadata = new ActionLayoutMetadata();
        updateNameActionLayoutMetadata.setId("updateName");
        namePropertyLayoutMetadata.setActions(Lists.<ActionLayoutMetadata>newArrayList());
        namePropertyLayoutMetadata.getActions().add(updateNameActionLayoutMetadata);

        ActionLayoutMetadata deleteActionLayoutMetadata = new ActionLayoutMetadata();
        deleteActionLayoutMetadata.setId("delete");
        objectLayoutMetadata.setActions(Lists.<ActionLayoutMetadata>newArrayList());
        objectLayoutMetadata.getActions().add(deleteActionLayoutMetadata);

        String xml = jaxbService.toXml(objectLayoutMetadata,
                ImmutableMap.<String,Object>of(
                        Marshaller.JAXB_SCHEMA_LOCATION,
                        "http://isis.apache.org/schema/applib/layout http://isis.apache.org/schema/applib/layout/layout-1.0.xsd"
                ));
        System.out.println(xml);

        ObjectLayoutMetadata objectLayoutMetadataRoundtripped = jaxbService.fromXml(ObjectLayoutMetadata.class, xml);
        String xmlRoundtripped = jaxbService.toXml(objectLayoutMetadataRoundtripped,
                ImmutableMap.<String,Object>of(
                        Marshaller.JAXB_SCHEMA_LOCATION,
                        "http://isis.apache.org/schema/applib/layout http://isis.apache.org/schema/applib/layout/layout-1.0.xsd"
                ));
        assertThat(xml, is(equalTo(xmlRoundtripped)));


        System.out.println("==========");

        dumpXsd(objectLayoutMetadata);
    }

    protected void dumpXsd(final ObjectLayoutMetadata objectLayoutMetadata) {
        Map<String, String> schemas = jaxbService.toXsd(objectLayoutMetadata, JaxbService.IsisSchemas.INCLUDE);
        for (Map.Entry<String, String> entry : schemas.entrySet()) {
            //System.out.println(entry.getKey() + ":");
            System.out.println(entry.getValue());
        }
    }
}