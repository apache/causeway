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

package org.apache.isis.core.runtime.system.persistence;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.matchers.IsisMatchers.greaterThan;
import static org.hamcrest.CoreMatchers.is;

public class IdentifierGeneratorDefault implements IdentifierGenerator {

    public static class Memento {
        private final long persistentSerialNumber;
        private final long transientSerialNumber;

        Memento(final long persistentSerialNumber, final long transientSerialNumber) {
            this.persistentSerialNumber = persistentSerialNumber;
            this.transientSerialNumber = transientSerialNumber;
        }

        public long getTransientSerialNumber() {
            return transientSerialNumber;
        }

        public long getPersistentSerialNumber() {
            return persistentSerialNumber;
        }
    }

    private long persistentSerialNumber;
    private long transientSerialNumber;
    private long aggregatedId;

    // //////////////////////////////////////////////////////////////
    // constructor
    // //////////////////////////////////////////////////////////////

    public IdentifierGeneratorDefault() {
        this(1);
    }

    /**
     * Persistent {@link Oid}s count up from the provided seed parameter, while
     * {@link Oid#isTransient()} transient {@link Oid}s count down.
     */
    public IdentifierGeneratorDefault(final long seed) {
        this(seed, Long.MIN_VALUE + seed);
    }

    public IdentifierGeneratorDefault(final Memento memento) {
        this(memento.getPersistentSerialNumber(), memento.getTransientSerialNumber());
    }

    public IdentifierGeneratorDefault(final long persistentSerialNumber, final long transientSerialNumber) {
        ensureThatArg(persistentSerialNumber, is(greaterThan(0L)));
        this.persistentSerialNumber = persistentSerialNumber;
        this.transientSerialNumber = transientSerialNumber;
    }

    // //////////////////////////////////////////////////////////////
    // name
    // //////////////////////////////////////////////////////////////

    public String name() {
        return "Default Identifier Generator";
    }

    // //////////////////////////////////////////////////////////////
    // main API and hooks
    // //////////////////////////////////////////////////////////////

    @Override
    public String createAggregateLocalId(ObjectSpecId objectSpecId, final Object pojo, final ObjectAdapter parentAdapter) {
        return Long.toHexString(aggregatedId++);
    }
    
    @Override
    public String createTransientIdentifierFor(ObjectSpecId objectSpecId, Object pojo) {
         // counts down
        return "" + (transientSerialNumber--);
    }

    @Override
    public String createPersistentIdentifierFor(ObjectSpecId objectSpecId, Object pojo, RootOid transientRootOid) {
        return "" + (persistentSerialNumber++); // counts up
    }

    @Override
    public <T extends IdentifierGenerator> T underlying(Class<T> cls) {
        return cls == IdentifierGeneratorDefault.class? (T) this : null;
    }


    // //////////////////////////////////////////////////////////////
    // Memento (not API)
    // //////////////////////////////////////////////////////////////

    public Memento getMemento() {
        return new Memento(this.persistentSerialNumber, this.transientSerialNumber);
    }

    /**
     * Reset to a {@link Memento} previously obtained via {@link #getMemento()}.
     * 
     * <p>
     * Used in particular by the <tt>InMemoryObjectStore</tt> to reset (a new
     * {@link OidGenerator} is created each time).
     */
    public void resetTo(final Memento memento) {
        this.persistentSerialNumber = memento.getPersistentSerialNumber();
        this.transientSerialNumber = memento.getTransientSerialNumber();
    }

    // //////////////////////////////////////////////////////////////
    // debug
    // //////////////////////////////////////////////////////////////

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendln("Persistent", persistentSerialNumber);
        debug.appendln("Transient", transientSerialNumber);
    }

    @Override
    public String debugTitle() {
        return name();
    }

}
