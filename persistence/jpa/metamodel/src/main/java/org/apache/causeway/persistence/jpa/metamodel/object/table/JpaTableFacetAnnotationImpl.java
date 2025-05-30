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
package org.apache.causeway.persistence.jpa.metamodel.object.table;

import java.util.function.BiConsumer;

import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

public class JpaTableFacetAnnotationImpl
extends FacetAbstract
implements JpaTableFacetAnnotation {

    private final String schema;
    private final String tableOrTypeName;

    public JpaTableFacetAnnotationImpl(
            final String schemaName,
            final String tableOrTypeName,
            final FacetHolder holder) {

        super(JpaTableFacetAnnotation.class, holder);
        this.schema = schemaName;
        this.tableOrTypeName = tableOrTypeName;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public String getTable() {
        return tableOrTypeName;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("schema", schema);
        visitor.accept("table", tableOrTypeName);
    }

}
