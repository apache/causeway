/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.runtime.persistence.query;

import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Support fetching partial datasets from the datastore.
 *
 * @version $Rev$ $Date$
 */
public class PersistenceQueryFindPaged extends PersistenceQueryBuiltInAbstract {
    private final long start;
    private final long count;
    
    private long index;
    private long countedSoFar;
    public PersistenceQueryFindPaged(final ObjectSpecification specification, final long start, final long count) {
        super(specification);
        this.start = start;
        this.count = count;
        index=0;
        countedSoFar=0;
    }


    /**
     * Spoofing the limit function for datastores that don't support it directly.
     * 
     * Returns true so it matches all instances.
     */
    @Override
    public boolean matches(ObjectAdapter object) {
        if (index++ < start){
            return false;
        }
        if (countedSoFar++ <count){
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        final ToString str = ToString.createAnonymous(this);
        str.append("spec", getSpecification().getShortIdentifier());
        return str.toString();
    }


    /**
     * The start index into the set table
     * @return
     */
    public long getStart() {
        return start;
    }


    /**
     * The number of items to return, starting at {@link PersistenceQueryFindPaged#getStart()}
     * @return
     */
    public long getCount() {
        return count;
    }
}
