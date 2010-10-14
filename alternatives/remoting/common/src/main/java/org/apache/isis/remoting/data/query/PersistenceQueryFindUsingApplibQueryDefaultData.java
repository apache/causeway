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


package org.apache.isis.remoting.data.query;

import java.util.Map;

import org.apache.isis.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.remoting.data.common.ObjectData;
import org.apache.isis.runtime.persistence.query.PersistenceQueryFindUsingApplibQueryDefault;

/**
 * Serializable representation of {@link PersistenceQueryFindUsingApplibQueryDefault}.
 */
public class PersistenceQueryFindUsingApplibQueryDefaultData extends PersistenceQueryDataAbstract {
	
    private static final long serialVersionUID = 1L;
    private final Map<String, ObjectData> argumentDatasByParameterName;
	private final QueryCardinality cardinality;
	private final String queryName;
	
    public PersistenceQueryFindUsingApplibQueryDefaultData(
    		final ObjectSpecification noSpec, 
    		final String queryName,
    		final Map<String, ObjectData> argumentDatasByParameterName, 
    		final QueryCardinality cardinality) {
        super(noSpec);
        this.queryName = queryName;
        this.argumentDatasByParameterName = argumentDatasByParameterName;
        this.cardinality = cardinality;
    }

	public String getQueryName() {
		return queryName;
	}
    
	public Map<String, ObjectData> getArgumentDatasByParameterName() {
		return argumentDatasByParameterName;
	}
	
	public QueryCardinality getCardinality() {
		return cardinality;
	}

	public Class<?> getPersistenceQueryClass() {
		return PersistenceQueryFindUsingApplibQueryDefault.class;
	}
	
}

