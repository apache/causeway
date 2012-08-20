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

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.commons.url.UrlEncodingUtils;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

import com.google.common.base.Objects;

public final class RootOidDefault implements Serializable, RootOid {

    private static final long serialVersionUID = 1L;

    private final ObjectSpecId objectSpecId;
    private final String identifier;
    private final State state;
    
    // not part of equality check
    private final Long version;

    private int cachedHashCode;

    
    
    // ////////////////////////////////////////////
    // Constructor, factory methods
    // ////////////////////////////////////////////

    public static RootOidDefault create(ObjectSpecId objectSpecId, final String identifier) {
        return create(objectSpecId, identifier, null);
    }

    public static RootOidDefault create(ObjectSpecId objectSpecId, final String identifier, Long version) {
        return new RootOidDefault(objectSpecId, identifier, State.PERSISTENT, version);
    }

    public static RootOidDefault createTransient(ObjectSpecId objectSpecId, final String identifier) {
        return createTransient(objectSpecId, identifier, null);
    }

    public static RootOidDefault createTransient(ObjectSpecId objectSpecId, final String identifier, Long version) {
        return new RootOidDefault(objectSpecId, identifier, State.TRANSIENT, version);
    }

    public RootOidDefault(ObjectSpecId objectSpecId, final String identifier, final State state) {
    	this(objectSpecId, identifier, state, null);
    }

    public RootOidDefault(ObjectSpecId objectSpecId, final String identifier, final State state, Long version) {
		Ensure.ensureThatArg(objectSpecId, is(not(nullValue())));
        Ensure.ensureThatArg(identifier, is(not(nullValue())));
        Ensure.ensureThatArg(identifier, is(not(IsisMatchers.contains("#"))));
        Ensure.ensureThatArg(identifier, is(not(IsisMatchers.contains("@"))));
        Ensure.ensureThatArg(state, is(not(nullValue())));
        
        this.objectSpecId = objectSpecId;
        this.identifier = identifier;
        this.state = state;
        this.version = version;
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
        final RootOidDefault oid = getEncodingMarshaller().unmarshal(oidStr, RootOidDefault.class);
        this.objectSpecId = oid.objectSpecId;
        this.identifier = oid.identifier;
        this.state = oid.state;
        this.version = oid.version;
        cacheState();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        output.writeUTF(enString(getEncodingMarshaller()));
    }


    /**
     * Cannot be a reference because Oid gets serialized by wicket viewer
     */
    private OidMarshaller getEncodingMarshaller() {
        return new OidMarshaller();
    }


    // ////////////////////////////////////////////
    // deString'able, enString
    // ////////////////////////////////////////////

    public static RootOid deStringEncoded(final String urlEncodedOidStr, OidMarshaller oidMarshaller) {
        final String oidStr = UrlEncodingUtils.urlDecode(urlEncodedOidStr);
        return deString(oidStr, oidMarshaller);
    }

    public static RootOidDefault deString(final String oidStr, OidMarshaller oidMarshaller) {
		return oidMarshaller.unmarshal(oidStr, RootOidDefault.class);
    }

    @Override
    public String enString(OidMarshaller oidMarshaller) {
        return oidMarshaller.marshal(this);
    }

    @Override
    public String enStringNoVersion(OidMarshaller oidMarshaller) {
        return oidMarshaller.marshalNoVersion(this);
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


	public Long getVersion() {
		return version;
	}

    @Override
    public Comparison compareAgainst(RootOid other) {
        if(!equals(other)) {
            return Comparison.NOT_EQUIVALENT;
        }
        if(getVersion() == null || other.getVersion() == null) {
            return Comparison.EQUIVALENT_BUT_NO_VERSION_INFO;
        }
        return getVersion().equals(other.getVersion()) 
                ? Comparison.EQUIVALENT_AND_UNCHANGED
                : Comparison.EQUIVALENT_BUT_CHANGED;
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
        return equals((RootOid) other);
    }

    public boolean equals(final RootOid other) {
        return Objects.equal(objectSpecId, other.getObjectSpecId()) && Objects.equal(identifier, other.getIdentifier()) && Objects.equal(isTransient(), other.isTransient());
    }

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    @Override
    public String toString() {
        return enString(new OidMarshaller());
    }

    


}
