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

package org.apache.isis.runtimes.dflt.runtime.testsystem;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.OidGeneratorAbstract;

public class TestProxyOidGenerator extends OidGeneratorAbstract {
    private int transientId = 1;
    private int persistentId = 90000;

    private TestProxyOid createOid() {
        return new TestProxyOid(transientId++);
    }

    public String name() {
        return "";
    }

    @Override
    public TestProxyOid createTransientOid(final Object object) {
        return createOid();
    }

    @Override
    public void convertTransientToPersistentOid(final Oid oid) {
        final TestProxyOid testProxyOid = (TestProxyOid) oid;
        testProxyOid.setNewId(persistentId++);
        testProxyOid.makePersistent();
    }

    @Override
    public String createAggregateId(final Object pojo) {
        return "8";
    }

    @Override
    public void debugData(final DebugBuilder debug) {
    }

    @Override
    public String debugTitle() {
        return null;
    }

}
