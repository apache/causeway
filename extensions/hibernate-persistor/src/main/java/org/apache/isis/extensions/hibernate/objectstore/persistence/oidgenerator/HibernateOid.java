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


package org.apache.isis.extensions.hibernate.objectstore.persistence.oidgenerator;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.isis.commons.ensure.Assert;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.encoding.DataInputExtended;
import org.apache.isis.metamodel.encoding.DataInputStreamExtended;
import org.apache.isis.metamodel.encoding.DataOutputExtended;


public final class HibernateOid implements Oid {

    /**
     * Standard offset for Oids {@link #HibernateOid(Class, long) initially created transiently}.
     */
    static final int STANDARD_OFFSET = 100000;

    private static final long serialVersionUID = 1L;

    private String className;
    private Serializable primaryKey;
    private Serializable hibernateId;
    private boolean isTransient;
    private HibernateOid previous;

    private int cachedHashCode;
    private String cachedToString;

    private static enum State {
        TRANSIENT,
        PERSISTENT;

        public boolean isTransient() {
            return this == TRANSIENT;
        }
        public boolean isPersistent() {
            return this == PERSISTENT;
        }
    }
    
    ///////////////////////////////////////////////////////////
    // Factory methods
    ///////////////////////////////////////////////////////////
    
    /**
     * Create a new transient instance, creating a primaryKey from the provided <tt>id</tt>.
     * 
     * @see #createTransient(String, Serializable) for postconditions.
     */
    public static HibernateOid createTransient(final Class<?> clazz, final long id) {
        return createTransient(clazz.getName(), id);
    }

    /**
     * Create a new transient instance, creating a primaryKey from the provided <tt>id</tt>.
     * 
     * @see #createTransient(String, Serializable) for postconditions.
     */
    public static HibernateOid createTransient(final String className, final long id) {
        return createTransient(className, new Long(id + STANDARD_OFFSET));
    }

    /**
     * Create a new transient instance, using the specified <tt>primaryKey</tt>.
     * 
     * @see #createTransient(String, Serializable) for postconditions.
     */
    public static HibernateOid createTransient(final Class<?> clazz, final Serializable primaryKey) {
        return createTransient(clazz.getName(), primaryKey);
    }

    /**
     * Create a new transient id.
     * 
     * <p>
     * The {@link #getHibernateId()} will initially be <tt>null</tt>. 
     */
    public static HibernateOid createTransient(final String className, final Serializable primaryKey) {
        return new HibernateOid(className, primaryKey, null, State.TRANSIENT);
    }

    /**
     * Creates a new persistent instance, using the specified <tt>primaryKey</tt>.
     * 
     * @see #createPersistent(String, Serializable) for postconditions.
     */
    public static HibernateOid createPersistent(final Class<?> clazz, final Serializable primaryKey) {
        return createPersistent(clazz.getName(), primaryKey);
    }

    /**
     * Creates a new persistent instance, using the specified <tt>primaryKey</tt>.
     * 
     * <p>
     * The {@link #getHibernateId()} will be the same as the <tt>primaryKey</tt>.
     */
    public static HibernateOid createPersistent(final String className, final Serializable primaryKey) {
        return createPersistent(className, primaryKey, primaryKey);
    }

    /**
     * Creates a new persistent instance, using the specified <tt>primaryKey</tt> and (possibly different)
     * <tt>hibernateId</tt>.
     */
    public static HibernateOid createPersistent(
            final String className,
            final Serializable primaryKey,
            final Serializable hibernateId) {
        return new HibernateOid(className, primaryKey, hibernateId, State.PERSISTENT);
    }

    ///////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////

    private HibernateOid(
            final String className,
            final Serializable primaryKey,
            final Serializable hibernateId,
            final State state) {
        this.className = className;
        this.primaryKey = primaryKey;
        this.hibernateId = hibernateId;
        this.isTransient = state.isTransient();
        // previous is initially null
        initialized();
    }

    public HibernateOid(final DataInputExtended input) throws IOException {
        this.className = input.readUTF();
        this.primaryKey = input.readSerializable(Serializable.class);
        this.isTransient = input.readBoolean();
        this.hibernateId = input.readSerializable(Serializable.class);
        this.previous = input.readSerializable(HibernateOid.class);
        initialized();
    }


    public void encode(final DataOutputExtended output) throws IOException {
    	output.writeUTF(className);
    	output.writeSerializable(primaryKey);
    	output.writeBoolean(isTransient);
    	output.writeSerializable(hibernateId);
    	output.writeEncodable(previous);
    }
    
	private void initialized() {
		cacheState();
	}


    ///////////////////////////////////////////////////////////
    // copyFrom
    ///////////////////////////////////////////////////////////

    public void copyFrom(final Oid oid) {
        Assert.assertTrue(oid instanceof HibernateOid);
        final HibernateOid from = (HibernateOid) oid;
        this.primaryKey = from.primaryKey;
        this.className = from.className;
        this.hibernateId = from.hibernateId;
        this.isTransient = from.isTransient;
        cacheState();
    }

    ///////////////////////////////////////////////////////////
    // className, primaryKey
    ///////////////////////////////////////////////////////////

    /**
     * Used in {@link #equals(Object)}.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Used in {@link #equals(Object)}.
     * 
     * <p>
     * Will be the same as the {@link #getHibernateId()} once the Oid
     * has been {@link #makePersistent() made persistent}.
     * @return
     */
    public Serializable getPrimaryKey() {
        return primaryKey;
    }

    ///////////////////////////////////////////////////////////
    // makePersistent, HibernateId
    ///////////////////////////////////////////////////////////

    /**
     * Use the {@link #getHibernateId() hibernate Id} as the {@link #getPrimaryKey() primary key}, in the
     * process marking the Oid as {@link #isTransient() persistent}, and storing the
     * {@link #getPrevious() previous} value.
     * 
     * <p>
     * Note 1: should be preceded by a call to {@link #setHibernateId(Serializable)} to set the
     * {@link #getHibernateId() hibernate Id}.
     * 
     * <p>
     * Note 2: if called then the {@link #hashCode()} may change; it is the caller's responsibility to manage
     * any {@link Map}s that the Oid might be using.
     * 
     * <p>
     * TODO: should probably combine with {@link #setHibernateId(Serializable)} ??
     */
    public void makePersistent() {
        Assert.assertTrue(isTransient());
        this.previous = new HibernateOid(this.className, this.primaryKey, null, State.TRANSIENT);
        this.primaryKey = hibernateId;
        this.isTransient = false;
        cacheState();
    }


    /**
     * The id to return to hibernate.
     * 
     * <p>
     * Will return <tt>null</tt> if the {@link HibernateOid} has not been
     * {@link #makePersistent() made persistent}.
     */
    public Serializable getHibernateId() {
        return hibernateId;
    }

    /**
     * Update the Id, and recache state.
     * 
     * <p>
     * Note: if called then the {@link #hashCode()} may change; it is the caller's responsibility to manage
     * any {@link Map}s that the Oid might be using.
     */
    public void setHibernateId(final Serializable hibernateId) {
        this.hibernateId = hibernateId;
        cacheState();
    }


    ///////////////////////////////////////////////////////////
    // getPrevious, hasPrevious
    ///////////////////////////////////////////////////////////

    /**
     * The previous Oid, if any.
     * 
     * <p>
     * Will hold this {@link Oid} in its transient form once
     * {@link #makePersistent()} has been called.  This allows
     * client-side code to maintain its Oid->Object maps.
     */
    public Oid getPrevious() {
        return previous;
    }

    public boolean hasPrevious() {
        return previous != null;
    }


    public void clearPrevious() {
        previous = null;
    }

    ///////////////////////////////////////////////////////////
    // isTransient
    ///////////////////////////////////////////////////////////

    public boolean isTransient() {
        return isTransient;
    }


    ///////////////////////////////////////////////////////////
    // equals, hashCode impl.
    ///////////////////////////////////////////////////////////

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof HibernateOid) {
            final HibernateOid o = ((HibernateOid) obj);
            return className.equals(o.className) && primaryKey.equals(o.primaryKey);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    private void cacheState() {
        cachedHashCode = 17;
        cachedHashCode = 37 * cachedHashCode + className.hashCode();
        cachedHashCode = 37 * cachedHashCode + primaryKey.hashCode();
        cachedToString = (isTransient() ? "T" : "") + "HOID#" + primaryKey.toString() + "/" + className
                + (hibernateId == null ? "" : "(" + hibernateId + ")") + (previous == null ? "" : "+");
    }

    
    ///////////////////////////////////////////////////////////
    // toString
    ///////////////////////////////////////////////////////////
    
    @Override
    public String toString() {
        return cachedToString;
    }


}
