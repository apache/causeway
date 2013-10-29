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

package org.apache.isis.core.runtime.persistence.query;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.isis.applib.query.Query;
import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Corresponds to an object-store specific implementation of {@link Query}.
 */
public class PersistenceQueryFindUsingApplibQueryDefault extends PersistenceQueryAbstract {

    private final String queryName;
    private final QueryCardinality cardinality;
    private final Map<String, ObjectAdapter> argumentsAdaptersByParameterName;

    public PersistenceQueryFindUsingApplibQueryDefault(final ObjectSpecification specification, final String queryName, final Map<String, ObjectAdapter> argumentsAdaptersByParameterName, final QueryCardinality cardinality, final long ... range) {
        super(specification, range);
        this.queryName = queryName;
        this.cardinality = cardinality;
        this.argumentsAdaptersByParameterName = argumentsAdaptersByParameterName;
        initialized();
    }

    public PersistenceQueryFindUsingApplibQueryDefault(final DataInputExtended input, final long ... range) throws IOException {
        super(input, range);
        this.queryName = input.readUTF();
        this.cardinality = QueryCardinality.valueOf(input.readUTF());
        // TODO: need to read from input
        this.argumentsAdaptersByParameterName = new HashMap<String, ObjectAdapter>();
        initialized();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        super.encode(output);
        output.writeUTF(queryName);
        output.writeUTF(cardinality.name());
        // TODO: need to write to output
        // ... this.argumentsAdaptersByParameterName....

    }

    private void initialized() {
        // nothing to do
    }

    // ///////////////////////////////////////////////////////
    //
    // ///////////////////////////////////////////////////////

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
