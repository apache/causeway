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

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.schema.metamodel.v2.Collection;
import org.apache.causeway.schema.metamodel.v2.DomainClassDto;
import org.apache.causeway.schema.metamodel.v2.FacetAttr;
import org.apache.causeway.schema.metamodel.v2.FacetHolder.Facets;
import org.apache.causeway.schema.metamodel.v2.MetamodelDto;
import org.apache.causeway.schema.metamodel.v2.Property;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _AsciiExport {

    StringBuilder toAscii(MetamodelDto metamodelDto) {
        val sb = new StringBuilder();

        metamodelDto
            .getDomainClassDto()
            .forEach(typeDto->toAscii(typeDto, sb));

        return sb;
    }

    // -- HELPER

    private void toAscii(DomainClassDto typeDto, StringBuilder sb) {

        sb.append(typeDto.getId()).append("\n");

        toAscii("  * ", typeDto.getFacets(), sb);

        Optional.ofNullable(typeDto.getProperties())
        .map(props->props.getProp())
        .ifPresent(list->list.forEach(propDto->toAscii(propDto, sb)));

        Optional.ofNullable(typeDto.getCollections())
        .map(colls->colls.getColl())
        .ifPresent(list->list.forEach(collDto->toAscii(collDto, sb)));

        Optional.ofNullable(typeDto.getActions())
        .map(acts->acts.getAct())
        .ifPresent(list->list.forEach(actDto->toAscii(actDto, sb)));

    }

    private void toAscii(final String prefix, @Nullable Facets facets, final StringBuilder sb) {

        val attrPrefix = _Strings.of(prefix.length()-2, ' ') + "  | ";

        Optional.ofNullable(facets)
        .map(fac->fac.getFacet())
        .ifPresent(list->list.forEach(facetDto->{
            sb
                .append(prefix)
                .append(shorten(facetDto.getId()))
                .append("\n");

            Optional.ofNullable(facetDto.getAttr())
            .ifPresent(attrs->attrs.forEach(attr->{
                toAscii(attrPrefix, attr, sb);
            }));

        }));
    }

    private void toAscii(String attrPrefix, FacetAttr attr, StringBuilder sb) {
        if(attr.getName().equals("precedence")
                && attr.getValue().equals("DEFAULT")) {
            return; // suppress DEFAULT precedence
        }
        sb.append(attrPrefix).append(attr.getName()).append('=').append(attr.getValue()).append("\n");
    }

    private void toAscii(Property propDto, StringBuilder sb) {
        sb.append("+-- prop ").append(propDto.getId()).append("\n");
        toAscii  ("      * ", propDto.getFacets(), sb);
    }

    private void toAscii(Collection collDto, StringBuilder sb) {
        sb.append("+-- coll ").append(collDto.getId()).append("\n");
        toAscii  ("      * ", collDto.getFacets(), sb);
    }

    private void toAscii(org.apache.causeway.schema.metamodel.v2.Action actDto, StringBuilder sb) {
        sb.append("+-- act  ").append(actDto.getId()).append("\n");
        toAscii  ("      * ", actDto.getFacets(), sb);
    }

    private String shorten(String id) {
        return id.replace("org.apache.causeway.core.metamodel.facets", "..facets");
    }

}
