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

package org.apache.isis.persistence.jdo.integration.persistence.query;

import java.util.Map;

import org.apache.isis.applib.query.Query;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.commons.ToString;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.persistence.jdo.integration.persistence.queries.PersistenceQueryContext;
import org.apache.isis.persistence.jdo.integration.persistence.queries.PersistenceQueryFindUsingApplibQueryProcessor;

import lombok.Getter;

/**
 * Corresponds to an object-store specific implementation of {@link Query}.
 */
public class PersistenceQueryFindUsingApplibQueryDefault extends PersistenceQueryAbstract {

    @Getter private final String queryName;
    @Getter private final QueryCardinality cardinality;
    @Getter private final Map<String, Object> queryParametersByName;

    public PersistenceQueryFindUsingApplibQueryDefault(
            final ObjectSpecification specification,
            final String queryName,
            final Map<String, Object> queryParametersByName,
            final QueryCardinality cardinality,
            final SpecificationLoader specificationLoader,
            final long... range) {
        super(specification, range);
        this.queryName = queryName;
        this.cardinality = cardinality;
        this.queryParametersByName = queryParametersByName;
    }

    @Override
    public String toString() {
        final ToString str = ToString.createAnonymous(this);
        str.append("spec", getSpecification().getShortIdentifier());
        return str.toString();
    }

    public long getEnd() {
        // we default to Integer.MAX_VALUE because HSQLDB blows up
        // (with a ClassCastException from Long to Integer)
        // if we return Long.MAX_VALUE
        return getCount() != 0? getStart() + getCount(): Integer.MAX_VALUE;
    }

    public boolean hasRange() {
        return getStart() != 0 || getCount() != 0;
    }

    @Override
    public Can<ManagedObject> execute(PersistenceQueryContext queryContext) {
        return new PersistenceQueryFindUsingApplibQueryProcessor().process(queryContext, this);
    }
}
