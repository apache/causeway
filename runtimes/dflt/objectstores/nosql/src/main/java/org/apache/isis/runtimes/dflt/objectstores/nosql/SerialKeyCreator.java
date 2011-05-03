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

package org.apache.isis.runtimes.dflt.objectstores.nosql;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;

public class SerialKeyCreator implements KeyCreator {

    @Override
    public String key(final Oid oid) {
        if (oid.isTransient()) {
            throw new NoSqlStoreException("Oid is not for a persistent object: " + oid);
        }
        if (oid instanceof SerialOid) {
            final long serialNo = ((SerialOid) oid).getSerialNo();
            return Long.toString(serialNo, 16);
        } else {
            throw new NoSqlStoreException("Oid is not a SerialOid: " + oid);
        }
    }

    @Override
    public String reference(final ObjectAdapter object) {
        try {
            return object.getSpecification().getFullIdentifier() + "@" + key(object.getOid());
        } catch (final NoSqlStoreException e) {
            throw new NoSqlStoreException("Failed to create refence for " + object, e);
        }
    }

    @Override
    public SerialOid oid(final String id) {
        return SerialOid.createPersistent(Long.valueOf(id, 16).longValue());
    }

    @Override
    public Oid oidFromReference(final String ref) {
        final String id = ref.split("@")[1];
        return oid(id);
    }

    @Override
    public ObjectSpecification specificationFromReference(final String ref) {
        final String name = ref.split("@")[0];
        return IsisContext.getSpecificationLoader().loadSpecification(name);
    }

}
