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

import java.util.Arrays;
import java.util.List;

import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.grid.bootstrap.BSCol;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.layout.grid.bootstrap.BSRow;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.resources._Resources;

import lombok.extern.slf4j.Slf4j;

@Slf4j
record FallbackGridProvider(
    GridLoadingContext context) {

    public BSGrid defaultGrid(final Class<?> domainClass) {
        final Try<String> content = loadFallbackLayoutAsStringUtf8(domainClass);
        try {
            return content.getValue()
                    .flatMap(xml -> context.gridMarshaller(CommonMimeType.XML).orElseThrow()
                        .unmarshal(domainClass, xml, CommonMimeType.XML)
                        .getValue())
                    .filter(BSGrid.class::isInstance)
                    .map(BSGrid.class::cast)
                    .map(bsGrid->bsGrid.fallback(true))
                    .orElseGet(() -> fallback(domainClass));
        } catch (final Exception e) {
            return fallback(domainClass);
        }
    }

    // -- HELPER

    private Try<String> loadFallbackLayoutAsStringUtf8(final Class<?> domainClass) {
        return Try.call(()->_Resources.loadAsStringUtf8(FallbackGridProvider.class, "GridFallbackLayout.xml"));
    }

    //
    // only ever called if fail to load GridFallbackLayout.xml,
    // which *really* shouldn't happen
    //
    private BSGrid fallback(final Class<?> domainClass) {
        final BSGrid bsGrid = new BSGrid();
        bsGrid.domainClass(domainClass).fallback(true);

        final BSRow headerRow = new BSRow();
        bsGrid.getRows().add(headerRow);
        final BSCol headerRowCol = new BSCol();
        headerRowCol.setSpan(12);
        headerRowCol.setUnreferencedActions(true);
        headerRowCol.setDomainObject(new DomainObjectLayoutData());
        headerRow.getRowContents().add(headerRowCol);

        final BSRow propsRow = new BSRow();
        bsGrid.getRows().add(propsRow);

        // if no layout hints
        addFieldSetsToColumn(propsRow, 4, Arrays.asList("General"), true);

        final BSCol col = new BSCol();
        col.setUnreferencedCollections(true);
        col.setSpan(12);
        propsRow.getRowContents().add(col);

        return bsGrid;
    }

    private static void addFieldSetsToColumn(
            final BSRow propsRow,
            final int span,
            final List<String> memberGroupNames,
            final boolean unreferencedProperties) {

        if(span > 0 || unreferencedProperties) {
            final BSCol col = new BSCol();
            col.setSpan(span); // in case we are here because of 'unreferencedProperties' needs setting
            propsRow.getRowContents().add(col);
            final List<String> leftMemberGroups = memberGroupNames;
            for (String memberGroup : leftMemberGroups) {
                final FieldSet fieldSet = new FieldSet();
                fieldSet.setName(memberGroup);
                // fieldSet's id will be derived from the name later
                // during normalization phase.
                if(unreferencedProperties && col.getFieldSets().isEmpty()) {
                    fieldSet.setUnreferencedProperties(true);
                }
                col.getFieldSets().add(fieldSet);
            }
        }
    }

}
