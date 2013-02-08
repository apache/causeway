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

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.encoding.Encodable;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceQuery;

public abstract class PersistenceQueryAbstract implements PersistenceQuery, Encodable {

    protected final long start;
    protected final long count;
    
    private final ObjectSpecification specification;

    public PersistenceQueryAbstract(final ObjectSpecification specification, final long ... range) {
        this.start = range.length > 0 ? range[0]:0;
        this.count = range.length > 1 ? range[1]:0;
        
        this.specification = specification;        
        initialized();
    }

    protected PersistenceQueryAbstract(final DataInputExtended input, final long ... range) throws IOException {
        final String specName = input.readUTF();
        this.start = range.length > 0 ? range[0]:0;
        this.count = range.length > 1 ? range[1]:0;
        
        specification = getSpecificationLoader().loadSpecification(specName);
        initialized();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        output.writeUTF(specification.getFullIdentifier());
    }

    private void initialized() {
        // nothing to do
    }
    
    /**
     * The start index into the set table
     * @return
     */
    public long getStart() {
        return start;
    }


    /**
     * The number of items to return, starting at {@link QueryFindAllPaged#getStart()}
     * @return
     */
    public long getCount() {
        return count;
    }
    

    // ///////////////////////////////////////////////////////
    //
    // ///////////////////////////////////////////////////////

    @Override
    public ObjectSpecification getSpecification() {
        return specification;
    }

    // ///////////////////////////////////////////////////////
    // equals, hashCode
    // ///////////////////////////////////////////////////////

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PersistenceQueryAbstract other = (PersistenceQueryAbstract) obj;
        if (specification == null) {
            if (other.specification != null) {
                return false;
            }
        } else if (!specification.equals(other.specification)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + 1231;
        result = PRIME * result + ((specification == null) ? 0 : specification.hashCode());
        return result;
    }

    // ///////////////////////////////////////////////////////
    // Dependencies (from context)
    // ///////////////////////////////////////////////////////

    protected static SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

}
