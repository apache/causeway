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

import org.apache.isis.alternatives.remoting.common.data.common.ObjectData;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindByPattern;

/**
 * Serializable representation of {@link PersistenceQueryFindByPattern}.
 */
public class PersistenceQueryFindByPatternData extends PersistenceQueryDataAbstract {
	
    private static final long serialVersionUID = 1L;
    private final ObjectData patternData;

    public PersistenceQueryFindByPatternData(
    		final ObjectSpecification noSpec, final ObjectData patternData) {
        super(noSpec);
        this.patternData = patternData;
    }

    public ObjectData getPatternData() {
        return patternData;
    }

    public Class<?> getPersistenceQueryClass() {
        return PersistenceQueryFindByPattern.class;
    }
}

