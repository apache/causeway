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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import java.io.IOException;
import java.io.Serializable;

import com.google.common.base.Objects;

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

public final class RootOidDefault implements Serializable, RootOid {

    private static final long serialVersionUID = 1L;

    private final ObjectSpecId objectSpecId;
    private String identifier;
    private State state;

    private int cachedHashCode;
    
    
    // ////////////////////////////////////////////
    // Constructor, factory methods
    // ////////////////////////////////////////////

    public static RootOidDefault create(ObjectSpecId objectSpecId, final String identifier) {
        return new RootOidDefault(objectSpecId, identifier, State.PERSISTENT);
    }

    public static RootOidDefault createTransient(ObjectSpecId objectSpecId, final String identifier) {
        return new RootOidDefault(objectSpecId, identifier, State.TRANSIENT);
    }

    public RootOidDefault(ObjectSpecId objectSpecId, final String identifier, final State state) {
        Ensure.ensureThatArg(objectSpecId, is(not(nullValue())));
        Ensure.ensureThatArg(identifier, is(not(nullValue())));
        Ensure.ensureThatArg(identifier, is(not(IsisMatchers.contains("#"))));
        Ensure.ensureThatArg(identifier, is(not(IsisMatchers.contains("@"))));
        Ensure.ensureThatArg(state, is(not(nullValue())));
        
        this.objectSpecId = objectSpecId;
        this.identifier = identifier;
        this.state = state;
        initialized();
    }

    private void initialized() {
        cacheState();
    }

    // ////////////////////////////////////////////
    // Encodeable
    // ////////////////////////////////////////////

    public RootOidDefault(final DataInputExtended input) throws IOException {
        final String oidStr = input.readUTF();
        final RootOidDefault oid = getOidMarshaller().unmarshal(oidStr, RootOidDefault.class);
        this.objectSpecId = oid.objectSpecId;
        this.identifier = oid.identifier;
        this.state = oid.state;
        cacheState();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        output.writeUTF(enString());
    }


    // ////////////////////////////////////////////
    // deString'able, enString
    // ////////////////////////////////////////////

    public static RootOidDefault deString(final String oidStr) {
        return getOidMarshaller().unmarshal(oidStr, RootOidDefault.class);
    }

    @Override
    public String enString() {
        return getOidMarshaller().marshal(this);
    }

    // ////////////////////////////////////////////
    // Properties
    // ////////////////////////////////////////////

    @Override
    public ObjectSpecId getObjectSpecId() {
        return objectSpecId;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean isTransient() {
        return state.isTransient();
    }


    // ////////////////////////////////////////////
    // asPersistent
    // ////////////////////////////////////////////

    
    @Override
    public RootOidDefault asPersistent(String identifier) {
        Ensure.ensureThatState(state.isTransient(), is(true));
        Ensure.ensureThatArg(identifier, is(not(nullValue())));

        return new RootOidDefault(objectSpecId, identifier, State.PERSISTENT);
    }


    // ////////////////////////////////////////////
    // equals, hashCode
    // ////////////////////////////////////////////

    private void cacheState() {
        cachedHashCode = 17;
        cachedHashCode = 37 * cachedHashCode + objectSpecId.hashCode();
        cachedHashCode = 37 * cachedHashCode + identifier.hashCode();
        cachedHashCode = 37 * cachedHashCode + (isTransient() ? 0 : 1);
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        return equals((RootOidDefault) other);
    }

    public boolean equals(final RootOidDefault other) {
        return Objects.equal(objectSpecId, other.objectSpecId) && Objects.equal(identifier, other.identifier) && Objects.equal(state, other.state);
    }

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    @Override
    public String toString() {
        return enString();
    }

    protected static OidMarshaller getOidMarshaller() {
        return new OidMarshaller();
    }



}
