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
import org.apache.isis.applib.query.QueryBuiltInAbstract;
import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.persistence.PersistenceQuery;

/**
 * Corresponds to {@link QueryBuiltInAbstract}.
 *
 * <p>
 * REVIEW: now that we've dropped remoting, could we get rid of the {@link PersistenceQuery} hierarchy
 * classes and just use the applib {@link Query} throughout?
 */
public abstract class PersistenceQueryBuiltInAbstract extends PersistenceQueryAbstract implements PersistenceQueryBuiltIn {

    protected long index;
    protected long countedSoFar;

    public PersistenceQueryBuiltInAbstract(
            final ObjectSpecification specification,
            final SpecificationLoader specificationLoader,
            final long... range) {
        super(specification, specificationLoader, range);
    }

    public PersistenceQueryBuiltInAbstract(
            final DataInputExtended input,
            final SpecificationLoader specificationLoader,
            final long... range) throws IOException {
        super(input, specificationLoader, range);
    }

    protected boolean matchesRange(final boolean ifMatches) {
        if (!ifMatches){
            return false;
        }
        
        if (getCount() == 0 && getStart() == 0){
            return true;
        }
        if (index++ < start){
            return false;
        }
        if (countedSoFar++ < count){
            return true;
        }
        return false;
    }
    
}
