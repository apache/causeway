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


package org.apache.isis.runtime.persistence.oidgenerator;

import org.apache.isis.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.metamodel.adapter.oid.stringable.hex.OidStringifierHex;



public abstract class OidGeneratorAbstract implements OidGenerator {


    private final OidStringifier oidStringifier;
    
    /**
     * Defaults to using the {@link OidStringifierHex} for {@link #getOidStringifier()}.
     * Subclasses can replace by calling {@link #OidGeneratorAbstract(OidStringifier)} instead.
     */
    public OidGeneratorAbstract() {
    	this(new OidStringifierHex());
    }

    public OidGeneratorAbstract(final OidStringifier oidStringifier) {
    	this.oidStringifier = oidStringifier;
    }

	////////////////////////////////////////////////////////////////
    // open, close (session scoped)
    ////////////////////////////////////////////////////////////////

    /**
     * Default implementation does nothing.
     */
    public void open() {}
    
    /**
     * Default implementation does nothing.
     */
    public void close() {}

    
    /**
     * Default implemenation returns {@link OidStringifierHex}.
     * 
     * <p>
     * Subclasses can replace through constructor if required.
     */
    public final OidStringifier getOidStringifier() {
    	return oidStringifier;
    }
    
    // ////////////////////////////////////////////////////////////////////
    // injectInto
    // ////////////////////////////////////////////////////////////////////

    public void injectInto(Object candidate) {
        if (OidGeneratorAware.class.isAssignableFrom(candidate.getClass())) {
            OidGeneratorAware cast = OidGeneratorAware.class.cast(candidate);
            cast.setOidGenerator(this);
        }
    }



}
