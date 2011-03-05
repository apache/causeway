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


package org.apache.isis.alternatives.remoting.protocol.internal;

import org.apache.isis.alternatives.remoting.common.data.common.ObjectData;
import org.apache.isis.alternatives.remoting.common.data.query.PersistenceQueryData;
import org.apache.isis.alternatives.remoting.common.data.query.PersistenceQueryFindByPatternData;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQuery;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByPattern;

public class PersistenceQueryFindByPatternEncoder extends PersistenceQueryEncoderAbstract {

    public Class<?> getPersistenceQueryClass() {
        return PersistenceQueryFindByPattern.class;
    }
    
    public PersistenceQueryData encode(
    		final PersistenceQuery persistenceQuery) {
        final PersistenceQueryFindByPattern patternPersistenceQuery = downcast(persistenceQuery);
        final ObjectAdapter pattern = patternPersistenceQuery.getPattern();
        final ObjectData objectData = encodeObject(pattern);
        return new PersistenceQueryFindByPatternData(patternPersistenceQuery.getSpecification(), objectData);
    }

    @Override
    protected PersistenceQuery doDecode(
            final ObjectSpecification specification,
            final PersistenceQueryData persistenceQueryData) {
        final ObjectData patternData = downcast(persistenceQueryData).getPatternData();
        final ObjectAdapter patternObject = decodeObject(patternData);
        return new PersistenceQueryFindByPattern(specification, patternObject);
    }

	private PersistenceQueryFindByPattern downcast(
			final PersistenceQuery persistenceQuery) {
		return (PersistenceQueryFindByPattern) persistenceQuery;
	}

	private PersistenceQueryFindByPatternData downcast(
			final PersistenceQueryData persistenceQueryData) {
		return (PersistenceQueryFindByPatternData)persistenceQueryData;
	}


}

