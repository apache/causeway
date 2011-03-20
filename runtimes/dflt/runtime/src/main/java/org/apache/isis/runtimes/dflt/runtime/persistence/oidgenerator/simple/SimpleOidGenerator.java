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


package org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.stringable.directly.OidStringifierDirect;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.OidGeneratorAbstract;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.matchers.IsisMatchers.greaterThan;
import static org.hamcrest.CoreMatchers.is;


/**
 * Generates OIDs based on monotonically.
 * 
 * <p>
 * Specifies the {@link OidStringifierDirect} as the {@link #getOidStringifier() OID stringifier} 
 * ({@link SerialOid} is conformant)).
 */
public class SimpleOidGenerator extends OidGeneratorAbstract {

	public static class Memento {
		private long persistentSerialNumber;
		private long transientSerialNumber;
	    Memento(long persistentSerialNumber, long transientSerialNumber) {
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

    
    ////////////////////////////////////////////////////////////////
    // constructor
    ////////////////////////////////////////////////////////////////

    public SimpleOidGenerator() {
        this(1);
    }

    /**
     * Persistent {@link Oid}s count up from the provided seed parameter, while
     * {@link Oid#isTransient()} transient {@link Oid}s count down.  
     */
    public SimpleOidGenerator(final long seed) {
        this(seed, Long.MIN_VALUE + seed);
    }

    public SimpleOidGenerator(Memento memento) {
        this(memento.getPersistentSerialNumber(), memento.getTransientSerialNumber());
    }

    private SimpleOidGenerator(long persistentSerialNumber, long transientSerialNumber) {
    	super(new OidStringifierDirect(SerialOid.class));
        ensureThatArg(persistentSerialNumber, is(greaterThan(0L)));
		this.persistentSerialNumber = persistentSerialNumber;
		this.transientSerialNumber = transientSerialNumber;
	}

	////////////////////////////////////////////////////////////////
    // name
    ////////////////////////////////////////////////////////////////


    public String name() {
        return "Simple Serial OID Generator";
    }

    ////////////////////////////////////////////////////////////////
    // main API
    ////////////////////////////////////////////////////////////////


    public synchronized SerialOid createTransientOid(final Object object) {
        return SerialOid.createTransient(transientSerialNumber--); // counts down
    }

    public synchronized void convertTransientToPersistentOid(final Oid oid) {
    	if (!(oid instanceof SerialOid)) {
    		throw new IllegalArgumentException("Oid is not a SerialOid");
    	}
        final SerialOid serialOid = (SerialOid) oid;
        serialOid.setId(persistentSerialNumber++); // counts up
        serialOid.makePersistent(); 
    }

    public String createAggregateId(Object pojo) {
        return Long.toHexString(aggregatedId++);
    }


    ////////////////////////////////////////////////////////////////
    // Memento (not API)
    ////////////////////////////////////////////////////////////////

    public Memento getMemento() {
    	return new Memento(this.persistentSerialNumber, this.transientSerialNumber);
    }

    /**
     * Reset to a {@link Memento} previously obtained via {@link #getMemento()}.
     * 
     * <p>
     * Used in particular by the <tt>InMemoryObjectStore</tt> to reset (a new {@link OidGenerator}
     * is created each time).
     */
	public void resetTo(Memento memento) {
		this.persistentSerialNumber = memento.getPersistentSerialNumber();
		this.transientSerialNumber = memento.getTransientSerialNumber();
	}

    ////////////////////////////////////////////////////////////////
    // debug
    ////////////////////////////////////////////////////////////////
    

    public void debugData(final DebugString debug) {
        debug.appendln("Persistent", persistentSerialNumber);
        debug.appendln("Transient", transientSerialNumber);
    }

    public String debugTitle() {
        return name();
    }



}
