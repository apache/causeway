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
package org.apache.isis.persistence.jpa.metamodel.object.domainobject.objectspecid;

import java.util.Locale;

import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.logicaltype.LogicalTypeFacet;
import org.apache.isis.core.metamodel.facets.object.logicaltype.LogicalTypeFacetAbstract;
import org.apache.isis.persistence.jpa.metamodel.object.table.JpaTableFacetAnnotation;

public class LogicalTypeFacetForTableAnnotation
extends LogicalTypeFacetAbstract {

    public static LogicalTypeFacet create(
            final JpaTableFacetAnnotation tableFacet,
            final Class<?> correspondingClass,
            final FacetHolder holder) {

        if(tableFacet.isFallback()) {
            return null;
        }
        final String schema = tableFacet.getSchema();
        if(_Strings.isNullOrEmpty(schema)) {
            return null;
        }
        final String objectType = schema.toLowerCase(Locale.ROOT) + "." + tableFacet.getTable();
        return new LogicalTypeFacetForTableAnnotation(LogicalType.eager(correspondingClass, objectType), holder);
    }

    private LogicalTypeFacetForTableAnnotation(
            final LogicalType logicalType,
            final FacetHolder holder) {
        super(logicalType, holder);
    }
}
