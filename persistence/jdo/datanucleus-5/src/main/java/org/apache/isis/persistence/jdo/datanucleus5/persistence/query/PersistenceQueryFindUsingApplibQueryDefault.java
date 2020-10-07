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

package org.apache.isis.persistence.jdo.datanucleus5.persistence.query;

import java.util.Collections;
import java.util.Map;

import org.apache.isis.applib.query.Query;
import org.apache.isis.core.metamodel.commons.ToString;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.persistence.jdo.datanucleus5.objectadapter.ObjectAdapter;

/**
 * Corresponds to an object-store specific implementation of {@link Query}.
 */
public class PersistenceQueryFindUsingApplibQueryDefault extends PersistenceQueryAbstract {

    private final String queryName;
    private final QueryCardinality cardinality;
    private final Map<String, ObjectAdapter> argumentsAdaptersByParameterName;

    public PersistenceQueryFindUsingApplibQueryDefault(
            final ObjectSpecification specification,
            final String queryName,
            final Map<String, ObjectAdapter> argumentsAdaptersByParameterName,
            final QueryCardinality cardinality,
            final SpecificationLoader specificationLoader,
            final long... range) {
        super(specification, range);
        this.queryName = queryName;
        this.cardinality = cardinality;
        this.argumentsAdaptersByParameterName = argumentsAdaptersByParameterName;
    }



    public String getQueryName() {
        return queryName;
    }

    public Map<String, ObjectAdapter> getArgumentsAdaptersByParameterName() {
        return Collections.unmodifiableMap(argumentsAdaptersByParameterName);
    }

    public QueryCardinality getCardinality() {
        return cardinality;
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
}
