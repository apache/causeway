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


package org.apache.isis.metamodel.adapter.oid;

import java.io.IOException;
import java.io.Serializable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.metamodel.encoding.DataInputExtended;
import org.apache.isis.metamodel.encoding.DataOutputExtended;


/**
 * Used as the {@link Oid} for collections, values and <tt>@Aggregated</tt> types.
 * 
 * <p>
 * The Hibernate Object Store has custom handling for collections.
 */
public class AggregatedOid implements Oid, Serializable {

    private static final long serialVersionUID = 1L;

	private static <T> T ensureNotNull(final T oid, String message) {
		Assert.assertNotNull(message, oid);
		return oid;
	}

    private final Oid parentOid;
    private final String fieldName;
    private final int element;
    
    private AggregatedOid previous;

	private int cachedHashCode;

	
    ///////////////////////////////////////////////////////////
    // Constructor, Encodeable
    ///////////////////////////////////////////////////////////

    public AggregatedOid(final Oid oid, final String id) {
        this(oid, id, -1);
    }
    
    public AggregatedOid(final Oid oid, final String id, final int element) {
        Assert.assertNotNull("Field required", id);
        this.parentOid = oid;
        this.fieldName = id;
        this.element = element;
        initialized();
    }

    public AggregatedOid(final Oid oid, final Identifier identifier) {
        this(oid,  
        	ensureNotNull(identifier, "Field required").getMemberName());
    }

    public AggregatedOid(DataInputExtended input) throws IOException {
    	this.parentOid = input.readEncodable(Oid.class);
    	this.fieldName = input.readUTF();
    	this.element = input.readInt();
    	initialized();
    }
    
    public void encode(DataOutputExtended output) throws IOException {
    	output.writeEncodable(parentOid);
        output.writeUTF(fieldName);
        output.writeInt(element);
    }

	private void initialized() {
		cacheState();
	}

    ///////////////////////////////////////////////////////////
    // Properties
    ///////////////////////////////////////////////////////////

    public Oid getParentOid() {
        return parentOid;
    }

    public String getFieldName() {
        return fieldName;
    }
    
    public int getElement() {
        return element;
    }

    
    ///////////////////////////////////////////////////////////
    // makePersistent
    ///////////////////////////////////////////////////////////

    public void makePersistent() {
        this.previous = new AggregatedOid(this.parentOid, this.fieldName, this.element);
        cacheState();
    }
    
    ///////////////////////////////////////////////////////////
    // Previous
    ///////////////////////////////////////////////////////////

    public Oid getPrevious() {
        return previous;
    }

    public boolean hasPrevious() {
        return false;
    }

    public void clearPrevious() {}


    
    ///////////////////////////////////////////////////////////
    // Other OID stuff
    ///////////////////////////////////////////////////////////

    public void copyFrom(final Oid oid) {
        throw new NotYetImplementedException();
    }

    public boolean isTransient() {
        return parentOid.isTransient();
    }

    
    ///////////////////////////////////////////////////////////
    // Value semantics
    ///////////////////////////////////////////////////////////
    
    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        return equals((AggregatedOid)other);
    }
    
    public boolean equals(final AggregatedOid other) {
        return other.parentOid.equals(parentOid) &&  other.fieldName.equals(fieldName) && other.element == element;
    }

    @Override
    public int hashCode() {
        cacheState();
        return cachedHashCode;
    }

	private void cacheState() {
		int hashCode = 17;
        hashCode = 37 * hashCode + parentOid.hashCode();
        hashCode = 37 * hashCode + fieldName.hashCode();
        hashCode = 37 * hashCode + element;
        cachedHashCode = hashCode;
	}

    
    ///////////////////////////////////////////////////////////
    // toString
    ///////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "AOID[" + parentOid + "," + fieldName + (element == -1 ? "" : "," + element) + "]";
    }



}

