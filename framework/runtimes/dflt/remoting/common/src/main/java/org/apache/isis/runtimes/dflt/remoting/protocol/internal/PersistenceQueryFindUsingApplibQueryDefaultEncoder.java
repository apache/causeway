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

package org.apache.isis.runtimes.dflt.remoting.protocol.internal;

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.remoting.common.data.common.ObjectData;
import org.apache.isis.runtimes.dflt.remoting.common.data.query.PersistenceQueryData;
import org.apache.isis.runtimes.dflt.remoting.common.data.query.PersistenceQueryFindUsingApplibQueryDefaultData;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByTitle;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindUsingApplibQueryDefault;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;

public class PersistenceQueryFindUsingApplibQueryDefaultEncoder extends PersistenceQueryEncoderAbstract {

    @Override
    public Class<?> getPersistenceQueryClass() {
        return PersistenceQueryFindByTitle.class;
    }

    @Override
    public PersistenceQueryData encode(final PersistenceQuery persistenceQuery) {
        final PersistenceQueryFindUsingApplibQueryDefault findByQuery = downcast(persistenceQuery);
        return new PersistenceQueryFindUsingApplibQueryDefaultData(findByQuery.getSpecification(),
            findByQuery.getQueryName(), encode(findByQuery.getArgumentsAdaptersByParameterName()),
            findByQuery.getCardinality());
    }

    @Override
    protected PersistenceQuery doDecode(final ObjectSpecification specification,
        final PersistenceQueryData persistenceQueryData) {
        final PersistenceQueryFindUsingApplibQueryDefaultData findByQueryData = downcast(persistenceQueryData);
        return new PersistenceQueryFindUsingApplibQueryDefault(specification, findByQueryData.getQueryName(),
            decode(findByQueryData.getArgumentDatasByParameterName()), (findByQueryData.getCardinality()));
    }

    private Map<String, ObjectData> encode(final Map<String, ObjectAdapter> argumentsAdaptersByParameterName) {
        final Map<String, ObjectData> argumentDatasByParameterName = new HashMap<String, ObjectData>();
        for (final Map.Entry<String, ObjectAdapter> entry : argumentsAdaptersByParameterName.entrySet()) {
            final String parameterName = entry.getKey();
            final ObjectAdapter adapter = entry.getValue();
            argumentDatasByParameterName.put(parameterName, encodeObject(adapter));
        }
        return argumentDatasByParameterName;
    }

    private Map<String, ObjectAdapter> decode(final Map<String, ObjectData> argumentDatasByParameterName) {
        final Map<String, ObjectAdapter> argumentAdaptersByParameterName = new HashMap<String, ObjectAdapter>();
        for (final Map.Entry<String, ObjectData> entry : argumentDatasByParameterName.entrySet()) {
            final String parameterName = entry.getKey();
            final ObjectData data = entry.getValue();
            argumentAdaptersByParameterName.put(parameterName, decodeObject(data));
        }
        return argumentAdaptersByParameterName;
    }

    private PersistenceQueryFindUsingApplibQueryDefault downcast(final PersistenceQuery persistenceQuery) {
        return (PersistenceQueryFindUsingApplibQueryDefault) persistenceQuery;
    }

    private PersistenceQueryFindUsingApplibQueryDefaultData downcast(final PersistenceQueryData persistenceQueryData) {
        return (PersistenceQueryFindUsingApplibQueryDefaultData) persistenceQueryData;
    }

}
