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


package org.apache.isis.extensions.sql.objectstore;

import java.io.IOException;

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.encoding.DataInputExtended;
import org.apache.isis.core.metamodel.encoding.DataOutputExtended;


public final class SqlOid implements Oid {
    private static final long serialVersionUID = 1L;

    public static enum State {
        PERSISTENT,
        TRANSIENT;
        public boolean isTransient() {
            return this == TRANSIENT;
        }
    }

    
    ////////////////////////////////////////////////////
    // Factory methods
    ////////////////////////////////////////////////////
    
    public static SqlOid createPersistent(final String className, final PrimaryKey primaryKey) {
        return new SqlOid(className, primaryKey, State.PERSISTENT);
    }

    public static SqlOid createTransient(final String className, final long serialNo) {
        return new SqlOid(className, new TransientKey(serialNo), State.TRANSIENT);
    }

    
    ////////////////////////////////////////////////////
    // Constructors, encode
    ////////////////////////////////////////////////////

    private String className;
    private SqlOid previous;
    private PrimaryKey primaryKey;
    private PrimaryKey newPrimaryKey;
    private State state;

    public SqlOid(final String className, final PrimaryKey primaryKey, final State state) {
        this.className = className;
        this.primaryKey = primaryKey;
        this.state = state;
        initialized();
    }

    public SqlOid(DataInputExtended input) throws IOException {
    	this.className = input.readUTF();
    	this.primaryKey = input.readSerializable(PrimaryKey.class);
    	this.newPrimaryKey = input.readSerializable(PrimaryKey.class);
    	this.previous = input.readEncodable(SqlOid.class);
    	this.state = input.readSerializable(State.class);
    	initialized();
    }

	public void encode(DataOutputExtended output) throws IOException {
        output.writeUTF(className);
        output.writeSerializable(primaryKey);
        output.writeSerializable(newPrimaryKey);
        output.writeEncodable(previous);
        output.writeSerializable(state);
    }


    private void initialized() {
		// nothing to do
	}


    ////////////////////////////////////////////////////
    // impl
    ////////////////////////////////////////////////////

    public void copyFrom(final Oid oid) {
        Assert.assertTrue(oid instanceof SqlOid);
        SqlOid from = (SqlOid) oid;
        this.primaryKey = from.primaryKey;
        this.className = from.className;
    }

    public String getClassName() {
        return className;
    }

    public PrimaryKey getPrimaryKey() {
    	return primaryKey;
    }
    
    public boolean isTransient() {
    	// TODO: why not look at the State?
    	return primaryKey instanceof TransientKey;
    }
    
    /**
     * Should be called prior to {@link #makePersistent()}
     */
    public void setId(final PrimaryKey primaryKey) {
        Assert.assertTrue(state.isTransient());
        this.newPrimaryKey = primaryKey;
    }

    public void makePersistent() {
        Assert.assertTrue(state.isTransient());
        Assert.assertNotNull(newPrimaryKey);
        previous = new SqlOid(this.className, this.primaryKey, state);
        this.primaryKey = newPrimaryKey;
        this.state = State.PERSISTENT;
        
    }

    public boolean hasPrevious() {
    	return previous != null;
    }
    
    public Oid getPrevious() {
    	return previous;
    }
    
    public void clearPrevious() {
        previous = null;
    }

    ////////////////////////////////////////////////////
    // equals, hashCode
    ////////////////////////////////////////////////////

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof SqlOid) {
            SqlOid otherOid = ((SqlOid) obj);
            return className.equals(otherOid.className) && primaryKey.equals(otherOid.primaryKey);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode = 37 * hashCode + className.hashCode();
        hashCode = 37 * hashCode + primaryKey.hashCode();
        return hashCode;
    }


    ////////////////////////////////////////////////////
    // toString
    ////////////////////////////////////////////////////

    @Override
    public String toString() {
        return (isTransient() ? "T" : "") + "OID#" + primaryKey.stringValue() + "/" + className + (previous == null ? "" : "+");
    }


}
