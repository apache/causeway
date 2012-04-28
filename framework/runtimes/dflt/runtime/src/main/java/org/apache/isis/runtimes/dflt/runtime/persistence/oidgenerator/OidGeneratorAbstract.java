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

package org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator;

import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;

public abstract class OidGeneratorAbstract implements OidGenerator {

    private final OidStringifier oidStringifier;

    public OidGeneratorAbstract(Class<? extends Oid> oidClass) {
        this.oidStringifier = new OidStringifier(oidClass);
    }

    // //////////////////////////////////////////////////////////////
    // open, close (session scoped)
    // //////////////////////////////////////////////////////////////

    /**
     * Default implementation does nothing.
     */
    @Override
    public void open() {
    }

    /**
     * Default implementation does nothing.
     */
    @Override
    public void close() {
    }

    
    // //////////////////////////////////////////////////////////////
    // OidStringifier
    // //////////////////////////////////////////////////////////////


    @Override
    public final OidStringifier getOidStringifier() {
        return oidStringifier;
    }

    // ////////////////////////////////////////////////////////////////////
    // injectInto
    // ////////////////////////////////////////////////////////////////////

    @Override
    public void injectInto(final Object candidate) {
        if (OidGeneratorAware.class.isAssignableFrom(candidate.getClass())) {
            final OidGeneratorAware cast = OidGeneratorAware.class.cast(candidate);
            cast.setOidGenerator(this);
        }
    }

}
