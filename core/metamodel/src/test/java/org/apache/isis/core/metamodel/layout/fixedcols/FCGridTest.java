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
package org.apache.isis.core.metamodel.layout.fixedcols;

import java.util.Map;

import javax.xml.bind.Marshaller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.layout.fixedcols.FCColumn;
import org.apache.isis.applib.layout.fixedcols.FCGrid;
import org.apache.isis.applib.layout.fixedcols.FCTab;
import org.apache.isis.applib.layout.fixedcols.FCTabGroup;
import org.apache.isis.applib.layout.common.ActionLayoutData;
import org.apache.isis.applib.layout.common.CollectionLayoutData;
import org.apache.isis.applib.layout.common.FieldSet;
import org.apache.isis.applib.layout.common.PropertyLayoutData;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.core.metamodel.services.grid.normalizer.GridNormalizerServiceDefault;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FCGridTest {

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

        final FCGrid fcPage = new FCGrid();

        fcPage.setTabGroups(Lists.<FCTabGroup>newArrayList());
        fcPage.getTabGroups().add(new FCTabGroup());
        FCTabGroup tabGroup = fcPage.getTabGroups().get(0);
        FCTab FCTab = tabGroup.getTabs().get(0);
        FCTab.setName("Common");
        FCColumn left = FCTab.getLeft();

        FieldSet leftPropGroup = new FieldSet();
        left.setFieldSets(Lists.<FieldSet>newArrayList());
        left.getFieldSets().add(leftPropGroup);
        leftPropGroup.setName("General");

        CollectionLayoutData similarToColl = new CollectionLayoutData();
        left.setCollections(Lists.<CollectionLayoutData>newArrayList());
        left.getCollections().add(similarToColl);
        similarToColl.setId("similarTo");

        left.getFieldSets().add(new FieldSet("General"));
        PropertyLayoutData namePropertyLayoutData = new PropertyLayoutData("name");
        left.getFieldSets().get(0).getProperties().add(namePropertyLayoutData);

        ActionLayoutData updateNameActionLayoutData = new ActionLayoutData();
        updateNameActionLayoutData.setId("updateName");
        namePropertyLayoutData.setActions(Lists.<ActionLayoutData>newArrayList());
        namePropertyLayoutData.getActions().add(updateNameActionLayoutData);

        ActionLayoutData deleteActionLayoutData = new ActionLayoutData();
        deleteActionLayoutData.setId("delete");
        fcPage.setActions(Lists.<ActionLayoutData>newArrayList());
        fcPage.getActions().add(deleteActionLayoutData);

        final String schemaLocations = new GridNormalizerServiceDefault().schemaLocationsFor(fcPage);
        String xml = jaxbService.toXml(fcPage,
                ImmutableMap.<String,Object>of(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocations));
        System.out.println(xml);

        FCGrid fcPageRoundtripped = jaxbService.fromXml(FCGrid.class, xml);
        String xmlRoundtripped = jaxbService.toXml(fcPageRoundtripped,
                ImmutableMap.<String,Object>of(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocations));
        assertThat(xml, is(equalTo(xmlRoundtripped)));


        System.out.println("==========");

        dumpXsd(fcPage);
    }

    protected void dumpXsd(final FCGrid fcPage) {
        Map<String, String> schemas = jaxbService.toXsd(fcPage, JaxbService.IsisSchemas.INCLUDE);
        for (Map.Entry<String, String> entry : schemas.entrySet()) {
            System.out.println(entry.getKey() + ":");
            System.out.println(entry.getValue());
        }
    }
}