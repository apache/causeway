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
package org.apache.isis.persistence.jdo.provider.metamodel.facets.object.query;

import javax.annotation.Nullable;
import javax.jdo.annotations.Query;

import org.apache.isis.core.metamodel.services.metamodel.MetaModelExportSupport;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Value object that represents the information of a
 * {@link javax.jdo.annotations.Query}.
 *
 * @see org.apache.isis.persistence.jdo.provider.metamodel.facets.object.query.JdoQueryFacet
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString @EqualsAndHashCode
public final class JdoNamedQuery implements MetaModelExportSupport {

    private final @NonNull  String name;
    private final @NonNull  String query;
    private final @Nullable String language;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final @NonNull  ObjectSpecification objectSpecification;

    public JdoNamedQuery(
            final Query jdoNamedQuery,
            final ObjectSpecification objSpec) {
        this(jdoNamedQuery.name(), jdoNamedQuery.value(), jdoNamedQuery.language(), objSpec);
    }

    @Override
    public String toMetamodelString() {
        return getName();
    }

}
