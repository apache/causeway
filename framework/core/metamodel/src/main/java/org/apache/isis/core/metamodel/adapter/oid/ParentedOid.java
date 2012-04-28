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

package org.apache.isis.core.metamodel.adapter.oid;

import java.io.IOException;
import java.io.Serializable;

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;

/**
 * Base type of the {@link Oid} for collections, values and <tt>@Aggregated</tt>
 * types.
 * 
 * @see AggregatedOid
 */
public abstract class ParentedOid implements Oid, Serializable {

    private static final long serialVersionUID = 1L;

    private final Oid parentOid;

    // /////////////////////////////////////////////////////////
    // Constructor
    // /////////////////////////////////////////////////////////

    public ParentedOid(final Oid parentOid) {
        this.parentOid = parentOid;
    }

    
    // ////////////////////////////////////////////
    // Encodeable
    // ////////////////////////////////////////////

    public ParentedOid(final DataInputExtended input) throws IOException {
        final String type = input.readUTF();
        this.parentOid = recreateParentOid(type, input);
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        final String type = determineParentType();
        output.writeUTF(type);
        this.parentOid.encode(output);
    }

    
    private String determineParentType() {
        if(parentOid instanceof RootOid) { return "R"; }
        if(parentOid instanceof AggregatedOid) { return "A"; }
        throw new IllegalStateException("Unknown parent Oid type; parentOid class is: " + parentOid.getClass().getName());
    }

    private static Oid recreateParentOid(String type, DataInputExtended input) throws IOException {
        if(type.equals("R")) { return new RootOidDefault(input); }
        if(type.equals("A")) { return new AggregatedOid(input); }
        throw new IllegalArgumentException("Unknown parent Oid type: " + type);
    }


    // /////////////////////////////////////////////////////////
    // Properties
    // /////////////////////////////////////////////////////////

    public Oid getParentOid() {
        return parentOid;
    }

    @Override
    public boolean isTransient() {
        return getParentOid().isTransient();
    }


}
