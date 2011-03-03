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


package org.apache.isis.core.runtime.testsystem;

import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.metamodel.adapter.oid.Oid;


public class TestProxyOid implements Oid {
    
    private static final long serialVersionUID = 1L;

    /**
     * pending, prior to {@link #makePersistent()}.
     */
    private int newId;
    int id;
    private TestProxyOid previous;
    public boolean isTransient = true;

    private int hashCode;


    /**
     * Creates transient.
     */
    public TestProxyOid(final int id) {
        this(id, false);
    }

    /**
     * Creates either persistent or transient.
     */
    public TestProxyOid(final int id, final boolean persistent) {
        this.id = id;
        this.isTransient = !persistent;
        cacheHashCode();
    }

    public void encode(DataOutputExtended outputStream) {
        throw new UnsupportedOperationException();
    }


    public boolean hasPrevious() {
        return previous != null;
    }

    public Oid getPrevious() {
        return previous;
    }

    public void copyFrom(final Oid oid) {
        this.id = ((TestProxyOid) oid).id;
        this.isTransient = ((TestProxyOid) oid).isTransient;
        cacheHashCode();
    }

    public boolean isTransient() {
        return isTransient;
    }

    /**
     * Should be called prior to makePersistent
     * @param id
     */
    public void setId(final int id) {
        this.id = id;
    }

    public void makePersistent() {
        this.previous = new TestProxyOid(this.id, !this.isTransient);
        this.isTransient = false;
        this.id = newId;
    }

    public void setupPrevious(final TestProxyOid previous) {
        this.previous = previous;
    }

    public void clearPrevious() {
        previous = null;
    }

	public void setNewId(int newId) {
		this.newId = newId;
		
	}

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestProxyOid other = (TestProxyOid) obj;
        if (id != other.id)
            return false;
        if (isTransient != other.isTransient)
            return false;
        return true;
    }


    private void cacheHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + (isTransient ? 1231 : 1237);
        hashCode = result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "Oid#" + id + (isTransient ? " T" : "") + (hasPrevious() ? " (" + previous + ")" : "; hashCode=" + hashCode);
    }


}
