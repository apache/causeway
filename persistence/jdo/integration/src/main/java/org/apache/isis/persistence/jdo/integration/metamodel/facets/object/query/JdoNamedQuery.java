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
package org.apache.isis.persistence.jdo.integration.metamodel.facets.object.query;

import javax.jdo.annotations.Query;

import org.apache.isis.core.metamodel.services.metamodel.MetaModelExportSupport;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Value object that represents the information of a
 * {@link javax.jdo.annotations.Query}.
 *
 * @see {@link JdoQueryFacet}.
 */
public final class JdoNamedQuery implements MetaModelExportSupport {

    private final String name;
    private final String query;
    private final String language;
    private final ObjectSpecification objSpec;

    private JdoNamedQuery(
            final String name,
            final String query,
            final String language,
            final ObjectSpecification noSpec) {
        this.language = language;

        assert name != null;
        assert query != null;
        assert noSpec != null;

        this.name = name;
        this.query = query;
        this.objSpec = noSpec;
    }

    public JdoNamedQuery(
            final Query jdoNamedQuery,
            final ObjectSpecification objSpec) {
        this(jdoNamedQuery.name(), jdoNamedQuery.value(), jdoNamedQuery.language(), objSpec);
    }

    public String getName() {
        return name;
    }

    public String getQuery() {
        return query;
    }

    public String getLanguage() {
        return language;
    }

    public ObjectSpecification getObjectSpecification() {
        return objSpec;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JdoNamedQuery other = (JdoNamedQuery) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toMetamodelString() {
        return getName();
    }

}
