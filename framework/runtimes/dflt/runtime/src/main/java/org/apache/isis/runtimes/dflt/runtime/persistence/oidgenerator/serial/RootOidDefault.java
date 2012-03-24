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

package org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;

import org.apache.isis.core.commons.encoding.DataInputExtended;
import org.apache.isis.core.commons.encoding.DataOutputExtended;
import org.apache.isis.core.commons.encoding.Encodable;
import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PojoAdapterBuilder;

public final class RootOidDefault implements Encodable, Serializable, RootOid {

    private static final long serialVersionUID = 1L;

    private final String objectType;
    private String identifier;

    private int hashCode;
    private State state;
    private String toString;
    private String enString;
    
    private static final String TOSTRING_SERIAL_NUM_PREFIX = "#";

    private static final String OBJECT_TYPE_PREFIX = ":";
    private static final String ENSTRING_SERIAL_NUM_PREFIX = ":";
    
    // identifier cannot contain @ or #
    // @ is used by AggregateOid as an appendix
    // # is used in REST URLs, so want to avoid possible clashes
    private static Pattern DESTRING_PATTERN = 
            Pattern.compile("^" +
            		"(T?)OID" +
                    OBJECT_TYPE_PREFIX + "([^" + ENSTRING_SERIAL_NUM_PREFIX + "]+)" +
                    ENSTRING_SERIAL_NUM_PREFIX + "(-?[^#@]+)" +  
            		"$");

    
    // ////////////////////////////////////////////
    // Constructor, factory methods
    // ////////////////////////////////////////////

    public static RootOidDefault create(String objectType, final String identifier) {
        return new RootOidDefault(objectType, identifier, State.PERSISTENT);
    }

    public static RootOidDefault createTransient(String objectType, final String identifier) {
        return new RootOidDefault(objectType, identifier, State.TRANSIENT);
    }

    /**
     * Primarily for tests
     */
    public static RootOidDefault create(String oidStr) {
        final Iterator<String> iterator = Splitter.on("|").split(oidStr).iterator();
        if(!iterator.hasNext()) { throw new IllegalArgumentException("expected oid in form XXX|123; oidStr: " + oidStr); }
        String objectType = iterator.next();
        if(!iterator.hasNext()) { throw new IllegalArgumentException("expected oid in form XXX|123; oidStr: " + oidStr); }
        String identifier = iterator.next();
        return create(objectType, identifier);
    }

    /**
     * Primarily for tests
     */
    public static RootOidDefault createTransient(String oidStr) {
        final Iterator<String> iterator = Splitter.on("|").split(oidStr).iterator();
        if(!iterator.hasNext()) { throw new IllegalArgumentException("expected oid in form XXX|123; oidStr: " + oidStr); }
        String objectType = iterator.next();
        if(!iterator.hasNext()) { throw new IllegalArgumentException("expected oid in form XXX|123; oidStr: " + oidStr); }
        String identifier = iterator.next();
        return createTransient(objectType, identifier);
    }

    public RootOidDefault(String objectType, final String identifier, final State state) {
        this.objectType = objectType;
        this.identifier = identifier;
        this.state = state;
        initialized();
    }

    private void initialized() {
        cacheState();
    }

    // ////////////////////////////////////////////
    // Encodeable, deString'able, enString
    // ////////////////////////////////////////////

    public RootOidDefault(final DataInputExtended input) throws IOException {
        this.objectType = input.readUTF();
        this.identifier = input.readUTF();
        this.state = input.readBoolean() ? State.TRANSIENT : State.PERSISTENT;
        initialized();
    }

    @Override
    public void encode(final DataOutputExtended output) throws IOException {
        output.writeUTF(this.objectType);
        output.writeUTF(this.identifier);
        output.writeBoolean(this.state.isTransient());
    }


    // ////////////////////////////////////////////
    // deString'able, enString
    // ////////////////////////////////////////////

    /**
     * @see DirectlyStringableOid
     * @see #enString()
     */
    public static RootOidDefault deString(final String oidStr) {
        final Matcher matcher = DESTRING_PATTERN.matcher(oidStr);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Could not parse OID '" + oidStr + "'; should match pattern: " + DESTRING_PATTERN.toString());
        }

        final String transientStr = matcher.group(1);
        final String objectType = matcher.group(2);
        final String serialNumInHexStr = matcher.group(3);
        
        final RootOidDefault oid = createOid(objectType, transientStr, serialNumInHexStr);

        return oid;
    }

    private static RootOidDefault createOid(String objectType, final String transientStr, final String identifier) {
        final boolean isTransient = "T".equals(transientStr);
        return isTransient ? RootOidDefault.createTransient(objectType, identifier) : RootOidDefault.create(objectType, identifier);
    }

    /**
     * Returns a well-defined format which can be converted back using
     * {@link #deString(String)}.
     * 
     * <p>
     * The options are:
     * <ul>
     * <li>For transient with no previous: <tt>TOID#12AB</tt> where the initial
     * T indicates transient and after the # is the serial number in hex.</li>
     * <li>For persistent with previous: <tt>OID#12ED+TOID#12AB</tt> where after
     * the + is the previous OID, encoded</li>
     * <li>For persistent with no previous: <tt>OID#12ED</tt>.</li>
     * 
     * @see DirectlyStringableOid
     * @see #deString(String)
     */
    @Override
    public String enString() {
        return enString;
    }

    // ////////////////////////////////////////////
    // Properties
    // ////////////////////////////////////////////

    @Override
    public String getObjectType() {
        return objectType;
    }


    @Override
    public boolean isTransient() {
        return state.isTransient();
    }

    public String getIdentifier() {
        return identifier;
    }


    // ////////////////////////////////////////////
    // makePersistent
    // ////////////////////////////////////////////

    
    @Override
    public RootOidDefault asPersistent(String identifier) {
        Ensure.ensureThatState(state.isTransient(), is(true));
        Ensure.ensureThatArg(identifier, is(not(nullValue())));

        return new RootOidDefault(objectType, identifier, State.PERSISTENT);
    }


    // ////////////////////////////////////////////
    // equals, hashCode
    // ////////////////////////////////////////////

    private void cacheState() {
        hashCode = 17;
        hashCode = 37 * hashCode + objectType.hashCode();
        hashCode = 37 * hashCode + identifier.hashCode();
        hashCode = 37 * hashCode + (isTransient() ? 0 : 1);
        toString = asString(this, TOSTRING_SERIAL_NUM_PREFIX);
        enString = asString(this, ENSTRING_SERIAL_NUM_PREFIX);
    }

    private static String asString(final RootOidDefault x, final String serialNumPrefix) {
        return (x.isTransient() ? "T" : "") + "OID" + OBJECT_TYPE_PREFIX + x.objectType + serialNumPrefix + x.identifier;
    }

    /*
     * public void setPrevious(SerialOid previous) {
     * Assert.assertNull(previous); this.previous = previous; }
     */

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
        return other.identifier == identifier && other.state == state;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return toString;
    }



}
