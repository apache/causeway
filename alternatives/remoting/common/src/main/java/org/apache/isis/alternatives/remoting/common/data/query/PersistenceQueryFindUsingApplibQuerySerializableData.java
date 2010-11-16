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


package org.apache.isis.alternatives.remoting.common.data.query;

import java.io.Serializable;

import org.apache.isis.applib.query.Query;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindUsingApplibQuerySerializable;

/**
 * Serializable representation of {@link PersistenceQueryFindUsingApplibQuerySerializable}.
 */
public class PersistenceQueryFindUsingApplibQuerySerializableData extends PersistenceQueryDataAbstract {
	
    private static final long serialVersionUID = 1L;
    private final Serializable querySerializable;
	private final QueryCardinality cardinality;
	
    public PersistenceQueryFindUsingApplibQuerySerializableData(
    		final ObjectSpecification noSpec, 
    		final Query applibQuery, QueryCardinality cardinality) {
        super(noSpec);
        querySerializable = applibQuery;
        this.cardinality = cardinality;
    }

    public Class<?> getPersistenceQueryClass() {
        return PersistenceQueryFindUsingApplibQuerySerializable.class;
    }

    public Serializable getApplibQuerySerializable() {
		return querySerializable;
	}

	public QueryCardinality getCardinality() {
		return cardinality;
	}

}

