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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.commons.internal.collections._Lists;

import lombok.experimental.UtilityClass;

@UtilityClass
class _CsvExport {

    StringBuilder toCsv(DomainModel domainModel) {
        return asBuf(asList(domainModel));
    }

    // -- HELPER

    private StringBuilder asBuf(final List<String> list) {
        final StringBuilder buf = new StringBuilder();
        for (final String row : list) {
            buf.append(row).append("\n");
        }
        return buf;
    }

    private List<String> asList(final DomainModel model) {
        final List<String> list = _Lists.newArrayList();
        list.add(header());
        for (final DomainMember row : model.getDomainMembers()) {
            list.add(asTextCsv(row));
        }
        return list;
    }

    private String header() {
        return "classType,logicalTypeName,className,packageName,memberType,memberName,numParams,mixedIn?,mixin,hidden,disabled,choices,autoComplete,default,validate";
    }

    private String asTextCsv(final DomainMember row) {
        return Stream.of(
                row.getClassType(),
                row.getLogicalTypeName(),
                row.getClassName(),
                row.getPackageName(),
                row.getType(),
                row.getMemberName(),
                row.getNumParams(),
                row.isMixedIn() ? "Y" : "",
                row.getMixin(),
                row.getHidden(),
                row.getDisabled(),
                row.getChoices(),
                row.getAutoComplete(),
                row.getDefault(),
                row.getValidate())
                .collect(Collectors.joining(","));
    }


}
