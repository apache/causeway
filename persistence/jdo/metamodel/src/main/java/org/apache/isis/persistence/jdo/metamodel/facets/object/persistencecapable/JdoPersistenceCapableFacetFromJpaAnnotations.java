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
package org.apache.isis.persistence.jdo.metamodel.facets.object.persistencecapable;

import java.util.Optional;
import java.util.function.BiConsumer;

import javax.jdo.annotations.IdentityType;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.persistence.jdo.provider.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

import lombok.val;

public class JdoPersistenceCapableFacetFromJpaAnnotations
extends FacetAbstract
implements JdoPersistenceCapableFacet {

    private final String schema;
    private final String table;
    private final IdentityType identityType;

    public static Optional<JdoPersistenceCapableFacet> create(
            final Optional<Entity> entityIfAny,
            final Optional<Table> tableIfAny,
            final Class<?> cls,
            final FacetHolder facetHolder) {

        if(!entityIfAny.isPresent()) {
            return Optional.empty();
        }

        //val entity = entityIfAny.get(); // optionally has a name, we don't use yet

        val table = tableIfAny
            .map(tableAnnot->tableAnnot.name())
            .map(_Strings::emptyToNull)
            .orElseGet(cls::getSimpleName);

        val schema = tableIfAny
                .map(tableAnnot->tableAnnot.schema())
                .map(_Strings::emptyToNull)
                .orElseGet(cls::getSimpleName);


        val identityType = IdentityType.UNSPECIFIED;

        return Optional.of(new JdoPersistenceCapableFacetFromJpaAnnotations(
                schema,
                table,
                identityType,
                facetHolder));

    }

    private JdoPersistenceCapableFacetFromJpaAnnotations(
            final String schemaName,
            final String tableOrTypeName,
            final IdentityType identityType,
            final FacetHolder holder) {

        super(JdoPersistenceCapableFacet.class, holder);
        this.schema = schemaName;
        this.table = tableOrTypeName;
        this.identityType = identityType;
    }

    @Override
    public IdentityType getIdentityType() {
        return identityType;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public String getTable() {
        return table;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("schema", schema);
        visitor.accept("table", table);
        visitor.accept("identityType", identityType);
    }


}
