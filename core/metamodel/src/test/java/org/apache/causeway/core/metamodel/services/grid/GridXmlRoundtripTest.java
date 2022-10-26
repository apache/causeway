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
package org.apache.causeway.core.metamodel.services.grid;

import java.util.Map;

import javax.xml.bind.Marshaller;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.bootstrap.BSCol;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.layout.grid.bootstrap.BSRow;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTab;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTabGroup;
import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.applib.services.jaxb.CausewaySchemas;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.core.metamodel.MetaModelTestAbstract;

class GridXmlRoundtripTest
extends MetaModelTestAbstract {

    private JaxbService jaxbService;
    private GridServiceDefault gridServiceDefault;

    @Override
    protected void afterSetUp() {
        jaxbService = getServiceRegistry().lookupServiceElseFail(JaxbService.class);
        gridServiceDefault = (GridServiceDefault)getServiceRegistry().lookupServiceElseFail(GridService.class);
    }

    @Test
    void happy_case() throws Exception {

        final BSGrid bsGrid = new BSGrid();

        // header
        final BSRow headerRow = new BSRow();
        bsGrid.getRows().add(headerRow);
        final BSCol headerCol = new BSCol();
        headerRow.getCols().add(headerCol);
        headerCol.setSpan(12);

        final DomainObjectLayoutData objectLayoutData = new DomainObjectLayoutData();
        headerCol.setDomainObject(objectLayoutData);

        final ActionLayoutData deleteActionLayoutData = new ActionLayoutData();
        deleteActionLayoutData.setId("delete");
        headerCol.setActions(_Lists.<ActionLayoutData>newArrayList());
        headerCol.getActions().add(deleteActionLayoutData);

        // content
        final BSRow contentRow = new BSRow();
        bsGrid.getRows().add(contentRow);

        final BSCol contentCol = new BSCol();
        contentRow.getCols().add(contentCol);
        contentCol.setSpan(12);

        // a tabgroup containing a 'Common' tab
        final BSTabGroup tabGroup = new BSTabGroup();
        contentCol.getTabGroups().add(tabGroup);
        BSTab bsTab = new BSTab();
        tabGroup.getTabs().add(bsTab);
        bsTab.setName("Common");

        // with a left col...
        final BSRow tabRow = new BSRow();
        bsTab.getRows().add(tabRow);
        final BSCol tabLeftCol = new BSCol();
        tabRow.getCols().add(tabLeftCol);
        tabLeftCol.setSpan(6);

        // containing a fieldset
        final FieldSet leftPropGroup = new FieldSet("General");
        tabLeftCol.setFieldSets(_Lists.<FieldSet>newArrayList());
        tabLeftCol.getFieldSets().add(leftPropGroup);
        leftPropGroup.setName("General");

        // with a single property
        final PropertyLayoutData namePropertyLayoutData = new PropertyLayoutData();
        leftPropGroup.getProperties().add(namePropertyLayoutData);
        namePropertyLayoutData.setNamed("name");

        // and its associated action
        final ActionLayoutData updateNameActionLayoutData = new ActionLayoutData();
        updateNameActionLayoutData.setId("updateName");
        namePropertyLayoutData.setActions(_Lists.<ActionLayoutData>newArrayList());
        namePropertyLayoutData.getActions().add(updateNameActionLayoutData);

        // and the tab also has a right col...
        final BSCol tabRightCol = new BSCol();
        tabRow.getCols().add(tabRightCol);
        tabRightCol.setSpan(6);

        // containing a collection
        final CollectionLayoutData similarToColl = new CollectionLayoutData();
        tabRightCol.setCollections(_Lists.<CollectionLayoutData>newArrayList());
        tabRightCol.getCollections().add(similarToColl);
        similarToColl.setId("similarTo");

        final String schemaLocations = gridServiceDefault.tnsAndSchemaLocation(bsGrid);
        String xml = jaxbService.toXml(bsGrid,
                _Maps.unmodifiable(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocations));
        println(xml);

        BSGrid bsPageroundtripped = jaxbService.fromXml(BSGrid.class, xml);
        String xmlRoundtripped = jaxbService.toXml(bsPageroundtripped,
                _Maps.unmodifiable(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocations));
        assertThat(xml, is(equalTo(xmlRoundtripped)));


        println("==========");

        dumpXsd(bsGrid);
    }

    protected void dumpXsd(final BSGrid bsPage) {
        Map<String, String> schemas = jaxbService.toXsd(bsPage, CausewaySchemas.INCLUDE);
        for (Map.Entry<String, String> entry : schemas.entrySet()) {
            println(entry.getKey() + ":");
            println(entry.getValue());
        }
    }

    private void println(String string) {
        //for test debugging only
    }

}
