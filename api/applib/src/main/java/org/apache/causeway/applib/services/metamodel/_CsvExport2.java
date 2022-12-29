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
package org.apache.causeway.applib.services.metamodel;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.schema.metamodel.v2.Action;
import org.apache.causeway.schema.metamodel.v2.Collection;
import org.apache.causeway.schema.metamodel.v2.DomainClassDto;
import org.apache.causeway.schema.metamodel.v2.MetamodelDto;
import org.apache.causeway.schema.metamodel.v2.Property;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _CsvExport2 {

    StringBuilder toCsv(final MetamodelDto metamodelDto) {
        val sb = new StringBuilder();

        sb.append(header()).append("\n");

        metamodelDto
            .getDomainClassDto()
            .forEach(typeDto->appendCsvRow(typeDto, sb));

        return sb;
    }

    // -- HELPER

    private String header() {
        return "logicalType,service?,memberType,memberId,numParams,mixedIn?";
    }

    private void appendCsvRow(final DomainClassDto typeDto, final StringBuilder sb) {

        Optional.ofNullable(typeDto.getProperties())
        .map(props->props.getProp())
        .ifPresent(list->list.forEach(propDto->appendCsvRow(typeDto, propDto, sb)));

        Optional.ofNullable(typeDto.getCollections())
        .map(colls->colls.getColl())
        .ifPresent(list->list.forEach(collDto->appendCsvRow(typeDto, collDto, sb)));

        Optional.ofNullable(typeDto.getActions())
        .map(acts->acts.getAct())
        .ifPresent(list->list.forEach(actDto->appendCsvRow(typeDto, actDto, sb)));

        typeDto.getCollections();
        typeDto.getActions();
    }

    private void appendCsvRow(final DomainClassDto typeDto, final Property prop, final StringBuilder sb) {
        val rowString =  Stream.of(
                typeDto.getId(),
                Optional.ofNullable(typeDto.isService()).orElse(false) ? "Y" : "",
                "prop",
                prop.getId(),
                "",
                prop.isMixedIn() ? "Y" : "")
                .collect(Collectors.joining(","));

        sb.append(rowString).append("\n");
    }

    private void appendCsvRow(final DomainClassDto typeDto, final Collection coll, final StringBuilder sb) {
        val rowString =  Stream.of(
                typeDto.getId(),
                Optional.ofNullable(typeDto.isService()).orElse(false) ? "Y" : "",
                "coll",
                coll.getId(),
                "",
                coll.isMixedIn() ? "Y" : "")
                .collect(Collectors.joining(","));

        sb.append(rowString).append("\n");
    }

    private void appendCsvRow(final DomainClassDto typeDto, final Action act, final StringBuilder sb) {
        val rowString =  Stream.of(
                typeDto.getId(),
                Optional.ofNullable(typeDto.isService()).orElse(false) ? "Y" : "",
                "act",
                act.getId(),
                Optional.ofNullable(act.getParams())
                    .map(params->"" + _NullSafe.size(params.getParam()))
                    .orElse("0"),
                act.isMixedIn() ? "Y" : "")
                .collect(Collectors.joining(","));

        sb.append(rowString).append("\n");
    }

}
