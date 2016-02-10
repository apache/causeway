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
package org.apache.isis.core.metamodel.facets.object.grid;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.MemberGroupLayout;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.component.DomainObjectLayoutData;
import org.apache.isis.applib.layout.component.FieldSet;
import org.apache.isis.applib.layout.component.Grid;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Col;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Grid;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Row;
import org.apache.isis.applib.services.layout.GridService;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupLayoutFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

public class GridFacetDefault
            extends FacetAbstract
            implements GridFacet {

    private static final Logger LOG = LoggerFactory.getLogger(GridFacetDefault.class);


    public static Class<? extends Facet> type() {
        return GridFacet.class;
    }


    public static GridFacet create(
            final FacetHolder facetHolder,
            final GridService gridService,
            final DeploymentCategory deploymentCategory) {
        return new GridFacetDefault(facetHolder, gridService, deploymentCategory);
    }

    private final DeploymentCategory deploymentCategory;
    private final GridService gridService;

    private Grid grid;

    private GridFacetDefault(
            final FacetHolder facetHolder,
            final GridService gridService,
            final DeploymentCategory deploymentCategory) {
        super(GridFacetDefault.type(), facetHolder, Derivation.NOT_DERIVED);
        this.gridService = gridService;
        this.deploymentCategory = deploymentCategory;
    }

    public Grid getGrid() {
        if (deploymentCategory.isProduction() && this.grid != null) {
            return this.grid;
        }
        final Class<?> domainClass = getSpecification().getCorrespondingClass();
        Grid grid = gridService.fromXml(domainClass);
        if(grid == null) {
            grid = deriveGrid();
        }

        this.grid = normalize(grid);
        return this.grid;
    }

    private Grid deriveGrid() {
        final BS3Grid bs3Grid = new BS3Grid();
        bs3Grid.setDomainClass(getSpecification().getCorrespondingClass());

        final BS3Row headerRow = new BS3Row();
        bs3Grid.getRows().add(headerRow);
        final BS3Col headerRowCol = new BS3Col();
        headerRowCol.setSpan(12);
        headerRowCol.setUnreferencedActions(true);
        headerRowCol.setDomainObject(new DomainObjectLayoutData());
        headerRow.getCols().add(headerRowCol);

        final BS3Row propsRow = new BS3Row();
        bs3Grid.getRows().add(propsRow);

        final MemberGroupLayoutFacet memberGroupLayoutFacet =
                getSpecification().getFacet(MemberGroupLayoutFacet.class);
        if(memberGroupLayoutFacet != null) {
            // if have @MemberGroupLayout (or equally, a .layout.json file)
            final MemberGroupLayout.ColumnSpans columnSpans = memberGroupLayoutFacet.getColumnSpans();
            addFieldSetsToColumn(propsRow, columnSpans.getLeft(), memberGroupLayoutFacet.getLeft(), true);
            addFieldSetsToColumn(propsRow, columnSpans.getMiddle(), memberGroupLayoutFacet.getMiddle(), false);
            addFieldSetsToColumn(propsRow, columnSpans.getRight(), memberGroupLayoutFacet.getRight(), false);

            final BS3Col col = new BS3Col();
            final int collectionSpan = columnSpans.getCollections();
            col.setUnreferencedCollections(true);
            col.setSpan(collectionSpan > 0? collectionSpan: 12);
            propsRow.getCols().add(col);

            // will already be sorted per @MemberOrder
            final List<OneToManyAssociation> collections = getSpecification().getCollections(Contributed.INCLUDED);
            for (OneToManyAssociation collection : collections) {
                col.getCollections().add(new CollectionLayoutData(collection.getId()));
            }
        } else {

            // if no layout hints other than @MemberOrder
            addFieldSetsToColumn(propsRow, 4, Arrays.asList("General"), true);

            final BS3Col col = new BS3Col();
            col.setUnreferencedCollections(true);
            col.setSpan(12);
            propsRow.getCols().add(col);
        }
        return bs3Grid;
    }

    void addFieldSetsToColumn(
            final BS3Row propsRow,
            final int span,
            final List<String> memberGroupNames,
            final boolean unreferencedProperties) {

        if(span > 0 || unreferencedProperties) {
            final BS3Col col = new BS3Col();
            col.setSpan(span); // in case we are here because of 'unreferencedProperties' needs setting
            propsRow.getCols().add(col);
            final List<String> leftMemberGroups = memberGroupNames;
            for (String memberGroup : leftMemberGroups) {
                final FieldSet fieldSet = new FieldSet(memberGroup);
                if(unreferencedProperties && col.getFieldSets().isEmpty()) {
                    fieldSet.setUnreferencedProperties(true);
                }
                col.getFieldSets().add(fieldSet);
            }
        }
    }



    private Grid normalize(final Grid grid) {
        if(grid == null) {
            return null;
        }
        final Class<?> domainClass = getSpecification().getCorrespondingClass();

        return gridService.normalize(grid);
    }


    private ObjectSpecification getSpecification() {
        return (ObjectSpecification) getFacetHolder();
    }


}
