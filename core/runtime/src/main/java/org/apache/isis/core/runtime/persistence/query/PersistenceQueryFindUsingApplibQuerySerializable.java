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

import org.apache.isis.applib.query.Query;
import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

/**
 * Corresponds to an object-store specific implementation of {@link Query}.
 */
public class PersistenceQueryFindUsingApplibQuerySerializable extends PersistenceQueryAbstract {

    private final Query<?> query;
    private final QueryCardinality cardinality;

    public PersistenceQueryFindUsingApplibQuerySerializable(
            final ObjectSpecification specification,
            final Query<?> query,
            final QueryCardinality cardinality,
            final SpecificationLoader specificationLoader) {
        super(specification, specificationLoader, query.getStart(), query.getCount());
        this.query = query;
        this.cardinality = cardinality;
        initialized();
    }

    public PersistenceQueryFindUsingApplibQuerySerializable(
            final DataInputExtended input,
            final SpecificationLoader specificationLoader,
            final long... range) throws IOException {
        super(input, specificationLoader, range);
        this.query = input.readSerializable(Query.class);
        this.cardinality = QueryCardinality.valueOf(input.readUTF());
        initialized();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        super.encode(output);
        output.writeSerializable(query);
        output.writeUTF(cardinality.name());
    }

    private void initialized() {
        // nothing to do
    }

    // ///////////////////////////////////////////////////////

    public Query getApplibQuery() {
        return query;
    }

    public QueryCardinality getCardinality() {
        return cardinality;
    }

    @Override
    public String toString() {
        final ToString str = ToString.createAnonymous(this);
        str.append("spec", getSpecification().getShortIdentifier());
        str.append("query", query.getDescription());
        return str.toString();
    }
}
