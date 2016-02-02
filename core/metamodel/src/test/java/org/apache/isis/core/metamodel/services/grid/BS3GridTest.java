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
package org.apache.isis.core.metamodel.services.grid;

import java.util.Arrays;
import java.util.Map;

import javax.xml.bind.Marshaller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.layout.bootstrap3.BS3Col;
import org.apache.isis.applib.layout.bootstrap3.BS3Grid;
import org.apache.isis.applib.layout.bootstrap3.BS3Row;
import org.apache.isis.applib.layout.bootstrap3.BS3Tab;
import org.apache.isis.applib.layout.bootstrap3.BS3TabGroup;
import org.apache.isis.applib.layout.common.ActionLayoutData;
import org.apache.isis.applib.layout.common.CollectionLayoutData;
import org.apache.isis.applib.layout.common.DomainObjectLayoutData;
import org.apache.isis.applib.layout.common.FieldSet;
import org.apache.isis.applib.layout.common.PropertyLayoutData;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.core.metamodel.services.grid.bootstrap3.GridNormalizerServiceBS3;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BS3GridTest {

    private JaxbService jaxbService;
    private GridServiceDefault gridServiceDefault;
    private GridNormalizerServiceBS3 gridNormalizerServiceBS3;

    @Before
    public void setUp() throws Exception {
        jaxbService = new JaxbService.Simple();
        gridServiceDefault = new GridServiceDefault();
        gridNormalizerServiceBS3 = new GridNormalizerServiceBS3();
        gridServiceDefault.gridNormalizerServices = Arrays.<GridNormalizerService>asList(gridNormalizerServiceBS3);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void xxx() throws Exception {

        final BS3Grid bs3Grid = new BS3Grid();

        // header
        final BS3Row headerRow = bs3Grid.getRows().get(0);
        final BS3Col headerCol = (BS3Col) headerRow.getCols().get(0);
        headerCol.setSpan(12);

        final DomainObjectLayoutData objectLayoutData = new DomainObjectLayoutData();
        headerCol.setDomainObject(objectLayoutData);

        final ActionLayoutData deleteActionLayoutData = new ActionLayoutData();
        deleteActionLayoutData.setId("delete");
        headerCol.setActions(Lists.<ActionLayoutData>newArrayList());
        headerCol.getActions().add(deleteActionLayoutData);

        // content
        final BS3Row contentRow = new BS3Row();
        bs3Grid.getRows().add(contentRow);

        final BS3Col contentCol = (BS3Col) contentRow.getCols().get(0);
        contentCol.setSpan(12);

        // a tabgroup containing a 'Common' tab
        final BS3TabGroup tabGroup = new BS3TabGroup();
        contentCol.getTabGroups().add(tabGroup);
        BS3Tab bs3Tab = tabGroup.getTabs().get(0);
        bs3Tab.setName("Common");

        // with a left col...
        final BS3Row tabRow = bs3Tab.getRows().get(0);
        final BS3Col tabLeftCol = (BS3Col) tabRow.getCols().get(0);
        tabLeftCol.setSpan(6);

        // containing a fieldset
        final FieldSet leftPropGroup = new FieldSet("General");
        tabLeftCol.setFieldSets(Lists.<FieldSet>newArrayList());
        tabLeftCol.getFieldSets().add(leftPropGroup);
        leftPropGroup.setName("General");

        // with a single property
        final PropertyLayoutData namePropertyLayoutData = leftPropGroup.getProperties().get(0);
        namePropertyLayoutData.setNamed("name");

        // and its associated action
        final ActionLayoutData updateNameActionLayoutData = new ActionLayoutData();
        updateNameActionLayoutData.setId("updateName");
        namePropertyLayoutData.setActions(Lists.<ActionLayoutData>newArrayList());
        namePropertyLayoutData.getActions().add(updateNameActionLayoutData);

        // and the tab also has a right col...
        final BS3Col tabRightCol = (BS3Col) tabRow.getCols().get(0);
        tabRightCol.setSpan(6);

        // containing a collection
        final CollectionLayoutData similarToColl = new CollectionLayoutData();
        tabRightCol.setCollections(Lists.<CollectionLayoutData>newArrayList());
        tabRightCol.getCollections().add(similarToColl);
        similarToColl.setId("similarTo");

        final String schemaLocations = gridServiceDefault.tnsAndSchemaLocation(bs3Grid);
        String xml = jaxbService.toXml(bs3Grid,
                ImmutableMap.<String,Object>of(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocations));
        System.out.println(xml);

        BS3Grid bs3Pageroundtripped = jaxbService.fromXml(BS3Grid.class, xml);
        String xmlRoundtripped = jaxbService.toXml(bs3Pageroundtripped,
                ImmutableMap.<String,Object>of(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocations));
        assertThat(xml, is(equalTo(xmlRoundtripped)));


        System.out.println("==========");

        dumpXsd(bs3Grid);
    }

    protected void dumpXsd(final BS3Grid bs3Page) {
        Map<String, String> schemas = jaxbService.toXsd(bs3Page, JaxbService.IsisSchemas.INCLUDE);
        for (Map.Entry<String, String> entry : schemas.entrySet()) {
            System.out.println(entry.getKey() + ":");
            System.out.println(entry.getValue());
        }
    }
}